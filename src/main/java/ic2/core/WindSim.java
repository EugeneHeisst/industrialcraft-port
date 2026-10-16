package ic2.core;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Per-level wind: strength random-walks between ~10 and ~20, scaled by a height profile fitted for the original 256 high
 * world with sea level 63 (1 at mid height, 0 at 9/8 of the height), x1.25 rain, x1.5 thunder, x2.4 overall.
 */
public final class WindSim {
    private static final Map<Level, WindSim> sims = new WeakHashMap<>();
    private static final RandomSource random = RandomSource.create();
    private int windStrength = 10 + random.nextInt(10);
    private int windDirection = random.nextInt(360);
    public int windTicker;
    private final Level level;
    private final double[] coefficients;

    private WindSim(Level level) {
        this.level = level;
        double sh = 63 + (256 - 63) / 2, fh = 256 * 9 / 8;
        double[][] a = {{sh * sh, sh * sh * sh, sh * sh * sh * sh}, {fh * fh, fh * fh * fh, fh * fh * fh * fh}, {2 * sh, 3 * sh * sh, 4 * sh * sh * sh}};
        double[] b = {1, 0, 0};
        coefficients = solve3(a, b);
    }

    public static WindSim get(Level level) { return sims.computeIfAbsent(level, WindSim::new); }

    private static double[] solve3(double[][] a, double[] b) {
        int n = 3;
        double[][] m = new double[n][n + 1];
        for (int i = 0; i < n; i++) { System.arraycopy(a[i], 0, m[i], 0, n); m[i][n] = b[i]; }
        for (int k = 0; k < n; k++) {
            int max = k;
            for (int i = k + 1; i < n; i++) if (Math.abs(m[i][k]) > Math.abs(m[max][k])) max = i;
            var t = m[k]; m[k] = m[max]; m[max] = t;
            for (int i = k + 1; i < n; i++) { double f = m[i][k] / m[k][k]; for (int j = k; j <= n; j++) m[i][j] -= f * m[k][j]; }
        }
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) { double s = m[i][n]; for (int j = i + 1; j < n; j++) s -= m[i][j] * x[j]; x[i] = s / m[i][i]; }
        return x;
    }

    public void updateWind() {
        if (windTicker++ % 128 != 0) return;
        int upChance = 10, downChance = 10;
        if (windStrength > 20) upChance -= windStrength - 20;
        else if (windStrength < 10) downChance -= 10 - windStrength;
        if (random.nextInt(100) < upChance) windStrength++;
        else if (random.nextInt(100) < downChance) windStrength--;
        switch (random.nextInt(3)) {
            case 0 -> windDirection = changeWindDirection(-18);
            case 2 -> windDirection = changeWindDirection(18);
            default -> {}
        }
    }

    public double getWindAt(double height) {
        double ret = windStrength * (height * height * coefficients[0] + height * height * height * coefficients[1] + height * height * height * height * coefficients[2]);
        if (level.isThundering()) ret *= 1.5;
        else if (level.isRaining()) ret *= 1.25;
        return ret * 2.4;
    }

    private int changeWindDirection(int amount) {
        windDirection += amount;
        return windDirection < 0 ? 359 - windDirection : windDirection > 359 ? windDirection - 359 : windDirection;
    }

    public static final class Ticker {
        @SubscribeEvent public void onLevelTick(LevelTickEvent.Post event) { if (!event.getLevel().isClientSide) get(event.getLevel()).updateWind(); }
    }
}
