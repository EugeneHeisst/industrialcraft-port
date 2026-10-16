package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import net.minecraft.world.item.ItemStack;

/** RSH / LZH condensator: absorbs heat until full; refilled with redstone / lapis in a crafting grid. */
public class ItemReactorCondensator extends ItemGradualInt implements ReactorComponent {
    public ItemReactorCondensator(Properties p, int maxDmg) { super(p, maxDmg + 1); }

    @Override public boolean canStoreHeat(IReactor r, ItemStack s, int x, int y) { return getCustomDamage(s) + 1 < getMaxCustomDamage(s); }
    @Override public int getMaxHeat(IReactor r, ItemStack s, int x, int y) { return getMaxCustomDamage(s); }

    @Override
    public int alterHeat(IReactor reactor, ItemStack stack, int x, int y, int heat) {
        if (heat < 0) return heat;
        int can = Math.min(getMaxCustomDamage(stack) - (getCustomDamage(stack) + 1), heat);
        setCustomDamage(stack, getCustomDamage(stack) + can);
        return heat - can;
    }
}
