package ic2.core.world;

import ic2.core.block.rubber.BlockRubWood;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Rubber tree shape of the original WorldGenRubTree. */
public final class WorldGenRubTree {
    public static final int maxHeight = 8;
    private WorldGenRubTree() {}

    /** Several trees around (x, z) like generate(world, random, x, count, z). */
    public static void generate(WorldGenLevel level, RandomSource random, int x, int count, int z, int minX, int maxX, int minZ, int maxZ) {
        while (count > 0) {
            x = Math.max(minX, Math.min(maxX, x));
            z = Math.max(minZ, Math.min(maxZ, z));
            int y = level.getMaxBuildHeight() - 1;
            while (y > level.getMinBuildHeight() && level.isEmptyBlock(new BlockPos(x, y - 1, z))) y--;
            if (!grow(level, new BlockPos(x, y, z), random)) count -= 3;
            x += random.nextInt(15) - 7;
            z += random.nextInt(15) - 7;
            count--;
        }
    }

    public static boolean grow(WorldGenLevel level, BlockPos pos, RandomSource random) {
        BlockState wood = Ic2Blocks.RUBBER_WOOD.get().defaultBlockState();
        BlockState leaves = Ic2Blocks.RUBBER_LEAVES.get().defaultBlockState().setValue(LeavesBlock.DISTANCE, 1);
        int treeholechance = 25;
        int height = getGrowHeight(level, pos);
        if (height < 2) return false;
        height -= random.nextInt(height / 2 + 1);
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        for (int cHeight = 0; cHeight < height; cHeight++) {
            BlockState log = wood;
            if (random.nextInt(100) <= treeholechance) {
                treeholechance -= 10;
                log = wood.setValue(BlockRubWood.HOLE, BlockRubWood.Hole.wet).setValue(BlockRubWood.FACING, Direction.from2DDataValue(random.nextInt(4)));
            }
            level.setBlock(new BlockPos(x, y + cHeight, z), log, 3);
            if (height < 4 || height < 7 && cHeight > 1 || cHeight > 2) {
                for (int cx = x - 2; cx <= x + 2; cx++) for (int cz = z - 2; cz <= z + 2; cz++) {
                    int c = Math.max(1, cHeight + 4 - height);
                    boolean gen = cx > x - 2 && cx < x + 2 && cz > z - 2 && cz < z + 2
                        || cx > x - 2 && cx < x + 2 && random.nextInt(c) == 0
                        || cz > z - 2 && cz < z + 2 && random.nextInt(c) == 0;
                    BlockPos lp = new BlockPos(cx, y + cHeight, cz);
                    if (gen && level.isEmptyBlock(lp)) level.setBlock(lp, leaves, 3);
                }
            }
        }
        for (int i = 0; i <= height / 4 + random.nextInt(2); i++) {
            BlockPos lp = new BlockPos(x, y + height + i, z);
            if (level.isEmptyBlock(lp)) level.setBlock(lp, leaves, 3);
        }
        return true;
    }

    public static int getGrowHeight(WorldGenLevel level, BlockPos pos) {
        BlockState base = level.getBlockState(pos.below());
        BlockState at = level.getBlockState(pos);
        if (!base.is(BlockTags.DIRT) || !(at.isAir() || at.is(Ic2Blocks.RUBBER_SAPLING.get()))) return 0;
        int height = 1;
        BlockPos p = pos;
        while (level.isEmptyBlock(p.above()) && height < maxHeight) { p = p.above(); height++; }
        return height;
    }
}
