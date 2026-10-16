package ic2.core.block.personal;

import ic2.core.ContainerBase;
import ic2.core.slot.SlotInvSlot;
import ic2.core.slot.SlotInvSlotReadOnly;
import net.minecraft.world.entity.player.Inventory;

/** Owner view (demand, upgrade, price buttons) or customer view (read-only demand, paid-for counter). */
public class ContainerEnergyOMat extends ContainerBase<TileEntityEnergyOMat> {
    public final boolean open;

    public ContainerEnergyOMat(int id, Inventory inventory, TileEntityEnergyOMat base, boolean open) {
        super(id, inventory, base);
        this.open = open;
        if (open) {
            addSlot(new SlotInvSlot(base.demandSlot, 0, 24, 17));
            addSlot(new SlotInvSlot(base.upgradeSlot, 0, 24, 53));
            addSlot(new SlotInvSlot(base.inputSlot, 0, 60, 17));
            addSlot(new SlotInvSlot(base.chargeSlot, 0, 60, 53));
        } else {
            addSlot(new SlotInvSlotReadOnly(base.demandSlot, 0, 50, 17));
            addSlot(new SlotInvSlot(base.inputSlot, 0, 143, 17));
            addSlot(new SlotInvSlot(base.chargeSlot, 0, 143, 53));
        }
        addPlayerInventorySlots(inventory, 166);
        sync(() -> base.paidFor, v -> base.paidFor = (int)v);
        sync(() -> base.euBuffer, v -> base.euBuffer = v);
        sync(() -> base.euOffer, v -> base.euOffer = (int)v);
        sync(() -> base.chargeSlot.tier, v -> base.chargeSlot.tier = (int)v);
    }

    @Override public void onContainerEvent(String event) { if (open) base.onNetworkEvent(player, Integer.parseInt(event)); }
}
