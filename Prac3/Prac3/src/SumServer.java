import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class SumServer {

    private static final int PORT = 5000;
    private static final int QUEUE_CAPACITY = 100;

    private static BlockingQueue<ClientTask> taskQueue =
            new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    private static ExecutorService workerPool =
            Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors()
            );

    static class ClientTask {
        Socket socket;
        long number;

        ClientTask(Socket socket, long number) {
            this.socket = socket;
            this.number = number;
        }
    }

    static class SumTask implements Callable<Long> {
        private final long start;
        private final long end;

        SumTask(long start, long end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public Long call() {
            long sum = 0;
            for (long i = start; i <= end; i++) {
                sum += i;
            }
            return sum;
        }
    }

    private static long parallelSum(long maxNumber) throws Exception {
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);

        long part = maxNumber / cores;
        Future<Long>[] futures = new Future[cores];

        long start = 1;
        for (int i = 0; i < cores; i++) {
            long end = (i == cores - 1) ? maxNumber : start + part - 1;
            futures[i] = executor.submit(new SumTask(start, end));
            start = end + 1;
        }

        long total = 0;
        for (int i = 0; i < cores; i++) {
            total += futures[i].get();
        }

        executor.shutdown();
        return total;
    }

    private static void startWorkers() {
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            workerPool.execute(() -> {
                while (true) {
                    try {
                        ClientTask task = taskQueue.take();
                        long result = parallelSum(task.number);

                        PrintWriter out = new PrintWriter(
                                task.socket.getOutputStream(), true);
                        out.println(result);

                        task.socket.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static void main(String[] args) {
        System.out.println("Servidor iniciado en puerto " + PORT);
        startWorkers();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();

                new Thread(() -> {
                    try {
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                        clientSocket.getInputStream()));

                        String line = in.readLine();
                        long number = Long.parseLong(line.trim());

                        taskQueue.put(new ClientTask(clientSocket, number));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}