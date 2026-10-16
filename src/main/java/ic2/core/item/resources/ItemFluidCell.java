package ic2.core.item.resources;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
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
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) {
        FluidStack fluid = stack.getOrDefault(ic2.core.ref.Ic2Components.FLUID.get(), SimpleFluidContent.EMPTY).copy();
        list.add(fluid.isEmpty() ? Component.translatable("ic2.item.FluidContainer.Empty").withStyle(ChatFormatting.GRAY) : Component.literal(fluid.getHoverName().getString() + ": " + fluid.getAmount() + " mB").withStyle(ChatFormatting.GRAY));
    }
}
