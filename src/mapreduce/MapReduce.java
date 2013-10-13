package mapreduce;

public class MapReduce {
    public static final int NUM_ELEMENTS = Integer.MAX_VALUE - 3;
    public static final int NUM_THREADS = 8;

    public static void main(String args[]) {
        int values[] = new int[NUM_ELEMENTS];
        for (int i = 0; i < NUM_ELEMENTS; i++) {
            values[i] = i + 1;
        }

        Map.IMapInt gcd = new Map.IMapInt() {
            @Override
            public int doMap(int a) {
                int b = 16;
                while (b != 0) {
                    int t = b;
                    b = a % t;
                    a = t;
                }
                return a;
            }
        };

        Reduce.IReduceInt max = new Reduce.IReduceInt() {
            @Override
            public int doReduce(int a, int b) {
                // A simple max is too fast to show scaling with > 4 threads.
                long wasteSomeTime = a + b + b;
                int aAgain = (int) (wasteSomeTime - b - b);
                return Math.max(aAgain, b);
            }
        };

        long startMap = System.currentTimeMillis();
        Map.pmap(values, 0, NUM_ELEMENTS, gcd, NUM_THREADS);
        System.out.println(String.format("Mapped %d elements in %.3f seconds.", NUM_ELEMENTS, (double) (System.currentTimeMillis() - startMap) / 1000));
        long startReduce = System.currentTimeMillis();
        int result = Reduce.preduce(values, 0, NUM_ELEMENTS, max, 0, NUM_THREADS);
        System.out.println(String.format("Reduced %d elements in %.3f seconds.", NUM_ELEMENTS, (double) (System.currentTimeMillis() - startReduce) / 1000));
        System.out.println(String.format("Processed %d elements in %.3f seconds.", NUM_ELEMENTS, (double) (System.currentTimeMillis() - startMap) / 1000));

        System.out.println(String.format("Heap size: %.2f GB", (float) Runtime.getRuntime().totalMemory() / 1024 / 1024 / 1024));

        System.out.println(result);
    }
}
