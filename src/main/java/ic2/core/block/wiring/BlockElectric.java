package ic2.core.block.wiring;

import ic2.core.block.BlockTileEntity;
import ic2.core.block.TileEntityBlock;
import ic2.core.ref.Ic2Components;
import ic2.core.util.Util;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Storage blocks and transformers: front can face up/down, storage blocks emit redstone and drive comparators. */
public class BlockElectric extends BlockTileEntity {
    public BlockElectric(Properties properties, Supplier<? extends BlockEntityType<? extends TileEntityBlock>> type) { super(properties, type); }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = Direction.UP;
        if (ctx.getPlayer() != null) {
            int pitch = Math.round(ctx.getPlayer().getXRot());
            if (pitch >= 65) facing = Direction.UP;
            else if (pitch <= -65) facing = Direction.DOWN;
            else facing = ctx.getHorizontalDirection().getOpposite();
        }
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TileEntityElectricBlock te) te.energy = stack.getOrDefault(Ic2Components.ENERGY.get(), 0.0);
    }

    @Override protected boolean isSignalSource(BlockState state) { return true; }
    @Override protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) { return level.getBlockEntity(pos) instanceof TileEntityElectricBlock te && te.isEmittingRedstone() ? 15 : 0; }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof TileEntityElectricBlock te ? (int)Math.round(Util.map(te.energy, te.maxStorage, 15)) : 0;
    }
}
