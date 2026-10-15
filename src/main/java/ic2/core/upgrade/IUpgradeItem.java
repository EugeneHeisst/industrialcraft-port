package ic2.core.upgrade;

import java.util.List;
import java.util.Set;
import net.minecraft.world.item.ItemStack;

public interface IUpgradeItem {
    boolean isSuitableFor(ItemStack stack, Set<UpgradableProperty> types);
    int getAugmentation(ItemStack stack, IUpgradableBlock parent);
    int getExtraProcessTime(ItemStack stack, IUpgradableBlock parent);
    double getProcessTimeMultiplier(ItemStack stack, IUpgradableBlock parent);
    int getExtraEnergyDemand(ItemStack stack, IUpgradableBlock parent);
    double getEnergyDemandMultiplier(ItemStack stack, IUpgradableBlock parent);
    int getExtraEnergyStorage(ItemStack stack, IUpgradableBlock parent);
    double getEnergyStorageMultiplier(ItemStack stack, IUpgradableBlock parent);
    int getExtraTier(ItemStack stack, IUpgradableBlock parent);
    boolean modifiesRedstoneInput(ItemStack stack, IUpgradableBlock parent);
    int getRedstoneInput(ItemStack stack, IUpgradableBlock parent, int externalInput);
    boolean onTick(ItemStack stack, IUpgradableBlock parent);
    void onProcessEnd(ItemStack stack, IUpgradableBlock parent, List<ItemStack> output);
}
