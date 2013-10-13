package mapreduce;

public class Map {
    public static final int NUM_ELEMENTS = Integer.MAX_VALUE - 3;
    public static final int NUM_THREADS = 4;

    public interface IMapInt {
        int doMap(int value);
    }

    public static void map(int[] values, int startIndex,
		int endIndex, IMapInt mapper) {
        for(int i = startIndex; i < endIndex; i++) {
            values[i] = mapper.doMap(values[i]);
        }
    }

    public static void main(String args[]) {
        int values[] = new int[NUM_ELEMENTS];
        for (int i = 0; i < NUM_ELEMENTS; i++) {
            values[i] = i + 1;
        }

        IMapInt gcd = new IMapInt() {
            @Override
            public int doMap(int a) {
		return a*2;
                /*int b = 7;
                while (b != 0) {
                    int t = b;
                    b = a % t;
                    a = t;
                }
                return a;*/
            }
        };

        long start = System.currentTimeMillis();
        pmap(values, 0, NUM_ELEMENTS, gcd, NUM_THREADS);
        System.out.println(
		String.format("Processed %d elements in %.3f seconds.",
		NUM_ELEMENTS,
		(double)(System.currentTimeMillis()-start)/1000)
	);
        System.out.println(String.format(
		"Heap size: %.2f GB",
		(float)Runtime.getRuntime().totalMemory()/1024/1024/1024
	));

        // Note: assertions are disabled by default
        for (int i = 0; i < NUM_ELEMENTS; i++) {
            assert values[i] == gcd.doMap(i+1);
        }
    }

    public static void pmap(final int[] values, final int startIndex,
		final int endIndex, final IMapInt mapper, int numThreads) {
	System.out.println(String.format("Running %d threads.", numThreads));
        Thread threads[] = new Thread[numThreads];
        final int numItemsPerThread = (endIndex - startIndex) / numThreads;
        for (int i = 0; i < numThreads; i++) {
            final int start = startIndex + i * numItemsPerThread;
            final int end;
            if (i == numThreads - 1) {
                end = endIndex;
            } else {
                end = (i + 1) * numItemsPerThread;
            }
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    map(values, start, end, mapper);
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
    }
}
