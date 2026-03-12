import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class DemoClient {

    private final String clientId = "C-" + UUID.randomUUID().toString().substring(0, 8);
    private final String resource;
    private final Random random = new Random();

    public DemoClient(String resource) {
        this.resource = resource;
    }

    public void start() throws Exception {
        RedirectInfo redirect = askLoadBalancer();
        System.out.println("[" + clientId + "] Redirigido a " + redirect.host + ":" + redirect.port);

        try (Socket socket = new Socket(redirect.host, redirect.port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            send(out, "REGISTER", Map.of(
                    "clientId", clientId,
                    "resource", resource
            ));
            System.out.println("[" + clientId + "] " + in.readLine());

            Thread heartbeatThread = new Thread(() -> runHeartbeats(out), "hb-" + clientId);
            Thread metricThread = new Thread(() -> runMetrics(out), "metric-" + clientId);

            heartbeatThread.start();
            metricThread.start();

            heartbeatThread.join();
            metricThread.join();
        }
    }

    private RedirectInfo askLoadBalancer() throws Exception {
        try (Socket socket = new Socket("localhost", 5000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String line = in.readLine();
            if (line == null || !line.startsWith("REDIRECT|")) {
                throw new IllegalStateException("Respuesta inválida del LB: " + line);
            }

            String[] parts = line.split("\\|");
            return new RedirectInfo(parts[1], Integer.parseInt(parts[2]));
        }
    }

    private void runHeartbeats(BufferedWriter out) {
        try {
            while (true) {
                synchronized (out) {
                    send(out, "HEARTBEAT", Map.of(
                            "clientId", clientId
                    ));
                }
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            System.err.println("[" + clientId + "] Heartbeat detenido: " + e.getMessage());
        }
    }

    private void runMetrics(BufferedWriter out) {
        try {
            while (true) {
                int value = random.nextInt(101);

                synchronized (out) {
                    send(out, "METRIC", Map.of(
                            "clientId", clientId,
                            "resource", resource,
                            "value", String.valueOf(value)
                    ));
                }

                System.out.println("[" + clientId + "] Enviando métrica " + resource + "=" + value);
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            System.err.println("[" + clientId + "] Métricas detenidas: " + e.getMessage());
        }
    }

    private void send(BufferedWriter out, String type, Map<String, String> data) throws IOException {
        String line = Protocol.build(type, data);
        out.write(line + "\n");
        out.flush();
    }

    private record RedirectInfo(String host, int port) {}

    public static void main(String[] args) throws Exception {
        String resource = args.length > 0 ? args[0] : "API";
        new DemoClient(resource).start();
    }
}