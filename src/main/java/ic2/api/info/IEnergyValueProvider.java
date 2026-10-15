package ic2.api.info;

import net.minecraft.world.item.ItemStack;

public interface IEnergyValueProvider {
    double getEnergyValue(ItemStack stack);
}
