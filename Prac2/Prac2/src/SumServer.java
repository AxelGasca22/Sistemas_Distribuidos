import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class SumServer {

    // Tarea de suma parcial (igual que tu lógica original)
    static class SumTask implements Callable<Long> {
        private final long start;
        private final long end;

        public SumTask(long start, long end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public Long call() {
            long sum = 0;
            for (long i = start; i <= end; i++) {
                sum += i;
            }
            System.out.println("Sumando desde " + start + " hasta " + end +
                    " en hilo: " + Thread.currentThread().getName());
            return sum;
        }
    }

    // Calcula la suma 1..maxNumber usando multiprocesamiento
    private static long parallelSum(long maxNumber) throws Exception {
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        System.out.println("Núcleos disponibles: " + numberOfCores);

        ExecutorService executor = Executors.newFixedThreadPool(numberOfCores);

        long part = maxNumber / numberOfCores;
        @SuppressWarnings("unchecked")
        Future<Long>[] futures = new Future[numberOfCores];

        long start = 1;

        for (int i = 0; i < numberOfCores; i++) {
            long end = (i == numberOfCores - 1) ? maxNumber : start + part - 1;
            futures[i] = executor.submit(new SumTask(start, end));
            start = end + 1;
        }

        long totalSum = 0;
        for (int i = 0; i < numberOfCores; i++) {
            totalSum += futures[i].get();
        }

        executor.shutdown();
        return totalSum;
    }

    public static void main(String[] args) {
        int port = 5000;

        System.out.println("Servidor iniciado en puerto " + port + "...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("\nCliente conectado: " + clientSocket.getInetAddress());

                // Para manejar varios clientes a la vez (opcional pero recomendado)
                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (IOException e) {
            System.out.println("Error en servidor: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            // Protocolo simple:
            // Cliente manda: N
            // Servidor responde: SUMA
            String line = in.readLine();
            if (line == null) return;

            long maxNumber = Long.parseLong(line.trim());
            System.out.println("Solicitud recibida: sumar de 1 a " + maxNumber);

            long totalSum = parallelSum(maxNumber);

            out.println(totalSum);
            System.out.println("Resultado enviado al cliente: " + totalSum);

        } catch (Exception e) {
            System.out.println("Error atendiendo cliente: " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }
}
