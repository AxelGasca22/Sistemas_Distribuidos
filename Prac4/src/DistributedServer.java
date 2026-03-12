import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class DistributedServer {

    private final int serverId;
    private final int clientPort;
    private final int clusterPort;
    private final List<Peer> peers;

    private volatile int leaderId;
    private volatile long lastLeaderHeartbeat = System.currentTimeMillis();

    private final AtomicBoolean runningElection = new AtomicBoolean(false);
    private final AtomicLong sequence = new AtomicLong(0);

    private final BlockingQueue<ClientEvent> eventQueue = new LinkedBlockingQueue<>();
    private final ExecutorService consumers = Executors.newFixedThreadPool(3);

    private final ConcurrentMap<String, Long> clientHeartbeats = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Deque<Integer>> metricsWindow = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> clientStatus = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Integer> resourceReplicas = new ConcurrentHashMap<>();

    public DistributedServer(int serverId, int clientPort, int clusterPort, List<Peer> peers) {
        this.serverId = serverId;
        this.clientPort = clientPort;
        this.clusterPort = clusterPort;
        this.peers = peers;
        this.leaderId = highestServerId(peers, serverId);
    }

    public void start() throws Exception {
        System.out.println("[S" + serverId + "] Iniciando. clientPort=" + clientPort +
                " clusterPort=" + clusterPort + " leaderId inicial=" + leaderId);

        resourceReplicas.put("API", 1);
        resourceReplicas.put("DB", 1);
        resourceReplicas.put("CACHE", 1);

        for (int i = 0; i < 3; i++) {
            consumers.submit(this::consumeEvents);
        }

        new Thread(this::startClientListener, "client-listener-" + serverId).start();
        new Thread(this::startClusterListener, "cluster-listener-" + serverId).start();
        new Thread(this::leaderHeartbeatLoop, "leader-heartbeat-" + serverId).start();
        new Thread(this::leaderWatchdogLoop, "leader-watchdog-" + serverId).start();
        new Thread(this::clientMonitorLoop, "client-monitor-" + serverId).start();
    }

    private void startClientListener() {
        try (ServerSocket serverSocket = new ServerSocket(clientPort)) {
            System.out.println("[S" + serverId + "] Escuchando clientes en " + clientPort);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClient(socket), "client-handler-" + serverId).start();
            }
        } catch (IOException e) {
            System.err.println("[S" + serverId + "] Error listener clientes: " + e.getMessage());
        }
    }

    private void handleClient(Socket socket) {
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String line;
            while ((line = in.readLine()) != null) {
                Protocol.ParsedMessage msg = Protocol.parse(line);
                String clientId = msg.data().getOrDefault("clientId", "unknown");

                switch (msg.type()) {
                    case "REGISTER" -> {
                        enqueueOrForward(new ClientEvent("REGISTER", clientId, msg.data()));
                        out.write("ACK|REGISTERED\n");
                        out.flush();
                    }
                    case "HEARTBEAT" -> {
                        enqueueOrForward(new ClientEvent("HEARTBEAT", clientId, msg.data()));
                        out.write("ACK|HEARTBEAT\n");
                        out.flush();
                    }
                    case "METRIC" -> {
                        enqueueOrForward(new ClientEvent("METRIC", clientId, msg.data()));
                        out.write("ACK|METRIC\n");
                        out.flush();
                    }
                    default -> {
                        out.write("ERROR|UNKNOWN_MESSAGE\n");
                        out.flush();
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("[S" + serverId + "] Error con cliente: " + e.getMessage());
        }
    }

    private void enqueueOrForward(ClientEvent event) {
        if (isLeader()) {
            eventQueue.offer(event);
        } else {
            forwardEventToLeader(event);
        }
    }

    private void forwardEventToLeader(ClientEvent event) {
        Peer leader = findPeer(leaderId);
        if (leader == null) {
            System.err.println("[S" + serverId + "] No encuentro al líder " + leaderId);
            return;
        }

        try (Socket socket = new Socket(leader.host(), leader.clusterPort());
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            Map<String, String> data = new HashMap<>(event.payload());
            data.put("sourceServer", String.valueOf(serverId));
            String line = Protocol.build("CLIENT_FORWARD_" + event.type(), data);
            out.write(line + "\n");
            out.flush();

        } catch (IOException e) {
            System.err.println("[S" + serverId + "] No se pudo reenviar evento al líder: " + e.getMessage());
        }
    }

    private void consumeEvents() {
        while (true) {
            try {
                ClientEvent event = eventQueue.take();
                processClientEvent(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                System.err.println("[S" + serverId + "] Error procesando evento: " + e.getMessage());
            }
        }
    }

    private void processClientEvent(ClientEvent event) {
        String clientId = event.clientId();

        switch (event.type()) {
            case "REGISTER" -> {
                clientStatus.put(clientId, "ALIVE");
                clientHeartbeats.put(clientId, System.currentTimeMillis());
                System.out.println("[S" + serverId + "][LEADER] Cliente registrado: " + clientId);
                replicate("REGISTER_CLIENT", Map.of("clientId", clientId));
            }

            case "HEARTBEAT" -> {
                clientHeartbeats.put(clientId, System.currentTimeMillis());
                clientStatus.put(clientId, "ALIVE");
                System.out.println("[S" + serverId + "][LEADER] Heartbeat de " + clientId);
                replicate("CLIENT_HEARTBEAT", Map.of("clientId", clientId));
            }

            case "METRIC" -> {
                String resource = event.payload().getOrDefault("resource", "API");
                int value = Integer.parseInt(event.payload().getOrDefault("value", "0"));

                metricsWindow.putIfAbsent(resource, new ArrayDeque<>());
                Deque<Integer> deque = metricsWindow.get(resource);

                synchronized (deque) {
                    if (deque.size() >= 5) {
                        deque.removeFirst();
                    }
                    deque.addLast(value);
                }

                int avg = average(deque);
                String state = classify(avg);

                System.out.println("[S" + serverId + "][LEADER] Métrica " + resource +
                        " valor=" + value + " promedio=" + avg + " estado=" + state);

                if ("CRITICAL".equals(state)) {
                    scaleUp(resource);
                } else if ("LOW".equals(state)) {
                    scaleDown(resource);
                }

                replicate("METRIC_APPLIED", Map.of(
                        "resource", resource,
                        "value", String.valueOf(value),
                        "avg", String.valueOf(avg),
                        "state", state
                ));
            }
        }
    }

    private void scaleUp(String resource) {
        resourceReplicas.merge(resource, 1, Integer::sum);
        int replicas = resourceReplicas.get(resource);

        System.out.println("[S" + serverId + "][LEADER] SCALE UP de " + resource +
                " -> replicas=" + replicas);

        replicate("SCALE_UP", Map.of(
                "resource", resource,
                "replicas", String.valueOf(replicas)
        ));
    }

    private void scaleDown(String resource) {
        resourceReplicas.compute(resource, (k, v) -> {
            int current = (v == null) ? 1 : v;
            return Math.max(1, current - 1);
        });

        int replicas = resourceReplicas.get(resource);

        System.out.println("[S" + serverId + "][LEADER] SCALE DOWN de " + resource +
                " -> replicas=" + replicas);

        replicate("SCALE_DOWN", Map.of(
                "resource", resource,
                "replicas", String.valueOf(replicas)
        ));
    }

    private void replicate(String eventType, Map<String, String> data) {
        long seq = sequence.incrementAndGet();

        Map<String, String> payload = new HashMap<>(data);
        payload.put("seq", String.valueOf(seq));
        payload.put("leaderId", String.valueOf(serverId));
        payload.put("time", Instant.now().toString());

        for (Peer peer : peers) {
            if (peer.id() == serverId) continue;

            try (Socket socket = new Socket(peer.host(), peer.clusterPort());
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

                String line = Protocol.build("REPL_" + eventType, payload);
                out.write(line + "\n");
                out.flush();

            } catch (IOException e) {
                System.err.println("[S" + serverId + "] No se pudo replicar a S" + peer.id() +
                        ": " + e.getMessage());
            }
        }
    }

    private void startClusterListener() {
        try (ServerSocket serverSocket = new ServerSocket(clusterPort)) {
            System.out.println("[S" + serverId + "] Escuchando cluster en " + clusterPort);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClusterMessage(socket), "cluster-handler-" + serverId).start();
            }
        } catch (IOException e) {
            System.err.println("[S" + serverId + "] Error listener cluster: " + e.getMessage());
        }
    }

    private void handleClusterMessage(Socket socket) {
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String line = in.readLine();
            if (line == null) return;

            Protocol.ParsedMessage msg = Protocol.parse(line);

            switch (msg.type()) {
                case "LEADER_HEARTBEAT" -> {
                    int newLeader = Integer.parseInt(msg.data().getOrDefault("leaderId", "-1"));
                    leaderId = newLeader;
                    lastLeaderHeartbeat = System.currentTimeMillis();
                }

                case "ELECTION" -> {
                    int candidateId = Integer.parseInt(msg.data().getOrDefault("candidateId", "-1"));
                    if (serverId > candidateId) {
                        out.write("OK|serverId=" + serverId + "\n");
                        out.flush();
                        if (!runningElection.get()) {
                            new Thread(this::startElection).start();
                        }
                    }
                }

                case "COORDINATOR" -> {
                    int newLeader = Integer.parseInt(msg.data().getOrDefault("leaderId", "-1"));
                    leaderId = newLeader;
                    lastLeaderHeartbeat = System.currentTimeMillis();
                    runningElection.set(false);
                    System.out.println("[S" + serverId + "] Nuevo líder anunciado: S" + leaderId);
                }

                case "CLIENT_FORWARD_REGISTER" -> {
                    String clientId = msg.data().get("clientId");
                    eventQueue.offer(new ClientEvent("REGISTER", clientId, msg.data()));
                }

                case "CLIENT_FORWARD_HEARTBEAT" -> {
                    String clientId = msg.data().get("clientId");
                    eventQueue.offer(new ClientEvent("HEARTBEAT", clientId, msg.data()));
                }

                case "CLIENT_FORWARD_METRIC" -> {
                    String clientId = msg.data().get("clientId");
                    eventQueue.offer(new ClientEvent("METRIC", clientId, msg.data()));
                }

                default -> {
                    if (msg.type().startsWith("REPL_")) {
                        applyReplication(msg);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("[S" + serverId + "] Error en mensaje de cluster: " + e.getMessage());
        }
    }

    private void applyReplication(Protocol.ParsedMessage msg) {
        String type = msg.type().replace("REPL_", "");
        String seq = msg.data().getOrDefault("seq", "?");

        switch (type) {
            case "REGISTER_CLIENT" -> {
                String clientId = msg.data().get("clientId");
                clientStatus.put(clientId, "ALIVE");
                clientHeartbeats.put(clientId, System.currentTimeMillis());
            }

            case "CLIENT_HEARTBEAT" -> {
                String clientId = msg.data().get("clientId");
                clientStatus.put(clientId, "ALIVE");
                clientHeartbeats.put(clientId, System.currentTimeMillis());
            }

            case "SCALE_UP", "SCALE_DOWN" -> {
                String resource = msg.data().get("resource");
                int replicas = Integer.parseInt(msg.data().get("replicas"));
                resourceReplicas.put(resource, replicas);
            }

            case "REMOVE_CLIENT" -> {
                String clientId = msg.data().get("clientId");
                clientStatus.put(clientId, "DEAD");
            }
        }

        System.out.println("[S" + serverId + "][FOLLOWER] Aplicó evento replicado " +
                type + " seq=" + seq);
    }

    private void leaderHeartbeatLoop() {
        while (true) {
            try {
                if (isLeader()) {
                    for (Peer peer : peers) {
                        if (peer.id() == serverId) continue;

                        try (Socket socket = new Socket(peer.host(), peer.clusterPort());
                             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

                            String line = Protocol.build("LEADER_HEARTBEAT",
                                    Map.of("leaderId", String.valueOf(serverId)));
                            out.write(line + "\n");
                            out.flush();

                        } catch (IOException ignored) {
                        }
                    }
                }
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void leaderWatchdogLoop() {
        while (true) {
            try {
                if (!isLeader()) {
                    long elapsed = System.currentTimeMillis() - lastLeaderHeartbeat;
                    if (elapsed > 5000 && runningElection.compareAndSet(false, true)) {
                        System.out.println("[S" + serverId + "] Líder no responde. Iniciando elección...");
                        startElection();
                    }
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void startElection() {
        boolean higherNodeExists = false;

        for (Peer peer : peers) {
            if (peer.id() <= serverId) continue;

            try (Socket socket = new Socket(peer.host(), peer.clusterPort());
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                String line = Protocol.build("ELECTION", Map.of("candidateId", String.valueOf(serverId)));
                out.write(line + "\n");
                out.flush();

                socket.setSoTimeout(2000);
                String response = in.readLine();
                if (response != null && response.startsWith("OK")) {
                    higherNodeExists = true;
                }

            } catch (IOException ignored) {
            }
        }

        if (!higherNodeExists) {
            becomeLeader();
        } else {
            System.out.println("[S" + serverId + "] Un nodo mayor respondió. Esperando coordinador...");
        }
    }

    private void becomeLeader() {
        leaderId = serverId;
        lastLeaderHeartbeat = System.currentTimeMillis();
        runningElection.set(false);

        System.out.println("[S" + serverId + "] *** AHORA SOY EL LÍDER ***");

        for (Peer peer : peers) {
            if (peer.id() == serverId) continue;

            try (Socket socket = new Socket(peer.host(), peer.clusterPort());
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

                String line = Protocol.build("COORDINATOR", Map.of("leaderId", String.valueOf(serverId)));
                out.write(line + "\n");
                out.flush();

            } catch (IOException ignored) {
            }
        }
    }

    private void clientMonitorLoop() {
        while (true) {
            try {
                if (isLeader()) {
                    long now = System.currentTimeMillis();
                    for (Map.Entry<String, Long> e : clientHeartbeats.entrySet()) {
                        long diff = now - e.getValue();
                        String clientId = e.getKey();

                        if (diff > 8000) {
                            if (!"DEAD".equals(clientStatus.get(clientId))) {
                                clientStatus.put(clientId, "DEAD");
                                System.out.println("[S" + serverId + "][LEADER] Cliente muerto: " + clientId);
                                replicate("REMOVE_CLIENT", Map.of("clientId", clientId));
                            }
                        } else if (diff > 4000) {
                            clientStatus.put(clientId, "SUSPECT");
                        } else {
                            clientStatus.put(clientId, "ALIVE");
                        }
                    }
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private boolean isLeader() {
        return leaderId == serverId;
    }

    private Peer findPeer(int id) {
        for (Peer p : peers) {
            if (p.id() == id) return p;
        }
        return null;
    }

    private static int highestServerId(List<Peer> peers, int selfId) {
        int max = selfId;
        for (Peer p : peers) {
            max = Math.max(max, p.id());
        }
        return max;
    }

    private static int average(Deque<Integer> deque) {
        synchronized (deque) {
            if (deque.isEmpty()) return 0;
            int sum = 0;
            for (int v : deque) sum += v;
            return sum / deque.size();
        }
    }

    private static String classify(int avg) {
        if (avg >= 80) return "CRITICAL";
        if (avg <= 30) return "LOW";
        return "NORMAL";
    }

    public record Peer(int id, String host, int clusterPort) {}
    public record ClientEvent(String type, String clientId, Map<String, String> payload) {}

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Uso: java DistributedServer <serverId> <clientPort> <clusterPort>");
            System.out.println("Ejemplo: java DistributedServer 1 6001 7001");
            return;
        }

        int id = Integer.parseInt(args[0]);
        int clientPort = Integer.parseInt(args[1]);
        int clusterPort = Integer.parseInt(args[2]);

        List<Peer> peers = List.of(
                new Peer(1, "localhost", 7001),
                new Peer(2, "localhost", 7002),
                new Peer(3, "localhost", 7003)
        );

        DistributedServer server = new DistributedServer(id, clientPort, clusterPort, peers);
        server.start();
    }
}