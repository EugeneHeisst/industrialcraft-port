package ic2.api.item;

import net.minecraft.world.item.ItemStack;

public interface ISpecialElectricItem extends IElectricItem {
    IElectricItemManager getManager(ItemStack stack);
}
