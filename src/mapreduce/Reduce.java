package mapreduce;

public class Reduce {
    public static final int NUM_ELEMENTS = Integer.MAX_VALUE - 3;
    public static final int NUM_THREADS = 8;

    public interface IReduceInt {
        int doReduce(int a, int b);
    }

    public static int reduce(int[] values, int startIndex, int endIndex, IReduceInt reducer, int initial) {
        int value = initial;
        for (int i = startIndex; i < endIndex; i++) {
            value = reducer.doReduce(value, values[i]);
        }
        return value;
    }

    public static void main(String args[]) {
        int values[] = new int[NUM_ELEMENTS];
        for (int i = 0; i < NUM_ELEMENTS; i++) {
            values[i] = i + 1;
        }

        IReduceInt max = new IReduceInt() {
            @Override
            public int doReduce(int a, int b) {
                // A simple max is too fast to show scaling with > 4 threads.
                long wasteSomeTime = a + b + b;
                int aAgain = (int) (wasteSomeTime - b - b);
                return Math.max(aAgain, b);
            }
        };

        long start = System.currentTimeMillis();
        int result = preduce(values, 0, NUM_ELEMENTS, max, 0, NUM_THREADS);
        System.out.println(String.format(
		"Processed %d elements in %.3f seconds.",
		NUM_ELEMENTS,
		(double) (System.currentTimeMillis() - start) / 1000)
	);
        System.out.println(String.format(
		"Heap size: %.2f GB",
		(float) Runtime.getRuntime().totalMemory() / 1024 / 1024 / 1024
	));

        assert result == NUM_ELEMENTS;
    }

    public static int preduce(final int[] values, final int startIndex,
		final int endIndex, final IReduceInt reducer,
		final int initial, final int numThreads) {
	System.out.println(String.format("Running %d threads.", numThreads));
        Thread threads[] = new Thread[numThreads];
        final int results[] = new int[numThreads];
        final int numItemsPerThread = (endIndex - startIndex) / numThreads;

        for (int i = 0; i < numThreads; i++) {
            final int start = startIndex + i * numItemsPerThread;
            final int end;
            if (i == numThreads - 1) {
                end = endIndex;
            } else {
                end = (i + 1) * numItemsPerThread;
            }

            final int resultIndex = i;
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    results[resultIndex] = reduce(values, start, end, reducer, initial);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return reduce(results, 0, numThreads, reducer, initial);
    }
}
