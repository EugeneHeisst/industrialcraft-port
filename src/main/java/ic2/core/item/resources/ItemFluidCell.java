package ic2.core.item.resources;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

/** Universal fluid cell: holds up to 1000 mB of any fluid. */
public class ItemFluidCell extends Item {
    public ItemFluidCell(Properties properties) { super(properties.stacksTo(1)); }

    public static FluidHandlerItemStack handler(ItemStack stack) { return new FluidHandlerItemStack(ic2.core.ref.Ic2Components.FLUID, stack, 1000); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(stack);
        BlockPos pos = hit.getBlockPos();
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, hit.getDirection(), stack)) return InteractionResultHolder.fail(stack);
        var fluid = level.getFluidState(pos);
        if (!fluid.isSource() || handler(stack.copy()).fill(new FluidStack(fluid.getType(), 1000), FluidAction.SIMULATE) < 1000) return InteractionResultHolder.pass(stack);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        var result = FluidUtil.tryPickUpFluid(stack, player, level, pos, hit.getDirection());
        if (!result.isSuccess()) return InteractionResultHolder.pass(stack);
        if (!player.getAbilities().instabuild) player.setItemInHand(hand, result.getResult());
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    /** Run before the machine opens its GUI, so cells can fill and drain its tanks. */
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var player = context.getPlayer();
        var level = context.getLevel();
        var pos = context.getClickedPos();
        if (player == null || level.isClientSide || !level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, context.getClickedFace(), stack)) return InteractionResult.PASS;
        return FluidUtil.interactWithFluidHandler(player, context.getHand(), level, pos, context.getClickedFace())
            ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) {
        FluidStack fluid = stack.getOrDefault(ic2.core.ref.Ic2Components.FLUID.get(), SimpleFluidContent.EMPTY).copy();
        list.add(fluid.isEmpty() ? Component.translatable("ic2.item.FluidContainer.Empty").withStyle(ChatFormatting.GRAY) : Component.literal(fluid.getHoverName().getString() + ": " + fluid.getAmount() + " mB").withStyle(ChatFormatting.GRAY));
    }
}
