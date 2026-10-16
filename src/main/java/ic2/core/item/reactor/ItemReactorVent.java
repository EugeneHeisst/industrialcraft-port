package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import net.minecraft.world.item.ItemStack;

/** Heat vent: pulls up to reactorVent heat from the hull into itself and vents selfVent per tick. */
public class ItemReactorVent extends ItemReactorHeatStorage {
    public final int selfVent, reactorVent;
    public ItemReactorVent(Properties p, int heatStorage, int selfVent, int reactorVent) { super(p, heatStorage); this.selfVent = selfVent; this.reactorVent = reactorVent; }

    @Override
    public void processChamber(IReactor reactor, ItemStack stack, int x, int y, boolean heatRun) {
        if (!heatRun) return;
        if (reactorVent > 0) {
            int rheat = reactor.getHeat(), drain = Math.min(rheat, reactorVent);
            rheat -= drain;
            if (alterHeat(reactor, stack, x, y, drain) > 0) return;
            reactor.setHeat(rheat);
        }
        int self = alterHeat(reactor, stack, x, y, -selfVent);
        if (self <= 0) reactor.addEmitHeat(self + selfVent);
    }
}
