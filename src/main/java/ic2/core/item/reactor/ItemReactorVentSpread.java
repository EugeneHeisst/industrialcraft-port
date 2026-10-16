package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Component heat vent: vents sideVent from each neighbouring heat store. */
public class ItemReactorVentSpread extends Item implements ReactorComponent {
    public final int sideVent;
    public ItemReactorVentSpread(Properties p, int sideVent) { super(p.stacksTo(1)); this.sideVent = sideVent; }

    @Override
    public void processChamber(IReactor reactor, ItemStack stack, int x, int y, boolean heatRun) {
        if (!heatRun) return;
        cool(reactor, x - 1, y); cool(reactor, x + 1, y); cool(reactor, x, y - 1); cool(reactor, x, y + 1);
    }

    private void cool(IReactor reactor, int x, int y) {
        ItemStack s = reactor.getItemAt(x, y);
        if (!s.isEmpty() && s.getItem() instanceof IReactorComponent c && c.canStoreHeat(reactor, s, x, y)) {
            int self = c.alterHeat(reactor, s, x, y, -sideVent);
            if (self <= 0) reactor.addEmitHeat(self + sideVent);
        }
    }
}
