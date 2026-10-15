package ic2.core.slot;

import ic2.core.block.invslot.InvSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SlotInvSlotReadOnly extends SlotInvSlot {
    public SlotInvSlotReadOnly(InvSlot invSlot, int index, int x, int y) { super(invSlot, index, x, y); }
    @Override public boolean mayPlace(ItemStack stack) { return false; }
    @Override public boolean mayPickup(Player player) { return false; }
}
