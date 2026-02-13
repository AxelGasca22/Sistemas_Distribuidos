import java.lang.management.LockInfo;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) {

        int numberOfCores = Runtime.getRuntime().availableProcessors();
        System.out.println("Número de núcleos disponibles: " + numberOfCores);

        ExecutorService executor = Executors.newFixedThreadPool(numberOfCores);

        long maxNumber = 1_000_000;
        long part = maxNumber / numberOfCores;

        Future<Long>[] futures = new Future[numberOfCores];

        long start = 1;

        for(int i = 0; i < numberOfCores; i++) {
            long end = (i == numberOfCores - 1) ? maxNumber : start + part - 1;
            futures[i] = executor.submit(new MultiprocessingExample.SumTask(start, end));
            start += part;
        }

        long totalSum = 0;
        for(int i =0; i < numberOfCores; i++) {
            try {
                totalSum += futures[i].get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        System.out.println("Suma total: " + totalSum);

    }
}