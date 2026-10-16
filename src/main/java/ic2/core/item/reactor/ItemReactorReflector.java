package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import net.minecraft.world.item.ItemStack;

/** Neutron reflector: bounces pulses back to the fuel rod and wears by one per reflected pulse. */
public class ItemReactorReflector extends ItemGradualInt implements ReactorComponent {
    public ItemReactorReflector(Properties p, int maxDamage) { super(p, maxDamage); }

    @Override
    public boolean acceptUraniumPulse(IReactor reactor, ItemStack stack, ItemStack pulsing, int youX, int youY, int pulseX, int pulseY, boolean heatRun) {
        if (!heatRun) ((IReactorComponent)pulsing.getItem()).acceptUraniumPulse(reactor, pulsing, stack, pulseX, pulseY, youX, youY, heatRun);
        else if (getCustomDamage(stack) + 1 >= getMaxCustomDamage(stack)) reactor.setItemAt(youX, youY, ItemStack.EMPTY);
        else setCustomDamage(stack, getCustomDamage(stack) + 1);
        return true;
    }

    @Override public float influenceExplosion(IReactor r, ItemStack s) { return -1; }
}
