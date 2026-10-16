package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import net.minecraft.world.item.ItemStack;

/** Defaults of a component that neither stores heat nor reacts to pulses. */
public interface ReactorComponent extends IReactorComponent {
    @Override default void processChamber(IReactor r, ItemStack s, int x, int y, boolean heatRun) {}
    @Override default boolean acceptUraniumPulse(IReactor r, ItemStack s, ItemStack p, int x, int y, int px, int py, boolean heatRun) { return false; }
    @Override default boolean canStoreHeat(IReactor r, ItemStack s, int x, int y) { return false; }
    @Override default int getMaxHeat(IReactor r, ItemStack s, int x, int y) { return 0; }
    @Override default int getCurrentHeat(IReactor r, ItemStack s, int x, int y) { return 0; }
    @Override default int alterHeat(IReactor r, ItemStack s, int x, int y, int heat) { return heat; }
    @Override default float influenceExplosion(IReactor r, ItemStack s) { return 0; }
}
