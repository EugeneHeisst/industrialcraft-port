package ic2.api.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IElectricItemManager {
    /** @return the amount actually charged */
    double charge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate);
    /** @param externally true when a machine or another item pulls the energy (needs canProvideEnergy) */
    double discharge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean externally, boolean simulate);
    double getCharge(ItemStack stack);
    boolean canUse(ItemStack stack, double amount);
    boolean use(ItemStack stack, double amount, LivingEntity entity);
    void chargeFromArmor(ItemStack stack, LivingEntity entity);
    String getToolTip(ItemStack stack);
}
