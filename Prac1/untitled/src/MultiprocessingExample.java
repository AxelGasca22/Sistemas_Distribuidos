import java.util.concurrent.Callable;

public class MultiprocessingExample {

    static class SumTask implements Callable<Long> {
        private long start;
        private long end;

        public SumTask(long start, long end) {
            this.start = start;
            this.end = end;
        }
        @Override
        public Long call() throws Exception {

            long sum = 0;
            for(long i = start; i <= end; i++) {
                sum += i;
            }
            System.out.println("Sum from " + start + " to " + end + " en thread " + Thread.currentThread().getName() + ": " + sum);
            return sum;
        }
    }
}
