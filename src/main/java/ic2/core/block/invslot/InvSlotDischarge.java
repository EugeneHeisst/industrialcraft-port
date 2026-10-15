package ic2.core.block.invslot;

import ic2.api.info.Info;
import ic2.api.item.ElectricItem;
import ic2.core.block.TileEntityInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class InvSlotDischarge extends InvSlot {
    public int tier;
    public boolean allowRedstoneDust;

    public InvSlotDischarge(TileEntityInventory base, Access access, int tier) { this(base, access, tier, InvSide.ANY); }
    public InvSlotDischarge(TileEntityInventory base, Access access, int tier, InvSide preferredSide) { this(base, access, tier, true, preferredSide); }
    public InvSlotDischarge(TileEntityInventory base, Access access, int tier, boolean allowRedstoneDust, InvSide preferredSide) {
        super(base, "discharge", access, 1, preferredSide);
        this.tier = tier;
        this.allowRedstoneDust = allowRedstoneDust;
    }

    @Override
    public boolean accepts(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(Items.REDSTONE) && !allowRedstoneDust) return false;
        return Info.itemEnergy.getEnergyValue(stack) > 0 || ElectricItem.manager.discharge(stack, Double.POSITIVE_INFINITY, tier, true, true, true) > 0;
    }

    public double discharge(double amount, boolean ignoreLimit) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0.");
        ItemStack stack = get(0);
        if (stack.isEmpty()) return 0;
        double real = ElectricItem.manager.discharge(stack, amount, tier, ignoreLimit, true, false);
        if (real <= 0) {
            real = Info.itemEnergy.getEnergyValue(stack);
            if (real <= 0) return 0;
            stack.shrink(1);
            if (stack.isEmpty()) put(0, ItemStack.EMPTY);
        }
        return real;
    }

    public void setTier(int tier) { this.tier = tier; }
}
