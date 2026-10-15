package ic2.core.block.invslot;

import ic2.api.item.ElectricItem;
import ic2.core.block.TileEntityInventory;
import net.minecraft.world.item.ItemStack;

public class InvSlotCharge extends InvSlot {
    public int tier;

    public InvSlotCharge(TileEntityInventory base, int tier) {
        super(base, "charge", Access.IO, 1, InvSide.TOP);
        this.tier = tier;
    }

    @Override public boolean accepts(ItemStack stack) { return ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, tier, true, true) > 0; }

    public double charge(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0.");
        ItemStack stack = get(0);
        return stack.isEmpty() ? 0 : ElectricItem.manager.charge(stack, amount, tier, false, false);
    }

    public void setTier(int tier) { this.tier = tier; }
}
