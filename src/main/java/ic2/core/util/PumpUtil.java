package ic2.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Walks up and along a liquid towards a source block (flow decay 0); stray flowing blocks around a dead end are drained. */
public final class PumpUtil {
    private PumpUtil() {}

    public static BlockPos searchFluidSource(Level level, BlockPos start) {
        int x = start.getX(), y = start.getY(), z = start.getZ();
        int cur = decay(level, x, y, z);
        for (int i = 0; i < 64; i++) {
            if (decay(level, x, y + 1, z) >= 0) cur = decay(level, x, ++y, z);
            else if (decay(level, x + 1, y + 1, z) >= 0) cur = decay(level, ++x, ++y, z);
            else if (decay(level, x - 1, y + 1, z) >= 0) cur = decay(level, --x, ++y, z);
            else if (decay(level, x, y + 1, z + 1) >= 0) cur = decay(level, x, ++y, ++z);
            else if (decay(level, x, y + 1, z - 1) >= 0) cur = decay(level, x, ++y, --z);
            else if (lower(level, x - 1, y, z, cur)) cur = decay(level, --x, y, z);
            else if (lower(level, x, y, z + 1, cur)) cur = decay(level, x, y, ++z);
            else if (lower(level, x, y, z - 1, cur)) cur = decay(level, x, y, --z);
            else if (lower(level, x + 1, y, z, cur)) cur = decay(level, ++x, y, z);
            else break;
        }
        int[][] seen = new int[65][3];
        for (int i = 0; i < 64; i++) {
            seen[i][0] = x; seen[i][1] = y; seen[i][2] = z;
            if (decay(level, x - 1, y, z) != -1 && !seen(x - 1, y, z, seen, i)) { if (decay(level, --x, y, z) == 0) return new BlockPos(x, y, z); }
            else if (decay(level, x, y, z + 1) != -1 && !seen(x, y, z + 1, seen, i)) { if (decay(level, x, y, ++z) == 0) return new BlockPos(x, y, z); }
            else if (decay(level, x, y, z - 1) != -1 && !seen(x, y, z - 1, seen, i)) { if (decay(level, x, y, --z) == 0) return new BlockPos(x, y, z); }
            else {
                if (decay(level, x + 1, y, z) == -1 || seen(x + 1, y, z, seen, i)) break;
                if (decay(level, ++x, y, z) == 0) return new BlockPos(x, y, z);
            }
        }
        for (int ix = 0; ix <= 4; ix++) for (int iz = 0; iz <= 4; iz++) {
            BlockPos p = new BlockPos(x + ix - 2, y, z + iz - 2);
            int d = decay(level, p.getX(), p.getY(), p.getZ());
            if (d == 0) return p;
            if (d >= 1 && d < 7) level.setBlock(p, level.getBlockState(p).setValue(LiquidBlock.LEVEL, d + 1), 3);
            else if (d >= 7) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        }
        return null;
    }

    private static boolean lower(Level level, int x, int y, int z, int cur) { int d = decay(level, x, y, z); return d < cur && d != -1; }

    /** Liquid level of a liquid block (0 = source), -1 otherwise. Falling liquid counts as 8 like the 1.7.10 metadata. */
    static int decay(Level level, int x, int y, int z) {
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        return state.getBlock() instanceof LiquidBlock ? state.getValue(LiquidBlock.LEVEL) : -1;
    }

    private static boolean seen(int x, int y, int z, int[][] arr, int end) {
        for (int i = 0; i <= end; i++) if (arr[i][0] == x && arr[i][1] == y && arr[i][2] == z) return true;
        return false;
    }
}
