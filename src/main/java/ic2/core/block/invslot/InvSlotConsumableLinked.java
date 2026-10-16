package ic2.core.block.invslot;

import ic2.core.block.TileEntityInventory;
import ic2.core.util.StackUtil;
import net.minecraft.world.item.ItemStack;

/** Takes only the item of the linked (template) slot, and only in full multiples of its stack size. */
public class InvSlotConsumableLinked extends InvSlotConsumable {
    public final InvSlot linkedSlot;

    public InvSlotConsumableLinked(TileEntityInventory base, String name, int count, InvSlot linkedSlot) {
        super(base, name, count);
        this.linkedSlot = linkedSlot;
    }

    @Override
    public boolean accepts(ItemStack stack) {
        ItemStack required = linkedSlot.get();
        return !required.isEmpty() && StackUtil.isStackEqualStrict(required, stack);
    }

    /** @return the consumed stack, or EMPTY unless a full template stack is available */
    public ItemStack consumeLinked(boolean simulate) {
        ItemStack required = linkedSlot.get();
        if (required.isEmpty()) return ItemStack.EMPTY;
        ItemStack available = consume(required.getCount(), true, true);
        return available.getCount() == required.getCount() ? consume(required.getCount(), simulate, true) : ItemStack.EMPTY;
    }
}
