package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class GatewayElectricItemManager implements IElectricItemManager {
    private IElectricItemManager getManager(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.getItem() instanceof ISpecialElectricItem special) return special.getManager(stack);
        if (stack.getItem() instanceof IElectricItem) return ElectricItem.rawManager;
        return ElectricItem.getBackupManager(stack);
    }

    @Override public double charge(ItemStack stack, double amount, int tier, boolean ignore, boolean simulate) { var m = getManager(stack); return m == null ? 0 : m.charge(stack, amount, tier, ignore, simulate); }
    @Override public double discharge(ItemStack stack, double amount, int tier, boolean ignore, boolean externally, boolean simulate) { var m = getManager(stack); return m == null ? 0 : m.discharge(stack, amount, tier, ignore, externally, simulate); }
    @Override public double getCharge(ItemStack stack) { var m = getManager(stack); return m == null ? 0 : m.getCharge(stack); }
    @Override public boolean canUse(ItemStack stack, double amount) { var m = getManager(stack); return m != null && m.canUse(stack, amount); }
    @Override public boolean use(ItemStack stack, double amount, LivingEntity entity) { var m = getManager(stack); return m != null && m.use(stack, amount, entity); }
    @Override public void chargeFromArmor(ItemStack stack, LivingEntity entity) { var m = getManager(stack); if (m != null && entity != null) m.chargeFromArmor(stack, entity); }
    @Override public String getToolTip(ItemStack stack) { var m = getManager(stack); return m == null ? null : m.getToolTip(stack); }
}
