package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Heat exchanger: balances its heat with neighbouring heat stores (switchSide) and with the reactor hull (switchReactor). */
public class ItemReactorHeatSwitch extends ItemReactorHeatStorage {
    public final int switchSide, switchReactor;
    public ItemReactorHeatSwitch(Properties p, int heatStorage, int switchSide, int switchReactor) { super(p, heatStorage); this.switchSide = switchSide; this.switchReactor = switchReactor; }

    private record Coord(ItemStack stack, int x, int y) {}

    private int step(double sum, double other, double mine, int cap, int computed) {
        int add = Math.min(computed, cap);
        if (sum < 1) add = switchSide / 2;
        if (sum < 0.75) add = switchSide / 4;
        if (sum < 0.5) add = switchSide / 8;
        if (sum < 0.25) add = 1;
        double o = Math.round(other * 10) / 10.0, m = Math.round(mine * 10) / 10.0;
        if (o > m) add -= 2 * add; else if (o == m) add = 0;
        return add;
    }

    @Override
    public void processChamber(IReactor reactor, ItemStack yourStack, int x, int y, boolean heatRun) {
        if (!heatRun) return;
        int myHeat = 0;
        if (switchSide > 0) {
            List<Coord> acceptors = new ArrayList<>();
            int[][] n = {{x - 1, y}, {x + 1, y}, {x, y - 1}, {x, y + 1}};
            for (int[] c : n) {
                ItemStack t = reactor.getItemAt(c[0], c[1]);
                if (!t.isEmpty() && t.getItem() instanceof IReactorComponent comp && comp.canStoreHeat(reactor, t, c[0], c[1])) acceptors.add(new Coord(t, c[0], c[1]));
            }
            for (Coord c : acceptors) {
                IReactorComponent h = (IReactorComponent)c.stack.getItem();
                double mymed = getCurrentHeat(reactor, yourStack, x, y) * 100.0 / getMaxHeat(reactor, yourStack, x, y);
                double hmed = h.getCurrentHeat(reactor, c.stack, c.x, c.y) * 100.0 / h.getMaxHeat(reactor, c.stack, c.x, c.y);
                int add = step(hmed + mymed / 2, hmed, mymed, switchSide, (int)(h.getMaxHeat(reactor, c.stack, c.x, c.y) / 100.0 * (hmed + mymed / 2)));
                myHeat -= add;
                myHeat += h.alterHeat(reactor, c.stack, c.x, c.y, add);
            }
        }
        if (switchReactor > 0) {
            double mymed = getCurrentHeat(reactor, yourStack, x, y) * 100.0 / getMaxHeat(reactor, yourStack, x, y);
            double rmed = reactor.getHeat() * 100.0 / reactor.getMaxHeat();
            int add = step(rmed + mymed / 2, rmed, mymed, switchReactor, (int)Math.round(reactor.getMaxHeat() / 100.0 * (rmed + mymed / 2)));
            myHeat -= add;
            reactor.setHeat(reactor.getHeat() + add);
        }
        alterHeat(reactor, yourStack, x, y, myHeat);
    }
}
