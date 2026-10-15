package ic2.api.info;

import net.minecraft.world.item.ItemStack;

public interface IFuelValueProvider {
    int getFuelValue(ItemStack stack, boolean allowLava);
}
