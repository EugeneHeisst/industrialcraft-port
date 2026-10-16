package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Reactor plating: raises the hull heat capacity and scales heat effects / explosion power. */
public class ItemReactorPlating extends Item implements ReactorComponent {
    public final int maxHeatAdd;
    public final float effectModifier;
    public ItemReactorPlating(Properties p, int maxHeatAdd, float effectModifier) { super(p); this.maxHeatAdd = maxHeatAdd; this.effectModifier = effectModifier; }

    @Override
    public void processChamber(IReactor reactor, ItemStack stack, int x, int y, boolean heatRun) {
        if (!heatRun) return;
        reactor.setMaxHeat(reactor.getMaxHeat() + maxHeatAdd);
        reactor.setHeatEffectModifier(reactor.getHeatEffectModifier() * effectModifier);
    }

    @Override public float influenceExplosion(IReactor r, ItemStack s) { return effectModifier >= 1 ? 0 : effectModifier; }
}
