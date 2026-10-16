package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Coolant cell: stores heat up to its capacity and melts above it. */
public class ItemReactorHeatStorage extends ItemGradualInt implements ReactorComponent {
    public ItemReactorHeatStorage(Properties p, int heatStorage) { super(p, heatStorage); }

    @Override public boolean canStoreHeat(IReactor r, ItemStack s, int x, int y) { return true; }
    @Override public int getMaxHeat(IReactor r, ItemStack s, int x, int y) { return getMaxCustomDamage(s); }
    @Override public int getCurrentHeat(IReactor r, ItemStack s, int x, int y) { return getCustomDamage(s); }

    @Override
    public int alterHeat(IReactor reactor, ItemStack stack, int x, int y, int heat) {
        int myHeat = getCurrentHeat(reactor, stack, x, y) + heat, max = getMaxCustomDamage(stack);
        if (myHeat > max) { reactor.setItemAt(x, y, ItemStack.EMPTY); return max - myHeat + 1; }
        if (myHeat < 0) { heat = myHeat; myHeat = 0; } else heat = 0;
        setCustomDamage(stack, myHeat);
        return heat;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) {
        if (getCustomDamage(stack) > 0) { list.add(Component.translatable("ic2.reactoritem.heatwarning.line1")); list.add(Component.translatable("ic2.reactoritem.heatwarning.line2")); }
    }
}
