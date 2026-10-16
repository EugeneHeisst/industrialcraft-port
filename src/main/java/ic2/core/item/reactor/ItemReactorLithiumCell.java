package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.core.ref.Ic2Items;
import net.minecraft.world.item.ItemStack;

/** Lithium cell: each pulse breeds it by 1 + hull heat / 3000 until it becomes a tritium cell (10000). */
public class ItemReactorLithiumCell extends ItemGradualInt implements ReactorComponent {
    public ItemReactorLithiumCell(Properties p) { super(p, 10000); }

    @Override
    public boolean acceptUraniumPulse(IReactor reactor, ItemStack stack, ItemStack pulsing, int youX, int youY, int pulseX, int pulseY, boolean heatRun) {
        if (heatRun) {
            int level = getCustomDamage(stack) + 1 + reactor.getHeat() / 3000;
            if (level >= maxDmg) reactor.setItemAt(youX, youY, new ItemStack(Ic2Items.get("tritium_cell")));
            else setCustomDamage(stack, level);
        }
        return true;
    }
}
