package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IElectricItemManager;
import ic2.core.ref.Ic2Components;
import ic2.core.util.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Default manager: the charge is the ic2:charge component; a charged stack never stacks (RE batteries stack while empty). */
public class ElectricItemManager implements IElectricItemManager {
    public static double getRawCharge(ItemStack stack) { return stack.getOrDefault(Ic2Components.CHARGE.get(), 0.0); }

    public static void setRawCharge(ItemStack stack, double charge) {
        if (charge > 0) {
            stack.set(Ic2Components.CHARGE.get(), charge);
            if (stack.getItem().getDefaultMaxStackSize() > 1) stack.set(DataComponents.MAX_STACK_SIZE, 1);
        } else {
            stack.remove(Ic2Components.CHARGE.get());
            stack.remove(DataComponents.MAX_STACK_SIZE);
        }
    }

    @Override
    public double charge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
        IElectricItem item = (IElectricItem)stack.getItem();
        if (amount < 0 || stack.getCount() > 1 || item.getTier(stack) > tier) return 0;
        if (!ignoreTransferLimit && amount > item.getTransferLimit(stack)) amount = item.getTransferLimit(stack);
        double charge = getRawCharge(stack);
        amount = Math.min(amount, item.getMaxCharge(stack) - charge);
        if (!simulate) setRawCharge(stack, charge + amount);
        return amount;
    }

    @Override
    public double discharge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean externally, boolean simulate) {
        IElectricItem item = (IElectricItem)stack.getItem();
        if (amount < 0 || stack.getCount() > 1 || item.getTier(stack) > tier) return 0;
        if (externally && !item.canProvideEnergy(stack)) return 0;
        if (!ignoreTransferLimit && amount > item.getTransferLimit(stack)) amount = item.getTransferLimit(stack);
        double charge = getRawCharge(stack);
        amount = Math.min(amount, charge);
        if (!simulate) setRawCharge(stack, charge - amount);
        return amount;
    }

    @Override public double getCharge(ItemStack stack) { return ElectricItem.manager.discharge(stack, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, false, true); }
    @Override public boolean canUse(ItemStack stack, double amount) { return ElectricItem.manager.getCharge(stack) >= amount; }

    @Override
    public boolean use(ItemStack stack, double amount, LivingEntity entity) {
        ElectricItem.manager.chargeFromArmor(stack, entity);
        double transfer = ElectricItem.manager.discharge(stack, amount, Integer.MAX_VALUE, true, false, true);
        if (!Util.isSimilar(transfer, amount)) return false;
        ElectricItem.manager.discharge(stack, amount, Integer.MAX_VALUE, true, false, false);
        ElectricItem.manager.chargeFromArmor(stack, entity);
        return true;
    }

    @Override
    public void chargeFromArmor(ItemStack stack, LivingEntity entity) {
        if (entity == null) return;
        boolean transferred = false;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            ItemStack armor = entity.getItemBySlot(slot);
            if (armor.isEmpty()) continue;
            int tier = armor.getItem() instanceof IElectricItem e ? e.getTier(stack) : Integer.MAX_VALUE;
            double transfer = ElectricItem.manager.discharge(armor, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true, true);
            if (transfer <= 0) continue;
            transfer = ElectricItem.manager.charge(stack, transfer, tier, true, false);
            if (transfer <= 0) continue;
            ElectricItem.manager.discharge(armor, transfer, Integer.MAX_VALUE, true, true, false);
            transferred = true;
        }
        if (transferred && entity instanceof Player p && !p.level().isClientSide) p.containerMenu.broadcastChanges();
    }

    @Override
    public String getToolTip(ItemStack stack) {
        double charge = ElectricItem.manager.getCharge(stack);
        double space = ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true);
        return Util.toSiString(charge, 3) + "/" + Util.toSiString(charge + space, 3) + " EU";
    }
}
