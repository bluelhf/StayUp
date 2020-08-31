package io.github.bluelhf.stayup.util;

public class Timing {
    public static long time(Runnable r) {
        long start = System.currentTimeMillis();
        r.run();
        return System.currentTimeMillis() - start;
    }
}
