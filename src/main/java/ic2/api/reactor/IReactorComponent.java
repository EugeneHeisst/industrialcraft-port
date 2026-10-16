package ic2.api.reactor;

import net.minecraft.world.item.ItemStack;

/** Items usable inside a nuclear reactor. Each reactor tick runs a heat pass and an energy pass over the grid. */
public interface IReactorComponent {
    void processChamber(IReactor reactor, ItemStack stack, int x, int y, boolean heatRun);
    boolean acceptUraniumPulse(IReactor reactor, ItemStack yourStack, ItemStack pulsingStack, int youX, int youY, int pulseX, int pulseY, boolean heatRun);
    boolean canStoreHeat(IReactor reactor, ItemStack stack, int x, int y);
    int getMaxHeat(IReactor reactor, ItemStack stack, int x, int y);
    int getCurrentHeat(IReactor reactor, ItemStack stack, int x, int y);
    /** @return heat that could not be stored (negative: heat that could not be removed) */
    int alterHeat(IReactor reactor, ItemStack stack, int x, int y, int heat);
    /** >= 1 or <= 0 adds to the explosion power, values in (0, 1) multiply it */
    float influenceExplosion(IReactor reactor, ItemStack stack);
}
