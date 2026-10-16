package ic2.core.item.tfbp;

import ic2.api.item.ITerraformingBP;
import ic2.core.ref.Ic2Blocks;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

/** Terraformer blueprints (TFBP) of the original: chilling, cultivation, desertification, flatification, irrigation, mushroom. */
public class ItemTFBP extends Item implements ITerraformingBP {
    public enum Kind { CHILLING(2000, 50), CULTIVATION(4000, 40), DESERTIFICATION(2500, 40), FLATIFICATION(4000, 40), IRRIGATION(3000, 60), MUSHROOM(8000, 25);
        final int consume, range; Kind(int c, int r) { consume = c; range = r; } }

    private final Kind kind;
    public ItemTFBP(Properties p, Kind kind) { super(p.stacksTo(1)); this.kind = kind; }
    @Override public int getConsume() { return kind.consume; }
    @Override public int getRange() { return kind.range; }

    // ---- helpers of TileEntityTerra
    public static int firstSolid(Level l, int x, int z, int y) {
        for (; y > l.getMinBuildHeight(); y--) { BlockPos p = new BlockPos(x, y, z); if (l.getBlockState(p).isRedstoneConductor(l, p)) return y; }
        return Integer.MIN_VALUE;
    }
    public static int firstBlock(Level l, int x, int z, int y) {
        for (; y > l.getMinBuildHeight(); y--) if (!l.getBlockState(new BlockPos(x, y, z)).isAir()) return y;
        return Integer.MIN_VALUE;
    }
    static boolean is(BlockState s, Block... blocks) { for (Block b : blocks) if (s.is(b)) return true; return false; }

    /** Replaces from with to: upwards adds one layer on top of a from column; downwards converts the first from below the to layer. */
    public static boolean switchGround(Level l, Block from, Block to, int x, int y, int z, boolean upwards) {
        if (upwards) {
            int saveY = ++y;
            while (l.getBlockState(new BlockPos(x, y - 1, z)).is(from)) y--;
            if (saveY == y) return false;
            l.setBlock(new BlockPos(x, y, z), to.defaultBlockState(), 3);
            return true;
        }
        while (l.getBlockState(new BlockPos(x, y, z)).is(to)) y--;
        if (!l.getBlockState(new BlockPos(x, y, z)).is(from)) return false;
        l.setBlock(new BlockPos(x, y, z), to.defaultBlockState(), 3);
        return true;
    }

    /** Sets the biome of the 4x4 column containing x, z and resends it to players. */
    public static void setBiomeAt(Level l, int x, int z, ResourceKey<Biome> key) {
        if (!(l instanceof ServerLevel sl)) return;
        Holder<Biome> biome = sl.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(key);
        LevelChunk chunk = sl.getChunkAt(new BlockPos(x, 0, z));
        int qx = (x & 15) >> 2, qz = (z & 15) >> 2;
        chunk.fillBiomesFromNoise((bx, by, bz, sampler) -> (bx & 3) == qx && (bz & 3) == qz ? biome : chunk.getNoiseBiome(bx & 3, by, bz & 3), sl.getChunkSource().randomState().sampler());
        chunk.setUnsaved(true);
        sl.getChunkSource().chunkMap.resendBiomesForChunks(List.of(chunk));
    }

    static final Set<Block> PLANTS = Set.of(Blocks.SHORT_GRASS, Blocks.POPPY, Blocks.DANDELION, Blocks.OAK_SAPLING, Blocks.WHEAT, Blocks.RED_MUSHROOM_BLOCK, Blocks.BROWN_MUSHROOM, Blocks.PUMPKIN);

    @Override
    public boolean terraform(Level l, int x, int z, int yCoord) {
        return switch (kind) {
            case CHILLING -> chilling(l, x, z, yCoord);
            case CULTIVATION -> cultivation(l, x, z, yCoord);
            case DESERTIFICATION -> desertification(l, x, z, yCoord);
            case FLATIFICATION -> flatification(l, x, z, yCoord);
            case IRRIGATION -> irrigation(l, x, z, yCoord);
            case MUSHROOM -> mushroom(l, x, z, yCoord);
        };
    }

    private boolean chilling(Level l, int x, int z, int yc) {
        int y = firstBlock(l, x, z, yc + 10);
        if (y == Integer.MIN_VALUE) return false;
        BlockPos p = new BlockPos(x, y, z);
        BlockState s = l.getBlockState(p);
        if (s.is(Blocks.WATER)) { l.setBlock(p, Blocks.ICE.defaultBlockState(), 3); return true; }
        if (s.is(Blocks.ICE) && l.getBlockState(p.below()).is(Blocks.WATER)) { l.setBlock(p.below(), Blocks.ICE.defaultBlockState(), 3); return true; }
        if (s.is(Blocks.SNOW) && surroundedBySnow(l, x, y, z)) { l.setBlock(p, Blocks.SNOW_BLOCK.defaultBlockState(), 3); return true; }
        if (!Blocks.SNOW.defaultBlockState().canSurvive(l, p.above()) && !s.is(Blocks.ICE) || !l.getBlockState(p.above()).isAir()) return false;
        l.setBlock(p.above(), Blocks.SNOW.defaultBlockState(), 3);
        return true;
    }
    private boolean surroundedBySnow(Level l, int x, int y, int z) { return snowHere(l, x + 1, y, z) && snowHere(l, x - 1, y, z) && snowHere(l, x, y, z + 1) && snowHere(l, x, y, z - 1); }
    private boolean snowHere(Level l, int x, int y, int z) {
        int yy = firstBlock(l, x, z, y + 16);
        if (yy == Integer.MIN_VALUE) return false;
        BlockPos p = new BlockPos(x, yy, z);
        BlockState s = l.getBlockState(p);
        if (s.is(Blocks.SNOW) || s.is(Blocks.SNOW_BLOCK)) return true;
        if ((Blocks.SNOW.defaultBlockState().canSurvive(l, p.above()) || s.is(Blocks.ICE)) && l.getBlockState(p.above()).isAir()) l.setBlock(p.above(), Blocks.SNOW.defaultBlockState(), 3);
        return false;
    }

    private boolean cultivation(Level l, int x, int z, int yc) {
        int y = firstSolid(l, x, z, yc + 10);
        if (y == Integer.MIN_VALUE) return false;
        if (switchGround(l, Blocks.SAND, Blocks.DIRT, x, y, z, true)) return true;
        BlockPos p = new BlockPos(x, y, z);
        BlockState s = l.getBlockState(p);
        if (s.is(Blocks.DIRT)) { l.setBlock(p, Blocks.GRASS_BLOCK.defaultBlockState(), 3); return true; }
        if (!s.is(Blocks.GRASS_BLOCK)) return false;
        BlockPos up = p.above();
        BlockState above = l.getBlockState(up);
        if (!above.isAir() && !(above.is(Blocks.SHORT_GRASS) && l.random.nextInt(4) == 0)) return false;
        Block plant = Blocks.SHORT_GRASS;
        Block[] order = {Blocks.SHORT_GRASS, Blocks.POPPY, Blocks.DANDELION, Blocks.OAK_SAPLING, Blocks.WHEAT, Blocks.RED_MUSHROOM_BLOCK, Blocks.BROWN_MUSHROOM, Blocks.PUMPKIN, Ic2Blocks.RUBBER_SAPLING.get()};
        for (Block b : order) if (l.random.nextInt(5) <= 1) { plant = b; break; }
        if (plant == Blocks.WHEAT) l.setBlock(p, Blocks.FARMLAND.defaultBlockState(), 3);
        l.setBlock(up, plant.defaultBlockState(), 3);
        return true;
    }

    private boolean desertification(Level l, int x, int z, int yc) {
        int y = firstBlock(l, x, z, yc + 10);
        if (y == Integer.MIN_VALUE) return false;
        if (switchGround(l, Blocks.DIRT, Blocks.SAND, x, y, z, false) || switchGround(l, Blocks.GRASS_BLOCK, Blocks.SAND, x, y, z, false) || switchGround(l, Blocks.FARMLAND, Blocks.SAND, x, y, z, false)) {
            switchGround(l, Blocks.DIRT, Blocks.SAND, x, y, z, false);
            return true;
        }
        BlockPos p = new BlockPos(x, y, z);
        BlockState s = l.getBlockState(p);
        if (s.is(Blocks.WATER) || s.is(Blocks.SNOW) || s.is(BlockTags.LEAVES) || PLANTS.contains(s.getBlock()) || s.is(Ic2Blocks.RUBBER_SAPLING.get())) { l.removeBlock(p, false); return true; }
        if (s.is(Blocks.ICE) || s.is(Blocks.SNOW_BLOCK)) { l.setBlock(p, Blocks.WATER.defaultBlockState(), 3); return true; }
        if ((s.is(BlockTags.PLANKS) || s.is(BlockTags.LOGS)) && l.random.nextInt(15) == 0) { l.setBlock(p, Blocks.FIRE.defaultBlockState(), 3); return true; }
        return false;
    }

    private static boolean removable(BlockState s) {
        return s.is(Blocks.SNOW_BLOCK) || s.is(Blocks.ICE) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.STONE) || s.is(Blocks.GRAVEL) || s.is(Blocks.SAND) || s.is(Blocks.DIRT)
            || s.is(BlockTags.LEAVES) || s.is(BlockTags.LOGS) || PLANTS.contains(s.getBlock()) || s.is(Ic2Blocks.RUBBER_SAPLING.get());
    }
    private boolean flatification(Level l, int x, int z, int yc) {
        int y = firstBlock(l, x, z, yc + 20);
        if (y == Integer.MIN_VALUE) return false;
        if (l.getBlockState(new BlockPos(x, y, z)).is(Blocks.SNOW)) y--;
        if (y == yc) return false;
        if (y < yc) { l.setBlock(new BlockPos(x, y + 1, z), Blocks.DIRT.defaultBlockState(), 3); return true; }
        BlockPos p = new BlockPos(x, y, z);
        if (!removable(l.getBlockState(p))) return false;
        l.removeBlock(p, false);
        return true;
    }

    private boolean irrigation(Level l, int x, int z, int yc) {
        if (l.random.nextInt(48000) == 0 && l instanceof ServerLevel sl) { sl.setWeatherParameters(0, 6000, true, false); return true; }
        int y = firstBlock(l, x, z, yc + 10);
        if (y == Integer.MIN_VALUE) return false;
        if (switchGround(l, Blocks.SAND, Blocks.DIRT, x, y, z, true)) { switchGround(l, Blocks.SAND, Blocks.DIRT, x, y, z, true); return true; }
        BlockPos p = new BlockPos(x, y, z);
        BlockState s = l.getBlockState(p);
        if (s.is(Blocks.SHORT_GRASS)) return spreadGrass(l, x + 1, y, z) || spreadGrass(l, x - 1, y, z) || spreadGrass(l, x, y, z + 1) || spreadGrass(l, x, y, z - 1);
        if (s.getBlock() instanceof SaplingBlock sap && l instanceof ServerLevel sl) { sap.advanceTree(sl, p, s.setValue(SaplingBlock.STAGE, 1), sl.random); return true; }
        if (s.is(Ic2Blocks.RUBBER_SAPLING.get()) && l instanceof ServerLevel sl && s.getBlock() instanceof BonemealableBlock b) { b.performBonemeal(sl, sl.random, p, s); return true; }
        if (s.is(BlockTags.LOGS)) {
            l.setBlock(p.above(), s, 3);
            Block leaves = leavesFor(s.getBlock());
            for (BlockPos q : new BlockPos[]{p.above(2), p.above().east(), p.above().west(), p.above().south(), p.above().north()}) if (l.getBlockState(q).isAir()) l.setBlock(q, leaves.defaultBlockState(), 3);
            return true;
        }
        if (s.getBlock() instanceof CropBlock crop) { l.setBlock(p, crop.getStateForAge(crop.getMaxAge()), 3); return true; }
        if (s.is(Blocks.FIRE)) { l.removeBlock(p, false); return true; }
        return false;
    }
    private static Block leavesFor(Block log) {
        if (log == Blocks.SPRUCE_LOG) return Blocks.SPRUCE_LEAVES; if (log == Blocks.BIRCH_LOG) return Blocks.BIRCH_LEAVES; if (log == Blocks.JUNGLE_LOG) return Blocks.JUNGLE_LEAVES;
        if (log == Blocks.ACACIA_LOG) return Blocks.ACACIA_LEAVES; if (log == Blocks.DARK_OAK_LOG) return Blocks.DARK_OAK_LEAVES; if (log == Ic2Blocks.RUBBER_WOOD.get()) return Ic2Blocks.RUBBER_LEAVES.get();
        return Blocks.OAK_LEAVES;
    }
    private boolean spreadGrass(Level l, int x, int y, int z) {
        if (l.random.nextBoolean()) return false;
        y = firstBlock(l, x, z, y);
        if (y == Integer.MIN_VALUE) return false;
        BlockPos p = new BlockPos(x, y, z);
        BlockState s = l.getBlockState(p);
        if (s.is(Blocks.DIRT)) { l.setBlock(p, Blocks.GRASS_BLOCK.defaultBlockState(), 3); return true; }
        if (s.is(Blocks.GRASS_BLOCK) && l.getBlockState(p.above()).isAir()) { l.setBlock(p.above(), Blocks.SHORT_GRASS.defaultBlockState(), 3); return true; }
        return false;
    }

    private boolean mushroom(Level l, int x, int z, int yc) {
        int y = firstSolid(l, x, z, yc + 20);
        return y != Integer.MIN_VALUE && grow(l, x, y, z, Blocks.BROWN_MUSHROOM_BLOCK, Blocks.BROWN_MUSHROOM);
    }
    private boolean grow(Level l, int x, int y, int z, Block target, Block dependency) {
        if (dependency != null) for (int xm = x - 1; xm < x + 1; xm++) for (int zm = z - 1; zm < z + 1; zm++) for (int ym = y + 5; ym > y - 2; ym--) {
            BlockState s = l.getBlockState(new BlockPos(xm, ym, zm));
            if (dependency == Blocks.MYCELIUM) {
                if (s.is(Blocks.MYCELIUM) || s.is(Blocks.BROWN_MUSHROOM_BLOCK) || s.is(Blocks.RED_MUSHROOM_BLOCK)) break;
                if (!s.isAir() && (s.is(Blocks.DIRT) || s.is(Blocks.GRASS_BLOCK))) {
                    l.setBlock(new BlockPos(xm, ym, zm), Blocks.MYCELIUM.defaultBlockState(), 3);
                    setBiomeAt(l, x, z, Biomes.MUSHROOM_FIELDS);
                    return true;
                }
            } else if (dependency == Blocks.BROWN_MUSHROOM) {
                if (s.is(Blocks.BROWN_MUSHROOM) || s.is(Blocks.RED_MUSHROOM)) break;
                if (!s.isAir() && grow(l, xm, ym, zm, Blocks.BROWN_MUSHROOM, Blocks.MYCELIUM)) return true;
            }
        }
        BlockPos base = new BlockPos(x, y, z);
        if (target == Blocks.BROWN_MUSHROOM) {
            BlockState b = l.getBlockState(base);
            if (!b.is(Blocks.MYCELIUM)) {
                if (!b.is(Blocks.BROWN_MUSHROOM_BLOCK) && !b.is(Blocks.RED_MUSHROOM_BLOCK)) return false;
                l.setBlock(base, Blocks.MYCELIUM.defaultBlockState(), 3);
            }
            BlockState above = l.getBlockState(base.above());
            if (!above.isAir() && !above.is(Blocks.SHORT_GRASS)) return false;
            l.setBlock(base.above(), (l.random.nextBoolean() ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM).defaultBlockState(), 3);
            return true;
        }
        if (target == Blocks.BROWN_MUSHROOM_BLOCK && l instanceof ServerLevel sl) {
            BlockState b = l.getBlockState(base.above());
            if (b.getBlock() instanceof net.minecraft.world.level.block.MushroomBlock m && m.growMushroom(sl, base.above(), b, sl.random)) {
                for (int xm = x - 1; xm < x + 1; xm++) for (int zm = z - 1; zm < z + 1; zm++) {
                    BlockPos q = new BlockPos(xm, y + 1, zm);
                    if (l.getBlockState(q).is(Blocks.BROWN_MUSHROOM) || l.getBlockState(q).is(Blocks.RED_MUSHROOM)) l.removeBlock(q, false);
                }
                return true;
            }
        }
        return false;
    }
}
