package ic2.core.slot;

import ic2.core.block.invslot.InvSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

public class SlotInvSlot extends Slot {
    public final InvSlot invSlot;
    public final int index;

    public SlotInvSlot(InvSlot invSlot, int index, int x, int y) {
        super(invSlot.base, invSlot.base.getBaseIndex(invSlot) + index, x, y);
        this.invSlot = invSlot;
        this.index = index;
    }

    @Override public boolean mayPlace(ItemStack stack) { return invSlot.accepts(stack); }
    @Override public int getMaxStackSize() { return invSlot.getStackSizeLimit(); }
}
