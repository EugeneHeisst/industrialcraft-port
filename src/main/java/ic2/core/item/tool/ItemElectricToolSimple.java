package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.core.ref.Ic2Blocks;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Electric hoe (tills for 50 EU) and electric treetap (taps resin for 50 EU). */
public class ItemElectricToolSimple extends ItemElectricTool {
    public enum Kind { hoe, treetap }
    private final Kind kind;

    public ItemElectricToolSimple(Properties properties, Kind kind) {
        super(properties, 50, HarvestLevel.Iron, kind == Kind.hoe ? EnumSet.of(ToolClass.Hoe) : EnumSet.noneOf(ToolClass.class));
        this.kind = kind;
        maxCharge = 10000;
        transferLimit = 100;
        tier = 1;
        efficiencyOnProperMaterial = 16;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        ItemStack stack = ctx.getItemInHand();
        if (!ElectricItem.manager.canUse(stack, operationEnergyCost)) return InteractionResult.PASS;
        BlockState state = level.getBlockState(pos);
        if (kind == Kind.treetap) {
            if (!state.is(Ic2Blocks.RUBBER_WOOD.get())) return InteractionResult.PASS;
            if (ItemTreetap.attemptExtract(ctx.getPlayer(), level, pos, ctx.getClickedFace(), null)) {
                if (!level.isClientSide) ElectricItem.manager.use(stack, operationEnergyCost, ctx.getPlayer());
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        }
        if (ctx.getClickedFace() != net.minecraft.core.Direction.DOWN && level.isEmptyBlock(pos.above()) && (state.is(Blocks.MYCELIUM) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT))) {
            // original: canPlayerEdit, then the cancellable UseHoeEvent. Modern equivalent: the native HOE_TILL tool-modification
            // event, fired once; a listener can cancel (protection) or replace the result. Without a change IC2 tills to farmland,
            // including mycelium, which vanilla's own tilling table does not cover.
            if (ctx.getPlayer() != null && !ctx.getPlayer().mayUseItemAt(pos, ctx.getClickedFace(), stack)) return InteractionResult.PASS;
            BlockState result = net.neoforged.neoforge.event.EventHooks.onToolUse(state, ctx, net.neoforged.neoforge.common.ItemAbilities.HOE_TILL, false);
            if (result == null) return InteractionResult.PASS;
            if (result == state) result = Blocks.FARMLAND.defaultBlockState();
            level.playSound(ctx.getPlayer(), pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1, 1);
            if (!level.isClientSide) {
                level.setBlock(pos, result, 11);
                ElectricItem.manager.use(stack, operationEnergyCost, ctx.getPlayer());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
