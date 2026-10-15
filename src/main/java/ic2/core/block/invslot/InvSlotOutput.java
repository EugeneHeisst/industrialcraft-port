package ic2.core.block.invslot;

import ic2.core.block.TileEntityInventory;
import ic2.core.util.StackUtil;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class InvSlotOutput extends InvSlot {
    public InvSlotOutput(TileEntityInventory base, String name, int count) { super(base, name, Access.O, count, InvSide.BOTTOM); }

    @Override public boolean accepts(ItemStack stack) { return false; }
    public int add(List<ItemStack> stacks) { return add(stacks.toArray(new ItemStack[0]), false); }
    public int add(ItemStack stack) { return add(new ItemStack[]{stack}, false); }
    public boolean canAdd(List<ItemStack> stacks) { return add(stacks.toArray(new ItemStack[0]), true) == 0; }
    public boolean canAdd(ItemStack stack) { return add(new ItemStack[]{stack}, true) == 0; }

    private int add(ItemStack[] stacks, boolean simulate) {
        if (stacks == null) return 0;
        if (simulate) backup();
        int total = 0;
        for (ItemStack stack : stacks) {
            int amount = stack.getCount();
            for (int pass = 0; pass < 2 && amount > 0; pass++) {
                for (int i = 0; i < size() && amount > 0; i++) {
                    ItemStack existing = get(i);
                    if (pass == 0 && !existing.isEmpty()) {
                        int space = Math.min(getStackSizeLimit(), existing.getMaxStackSize()) - existing.getCount();
                        if (space > 0 && StackUtil.isStackEqualStrict(stack, existing)) {
                            int moved = Math.min(space, amount);
                            existing.grow(moved);
                            amount -= moved;
                        }
                    } else if (pass == 1 && existing.isEmpty()) {
                        put(i, stack.copyWithCount(amount));
                        amount = 0;
                    }
                }
            }
            total += amount;
        }
        if (simulate) restore();
        return total;
    }
}
