package ic2.core.item.tool;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Forge hammer / cutter: stays in the crafting grid, losing one use per craft (getContainerItem of the original). */
public class ItemCraftingTool extends Item {
    public ItemCraftingTool(Properties properties) { super(properties.setNoRepair()); }

    @Override public boolean hasCraftingRemainingItem(ItemStack stack) { return true; }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack ret = stack.copy();
        ret.setDamageValue(ret.getDamageValue() + 1);
        return ret.getDamageValue() >= ret.getMaxDamage() ? ItemStack.EMPTY : ret;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) {
        list.add(Component.literal(Component.translatable("ic2.item.ItemTool.tooltip.UsesLeft").getString() + " " + (stack.getMaxDamage() - stack.getDamageValue())).withStyle(ChatFormatting.GRAY));
    }
}
