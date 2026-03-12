import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancer {

    private final int port;
    private final List<Backend> backends;
    private final AtomicInteger index = new AtomicInteger(0);

    public LoadBalancer(int port, List<Backend> backends) {
        this.port = port;
        this.backends = backends;
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("[LB] Load Balancer escuchando en puerto " + port);

        while (true) {
            Socket client = serverSocket.accept();
            new Thread(() -> handleClient(client)).start();
        }
    }

    private void handleClient(Socket client) {
        try (client;
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {

            Backend backend = nextBackend();
            String response = "REDIRECT|" + backend.host + "|" + backend.clientPort + "\n";
            out.write(response);
            out.flush();

            System.out.println("[LB] Redirigiendo cliente a Server-" + backend.id +
                    " (" + backend.host + ":" + backend.clientPort + ")");

        } catch (IOException e) {
            System.err.println("[LB] Error atendiendo cliente: " + e.getMessage());
        }
    }

    private Backend nextBackend() {
        int pos = Math.abs(index.getAndIncrement() % backends.size());
        return backends.get(pos);
    }

    public record Backend(int id, String host, int clientPort) {}

    public static void main(String[] args) throws Exception {
        List<Backend> servers = List.of(
                new Backend(1, "localhost", 6001),
                new Backend(2, "localhost", 6002),
                new Backend(3, "localhost", 6003)
        );

        new LoadBalancer(5000, servers).start();
    }
}