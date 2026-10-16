package ic2.core.util;

public final class Util {
    private Util() {}

    public static double map(double value, double srcMax, double dstMax) {
        if (value < 0 || Double.isNaN(value)) value = 0;
        if (value > srcMax) value = srcMax;
        return value / srcMax * dstMax;
    }

    public static double lerp(double start, double end, double fraction) { return start + (end - start) * fraction; }
    public static boolean isSimilar(double a, double b) { return Math.abs(a - b) < 1e-6; }
    public static int limit(int value, int min, int max) { return value <= min ? min : Math.min(value, max); }
    public static double limit(double value, double min, double max) { return Double.isNaN(value) || value <= min ? min : Math.min(value, max); }

    /** Number with SI prefix and the given significant digits, e.g. "1.2k ". */
    public static String toSiString(double value, int digits) {
        if (value == 0) return "0 ";
        if (Double.isNaN(value)) return "NaN ";
        String ret = "";
        if (value < 0) { ret = "-"; value = -value; }
        if (Double.isInfinite(value)) return ret + "∞ ";
        double log = Math.log10(value), mul;
        String si;
        if (log >= 0) {
            int reduce = (int)Math.floor(log / 3);
            mul = 1 / Math.pow(10, reduce * 3);
            si = switch (reduce) { case 0 -> ""; case 1 -> "k"; case 2 -> "M"; case 3 -> "G"; case 4 -> "T"; case 5 -> "P"; case 6 -> "E"; case 7 -> "Z"; case 8 -> "Y"; default -> "E" + reduce * 3; };
        } else {
            int expand = (int)Math.ceil(-log / 3);
            mul = Math.pow(10, expand * 3);
            si = switch (expand) { case 0 -> ""; case 1 -> "m"; case 2 -> "µ"; case 3 -> "n"; case 4 -> "p"; case 5 -> "f"; case 6 -> "a"; case 7 -> "z"; case 8 -> "y"; default -> "E-" + expand * 3; };
        }
        value *= mul;
        int iVal = (int)Math.floor(value);
        value -= iVal;
        int iDigits = 1;
        if (iVal > 0) iDigits += (int)Math.floor(Math.log10(iVal));
        mul = Math.pow(10, digits - iDigits);
        int dVal = (int)Math.round(value * mul);
        if (dVal >= mul) {
            iVal++;
            dVal -= (int)mul;
            iDigits = 1;
            if (iVal > 0) iDigits += (int)Math.floor(Math.log10(iVal));
        }
        ret += Integer.toString(iVal);
        if (digits > iDigits && dVal != 0) ret += String.format(".%0" + (digits - iDigits) + "d", dVal);
        ret = ret.replaceFirst("(\\.\\d*?)0+$", "$1");
        return ret + " " + si;
    }
    /** Block entity at pos, or null when its chunk is not loaded (never loads chunks from a neighbour lookup). */
    public static net.minecraft.world.level.block.entity.BlockEntity loadedBlockEntity(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        return level != null && level.isLoaded(pos) ? level.getBlockEntity(pos) : null;
    }
}
