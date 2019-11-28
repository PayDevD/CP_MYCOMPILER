public class Test {
    public static void main(String[] args) {
        for(int i = 0; i < 5; i ++)
            print10times();
    }

    static void print10times() {
        printer10 pp = new printer10();
        pp.run();
    }

    static class printer10 implements Runnable {

        @Override
        public void run() {
            for(int i = 0; i < 10; i++) {
                System.out.println(i);
            }
        }
    }
}
