package ic2.core.block.personal;

import ic2.core.ContainerBase;
import ic2.core.slot.SlotInvSlot;
import ic2.core.slot.SlotInvSlotReadOnly;
import net.minecraft.world.entity.player.Inventory;

/** Owner view (template slots editable, trade statistics) or customer view (read-only templates, stock). */
public class ContainerTradeOMat extends ContainerBase<TileEntityTradeOMat> {
    public final boolean open, admin;

    public ContainerTradeOMat(int id, Inventory inventory, TileEntityTradeOMat base, boolean open, boolean admin) {
        super(id, inventory, base);
        this.open = open;
        this.admin = admin;
        if (open) {
            addSlot(new SlotInvSlot(base.demandSlot, 0, 50, 19));
            addSlot(new SlotInvSlot(base.offerSlot, 0, 50, 53));
            addSlot(new SlotInvSlot(base.inputSlot, 0, 80, 19));
            addSlot(new SlotInvSlot(base.outputSlot, 0, 80, 53));
        } else {
            addSlot(new SlotInvSlotReadOnly(base.demandSlot, 0, 50, 19));
            addSlot(new SlotInvSlotReadOnly(base.offerSlot, 0, 50, 38));
            addSlot(new SlotInvSlot(base.inputSlot, 0, 143, 19));
            addSlot(new SlotInvSlot(base.outputSlot, 0, 143, 53));
        }
        addPlayerInventorySlots(inventory, 166);
        sync(() -> base.stock, v -> base.stock = (int)v);
        sync(() -> base.totalTradeCount, v -> base.totalTradeCount = (int)v);
    }

    @Override public void onContainerEvent(String event) { if (open) base.onNetworkEvent(player, Integer.parseInt(event)); }
}
