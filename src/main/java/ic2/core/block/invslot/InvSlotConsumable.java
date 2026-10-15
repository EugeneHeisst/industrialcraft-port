package ic2.core.block.invslot;

import ic2.core.block.TileEntityInventory;
import ic2.core.util.StackUtil;
import net.minecraft.world.item.ItemStack;

public abstract class InvSlotConsumable extends InvSlot {
    public InvSlotConsumable(TileEntityInventory base, String name, int count) { super(base, name, Access.I, count, InvSide.TOP); }
    public InvSlotConsumable(TileEntityInventory base, String name, Access access, int count, InvSide preferredSide) { super(base, name, access, count, preferredSide); }

    @Override public abstract boolean accepts(ItemStack stack);

    @Override public boolean canOutput() { return super.canOutput() || access != Access.NONE && !get().isEmpty() && !accepts(get()); }

    public ItemStack consume(int amount) { return consume(amount, false, false); }

    /** @return the consumed stack or EMPTY */
    public ItemStack consume(int amount, boolean simulate, boolean consumeContainers) {
        ItemStack ret = ItemStack.EMPTY;
        for (int i = 0; i < size(); i++) {
            ItemStack stack = get(i);
            if (stack.isEmpty() || !accepts(stack) || !ret.isEmpty() && !StackUtil.isStackEqualStrict(stack, ret)) continue;
            boolean hasContainer = stack.hasCraftingRemainingItem();
            if (stack.getCount() != 1 && !consumeContainers && hasContainer) continue;
            int current = Math.min(amount, stack.getCount());
            amount -= current;
            if (ret.isEmpty()) ret = stack.copyWithCount(current);
            else ret.grow(current);
            if (!simulate) {
                if (stack.getCount() == current) put(i, !consumeContainers && hasContainer ? stack.getCraftingRemainingItem() : ItemStack.EMPTY);
                else stack.shrink(current);
            }
            if (amount == 0) break;
        }
        return ret;
    }
}
