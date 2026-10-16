package ic2.core.block.personal;

import ic2.core.block.BlockTileEntity;
import ic2.core.block.TileEntityBlock;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Personal safe, trade-o-mat and energy-o-mat: unbreakable and blast proof, removed only with the owner's wrench. */
public class BlockPersonal extends BlockTileEntity {
    private final boolean safe;

    public BlockPersonal(Properties properties, Supplier<? extends BlockEntityType<? extends TileEntityBlock>> type, boolean safe) {
        super(properties, type);
        this.safe = safe;
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return safe ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.MODEL; }

    /** The placer owns the block at once instead of whoever opens it first. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player && level.getBlockEntity(pos) instanceof IPersonalBlock personal) personal.permitsAccess(player.getGameProfile());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (safe && !player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof TileEntityPersonalChest chest && !level.isClientSide && !chest.permitsAccess(player.getGameProfile())) {
            player.displayClientMessage(chest.ownerMessage(), false);
            return InteractionResult.FAIL;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        BlockEntity be = level.getBlockEntity(pos);
        return be != null && be.triggerEvent(id, param);
    }

    @Override public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) { return false; }
}
