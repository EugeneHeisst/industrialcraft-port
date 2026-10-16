package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.util.Keyboard;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Uses 50 EU per wrench damage point; lossless mode (mode switch + right click) always returns the block for 10000 EU. */
public class ItemToolWrenchElectric extends ItemToolWrench implements IElectricItem {
    public ItemToolWrenchElectric(Properties properties) { super(properties.stacksTo(1).setNoRepair()); }

    private static boolean lossless(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("losslessMode"); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) {
        String tip = ElectricItem.manager.getToolTip(stack);
        if (tip != null) list.add(Component.literal(tip).withStyle(ChatFormatting.GRAY));
        list.add(Component.translatable("ic2.tooltip.mode", Component.translatable(lossless(stack) ? "ic2.tooltip.mode.lossless" : "ic2.tooltip.mode.normal")).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && Keyboard.isModeSwitchKeyDown(player)) {
            boolean value = !lossless(stack);
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean("losslessMode", value));
            player.displayClientMessage(Component.literal(value ? "Lossless wrench mode enabled" : "Lossless wrench mode disabled"), false);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        return ctx.getPlayer() != null && Keyboard.isModeSwitchKeyDown(ctx.getPlayer()) ? InteractionResult.PASS : super.onItemUseFirst(stack, ctx);
    }

    @Override public boolean canTakeDamage(ItemStack stack, int amount) { return ElectricItem.manager.getCharge(stack) >= amount * 50; }
    @Override public void damage(ItemStack stack, int amount, Player player, UseOnContext ctx) { ElectricItem.manager.use(stack, 50 * amount, player); }
    @Override public boolean overrideWrenchSuccessRate(ItemStack stack) { return lossless(stack); }
    @Override public boolean canProvideEnergy(ItemStack stack) { return false; }
    @Override public double getMaxCharge(ItemStack stack) { return 12000; }
    @Override public int getTier(ItemStack stack) { return 1; }
    @Override public double getTransferLimit(ItemStack stack) { return 250; }
    @Override public boolean isBarVisible(ItemStack stack) { return true; }
    @Override public int getBarWidth(ItemStack stack) { return Math.round(13f * (float)Mth.clamp(ElectricItemManager.getRawCharge(stack) / 12000, 0, 1)); }
    @Override public int getBarColor(ItemStack stack) { return Mth.hsvToRgb((float)Mth.clamp(ElectricItemManager.getRawCharge(stack) / 12000, 0, 1) / 3f, 1, 1); }
}
