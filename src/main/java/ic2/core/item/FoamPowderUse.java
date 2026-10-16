package ic2.core.item;

import ic2.core.IC2;
import ic2.core.fluid.Ic2Fluids;
import ic2.core.ref.Ic2Items;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** CF powder (ItemFoamPowder): right click a water source to turn it into construction foam. */
@EventBusSubscriber(modid = IC2.MODID)
public final class FoamPowderUse {
    private FoamPowderUse() {}

    @SubscribeEvent
    static void use(PlayerInteractEvent.RightClickItem event) {
        var stack = event.getItemStack();
        if (!stack.is(Ic2Items.get("cf_powder"))) return;
        var player = event.getEntity();
        var level = event.getLevel();
        var from = player.getEyePosition();
        BlockHitResult hit = level.clip(new ClipContext(from, from.add(player.getLookAngle().scale(player.blockInteractionRange())), ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
        if (hit.getType() != HitResult.Type.BLOCK || !level.getBlockState(hit.getBlockPos()).is(Blocks.WATER) || !level.mayInteract(player, hit.getBlockPos())) return;
        if (!level.isClientSide) {
            level.setBlock(hit.getBlockPos(), Ic2Fluids.CONSTRUCTION_FOAM.block.get().defaultBlockState(), 3);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
    }
}
