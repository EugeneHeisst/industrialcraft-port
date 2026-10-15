package ic2.core.slot;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Armor slot of the player shown in IC2 GUIs (InvSlotArmor of the original). */
public class SlotArmor extends Slot {
    private final EquipmentSlot slot;
    public SlotArmor(Inventory inventory, EquipmentSlot slot, int x, int y) { super(inventory, 36 + slot.getIndex(), x, y); this.slot = slot; }
    @Override public boolean mayPlace(ItemStack stack) { return stack.canEquip(slot, ((Inventory)container).player); }
    @Override public int getMaxStackSize() { return 1; }
    @Override public boolean mayPickup(Player player) { return true; }
}
