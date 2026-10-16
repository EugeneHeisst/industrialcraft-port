package ic2.core.block;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Per side look of an obscured block (the original's retextureRef / retextureRefMeta / retextureRefSide). */
public final class Retexture {
    private BlockState[] ref;
    private Direction[] refSide;

    public boolean isEmpty() { return ref == null; }
    public BlockState state(Direction side) { return ref == null ? null : ref[side.get3DDataValue()]; }
    public Direction refSide(Direction side) { return refSide == null ? side : refSide[side.get3DDataValue()]; }
    public void clear() { ref = null; refSide = null; }

    /** @return whether anything changed */
    public boolean set(Direction side, BlockState state, Direction referencedSide) {
        boolean all = ref == null;
        if (all) { ref = new BlockState[6]; refSide = new Direction[6]; java.util.Arrays.fill(refSide, Direction.NORTH); }
        int i = side.get3DDataValue();
        boolean changed = all || ref[i] != state || refSide[i] != referencedSide;
        ref[i] = state;
        refSide[i] = referencedSide;
        return changed;
    }

    public void write(CompoundTag nbt) {
        if (ref == null) return;
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < 6; i++) {
            if (ref[i] == null) continue;
            tag.put("ref" + i, NbtUtils.writeBlockState(ref[i]));
            tag.putByte("side" + i, (byte)refSide[i].get3DDataValue());
        }
        nbt.put("retexture", tag);
    }

    public void read(CompoundTag nbt, Level level) {
        clear();
        if (!nbt.contains("retexture")) return;
        CompoundTag tag = nbt.getCompound("retexture");
        HolderGetter<Block> blocks = level != null ? level.holderLookup(Registries.BLOCK) : net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup();
        for (int i = 0; i < 6; i++) {
            if (!tag.contains("ref" + i)) continue;
            BlockState s = NbtUtils.readBlockState(blocks, tag.getCompound("ref" + i));
            if (s.isAir()) continue;
            set(Direction.from3DDataValue(i), s, Direction.from3DDataValue(tag.getByte("side" + i)));
        }
    }

    /** Full opaque model blocks only, like the original's renderAsNormalBlock check. */
    public static boolean isSuitable(BlockState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos) {
        return !state.isAir() && state.getRenderShape() == net.minecraft.world.level.block.RenderShape.MODEL && state.isSolidRender(level, pos) && !state.hasBlockEntity();
    }
}
