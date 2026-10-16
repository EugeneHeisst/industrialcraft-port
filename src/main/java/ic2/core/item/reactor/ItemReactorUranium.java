package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Uranium fuel rod (1, 2 or 4 cells, 20000 ticks): pulses itself and neighbouring rods / reflectors for EU, produces
 * triangular(pulses) * 4 heat spread over adjacent heat stores, the rest going to the hull.
 */
public class ItemReactorUranium extends ItemGradualInt implements IReactorComponent {
    public final int numberOfCells;
    private final Supplier<Item> depleted;

    public ItemReactorUranium(Properties p, int cells, int duration, Supplier<Item> depleted) { super(p, duration, 64); this.numberOfCells = cells; this.depleted = depleted; }

    private record Coord(ItemStack stack, int x, int y) {}

    @Override
    public void processChamber(IReactor reactor, ItemStack stack, int x, int y, boolean heatRun) {
        if (!reactor.produceEnergy()) return;
        int basePulses = 1 + numberOfCells / 2;
        for (int iteration = 0; iteration < numberOfCells; iteration++) {
            if (!heatRun) {
                for (int i = 0; i < basePulses; i++) acceptUraniumPulse(reactor, stack, stack, x, y, x, y, false);
                pulseable(reactor, x - 1, y, stack, x, y, false); pulseable(reactor, x + 1, y, stack, x, y, false);
                pulseable(reactor, x, y - 1, stack, x, y, false); pulseable(reactor, x, y + 1, stack, x, y, false);
            } else {
                int pulses = basePulses + pulseable(reactor, x - 1, y, stack, x, y, true) + pulseable(reactor, x + 1, y, stack, x, y, true)
                    + pulseable(reactor, x, y - 1, stack, x, y, true) + pulseable(reactor, x, y + 1, stack, x, y, true);
                int heat = getFinalHeat(reactor, stack, x, y, (pulses * pulses + pulses) / 2 * 4);
                Queue<Coord> acceptors = new ArrayDeque<>();
                acceptor(reactor, x - 1, y, acceptors); acceptor(reactor, x + 1, y, acceptors); acceptor(reactor, x, y - 1, acceptors); acceptor(reactor, x, y + 1, acceptors);
                while (!acceptors.isEmpty() && heat > 0) {
                    int dheat = heat / acceptors.size();
                    heat -= dheat;
                    Coord a = acceptors.remove();
                    heat += ((IReactorComponent)a.stack.getItem()).alterHeat(reactor, a.stack, a.x, a.y, dheat);
                }
                if (heat > 0) reactor.addHeat(heat);
            }
        }
        if (getCustomDamage(stack) >= getMaxCustomDamage(stack) - 1) reactor.setItemAt(x, y, new ItemStack(depleted.get()));
        else if (heatRun) applyCustomDamage(stack, 1);
    }

    protected int getFinalHeat(IReactor reactor, ItemStack stack, int x, int y, int heat) { return heat; }

    private static int pulseable(IReactor reactor, int x, int y, ItemStack me, int mx, int my, boolean heatRun) {
        ItemStack o = reactor.getItemAt(x, y);
        return !o.isEmpty() && o.getItem() instanceof IReactorComponent c && c.acceptUraniumPulse(reactor, o, me, x, y, mx, my, heatRun) ? 1 : 0;
    }

    private static void acceptor(IReactor reactor, int x, int y, Queue<Coord> out) {
        ItemStack t = reactor.getItemAt(x, y);
        if (!t.isEmpty() && t.getItem() instanceof IReactorComponent c && c.canStoreHeat(reactor, t, x, y)) out.add(new Coord(t, x, y));
    }

    @Override
    public boolean acceptUraniumPulse(IReactor reactor, ItemStack yourStack, ItemStack pulsingStack, int youX, int youY, int pulseX, int pulseY, boolean heatRun) {
        if (!heatRun) reactor.addOutput(1);
        return true;
    }

    @Override public boolean canStoreHeat(IReactor r, ItemStack s, int x, int y) { return false; }
    @Override public int getMaxHeat(IReactor r, ItemStack s, int x, int y) { return 0; }
    @Override public int getCurrentHeat(IReactor r, ItemStack s, int x, int y) { return 0; }
    @Override public int alterHeat(IReactor r, ItemStack s, int x, int y, int heat) { return heat; }
    @Override public float influenceExplosion(IReactor r, ItemStack s) { return 2 * numberOfCells; }

    /** MOX: 10000 ticks; output grows with hull heat, heat doubles above 50 % in a fluid reactor. */
    public static class MOX extends ItemReactorUranium {
        public MOX(Properties p, int cells, Supplier<Item> depleted) { super(p, cells, 10000, depleted); }
        @Override protected int getFinalHeat(IReactor reactor, ItemStack stack, int x, int y, int heat) {
            return reactor.isFluidCooled() && (float)reactor.getHeat() / reactor.getMaxHeat() > 0.5 ? heat * 2 : heat;
        }
        @Override public boolean acceptUraniumPulse(IReactor reactor, ItemStack yourStack, ItemStack pulsingStack, int youX, int youY, int pulseX, int pulseY, boolean heatRun) {
            if (!heatRun) reactor.addOutput(4f * reactor.getHeat() / reactor.getMaxHeat() + 1);
            return true;
        }
    }
}
