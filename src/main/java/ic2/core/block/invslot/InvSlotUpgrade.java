package ic2.core.block.invslot;

import ic2.core.block.TileEntityInventory;
import ic2.core.upgrade.IUpgradableBlock;
import ic2.core.upgrade.IUpgradeItem;
import net.minecraft.world.item.ItemStack;

public class InvSlotUpgrade extends InvSlot {
    public int augmentation, extraProcessTime, extraEnergyDemand, extraEnergyStorage, extraTier;
    public double processTimeMultiplier, energyDemandMultiplier, energyStorageMultiplier;
    private int redstoneModifierCount;

    public InvSlotUpgrade(TileEntityInventory base, String name, int count) {
        super(base, name, Access.NONE, count);
        if (!(base instanceof IUpgradableBlock)) throw new IllegalArgumentException("Base needs to be an IUpgradableBlock.");
        resetRates();
    }

    @Override
    public boolean accepts(ItemStack stack) {
        return stack.getItem() instanceof IUpgradeItem item && item.isSuitableFor(stack, ((IUpgradableBlock)base).getUpgradableProperties());
    }

    @Override
    public void onChanged() {
        resetRates();
        IUpgradableBlock block = (IUpgradableBlock)base;
        for (int i = 0; i < size(); i++) {
            ItemStack stack = get(i);
            if (stack.isEmpty() || !accepts(stack)) continue;
            IUpgradeItem upgrade = (IUpgradeItem)stack.getItem();
            int n = stack.getCount();
            augmentation += upgrade.getAugmentation(stack, block) * n;
            extraProcessTime += upgrade.getExtraProcessTime(stack, block) * n;
            processTimeMultiplier *= Math.pow(upgrade.getProcessTimeMultiplier(stack, block), n);
            extraEnergyDemand += upgrade.getExtraEnergyDemand(stack, block) * n;
            energyDemandMultiplier *= Math.pow(upgrade.getEnergyDemandMultiplier(stack, block), n);
            extraEnergyStorage += upgrade.getExtraEnergyStorage(stack, block) * n;
            energyStorageMultiplier *= Math.pow(upgrade.getEnergyStorageMultiplier(stack, block), n);
            extraTier += upgrade.getExtraTier(stack, block) * n;
            if (upgrade.modifiesRedstoneInput(stack, block)) redstoneModifierCount++;
        }
    }

    /** Redstone input after all inverter upgrades. */
    public int getRedstoneInput(int input) {
        IUpgradableBlock block = (IUpgradableBlock)base;
        for (int i = 0; i < size(); i++) {
            ItemStack stack = get(i);
            if (!stack.isEmpty() && accepts(stack) && ((IUpgradeItem)stack.getItem()).modifiesRedstoneInput(stack, block))
                input = ((IUpgradeItem)stack.getItem()).getRedstoneInput(stack, block, input);
        }
        return input;
    }

    private void resetRates() {
        augmentation = extraProcessTime = extraEnergyDemand = extraEnergyStorage = extraTier = redstoneModifierCount = 0;
        processTimeMultiplier = energyDemandMultiplier = energyStorageMultiplier = 1;
    }
}
