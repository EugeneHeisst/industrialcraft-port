package ic2.core.item;

import ic2.core.IC2;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Sticky resin: right click a piston face to make it sticky, or the top of a solid block to lay a resin sheet. */
@EventBusSubscriber(modid = IC2.MODID)
public final class ResinUse {
    private ResinUse() {}

    @SubscribeEvent
    static void use(PlayerInteractEvent.RightClickBlock event) {
        var stack = event.getItemStack();
        if (!stack.is(Ic2Items.get("resin"))) return;
        var level = event.getLevel();
        BlockPos pos = event.getPos();
        Direction side = event.getFace();
        var state = level.getBlockState(pos);
        boolean done = false;
        if (state.is(Blocks.PISTON) && state.getValue(PistonBaseBlock.FACING) == side && !state.getValue(PistonBaseBlock.EXTENDED)) {
            if (!level.isClientSide) level.setBlock(pos, Blocks.STICKY_PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, side), 3);
            done = true;
        } else if (side == Direction.UP && level.getBlockState(pos.above()).isAir() && Ic2Blocks.RESIN_SHEET.get().defaultBlockState().canSurvive(level, pos.above())) {
            if (!level.isClientSide) level.setBlock(pos.above(), Ic2Blocks.RESIN_SHEET.get().defaultBlockState(), 3);
            done = true;
        }
        if (!done) return;
        if (!event.getEntity().getAbilities().instabuild && !level.isClientSide) stack.shrink(1);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
    }
}
