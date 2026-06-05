package lol.catgirl.utils.client;

import org.jetbrains.annotations.Range;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

public class MathUtils {
    private static final SecureRandom RAND = new SecureRandom();

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double random(double min, double max) {
        double dis = Math.abs(max - min);
        return dis == 0.0d ? min : min + RAND.nextDouble() * dis;
    }

    public static long randomLong(long min, long max) {
        if (max <= min) return min;

        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    public static boolean random() {
        return RAND.nextBoolean();
    }

    public static float randomFloat(float min, float max) {
        float dis = Math.abs(max - min);
        return dis == 0.0f ? min : min + RAND.nextFloat() * dis;
    }

    public static float nextFloat(float range) {
        return RAND.nextFloat() * range;
    }

    public static float nextFloat(float min, float max) {
        float dis = Math.abs(max - min);
        return dis == 0.0f ? min : min + RAND.nextFloat() * dis;
    }

    public static double nextDouble(double range) {
        return RAND.nextDouble() * range;
    }

    public static double nextDouble(double min, double max) {
        double dis = Math.abs(max - min);
        return dis == 0.0d ? min : min + RAND.nextDouble() * dis;
    }

    public static int randomInt(@Range(from = 1, to = Integer.MAX_VALUE) int b) {
        return RAND.nextInt(b);
    }

    public static int randomInt(int min, int max) {
        if (max < min) {
            throw new IllegalArgumentException("max must be >= min");
        }
        return min + RAND.nextInt(max - min + 1);
    }
}
