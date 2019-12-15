//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public class Test {
    static int global_a;
    static int[] global_arr;

    public Test() {
    }

    public static int add(int var0, int var1) {
        int var2 = var0 + var1;
        return var2;
    }

    public static void main(String[] var0) {
        global_a = 1;
        global_arr = new int[3];
        int var1 = 25;
        byte var2 = 0;
        global_arr[2] = 2;

        while(true) {
            byte var32;
            label36: {
                int var10000 = (var1 >= 10 ? 1 : 0) != 0 ? 0 : 1;
                if (var2 != 0) {
                    if (var10000 != 0) {
                        var32 = 1;
                        break label36;
                    }
                }

                var32 = 0;
            }

            if (var32 != 0) {
                System.out.println(add(1, var1));
                return;
            }

            --var1;
            if ((var1 < 10 ? 1 : 0) == 0) {
                System.out.println(var1);
                var2 = 1;
            } else {
                System.out.println(-1);
            }
        }
    }
}
