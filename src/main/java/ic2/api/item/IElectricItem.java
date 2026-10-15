package ic2.api.item;

import net.minecraft.world.item.ItemStack;

/** Items storing EU; the charge lives in the ic2:charge data component and is handled by {@link ElectricItem#manager}. */
public interface IElectricItem {
    boolean canProvideEnergy(ItemStack stack);
    double getMaxCharge(ItemStack stack);
    int getTier(ItemStack stack);
    double getTransferLimit(ItemStack stack);
}
