package ic2.core.block.wiring;

import ic2.core.ContainerBase;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerChargepad extends ContainerBase<TileEntityChargepad> {
    public ContainerChargepad(int id, Inventory inv, TileEntityChargepad base) {
        super(id, inv, base);
        addPlayerInventorySlots(inv, 162);
        addSlot(new SlotInvSlot(base.chargeSlot, 0, 56, 17));
        addSlot(new SlotInvSlot(base.dischargeSlot, 0, 56, 53));
        sync(() -> base.energy, v -> base.energy = v);
        sync(() -> base.redstoneMode, v -> base.redstoneMode = (byte)v);
    }
    @Override public void onContainerEvent(String event) { if ("redstone".equals(event)) base.cycleRedstoneMode(player); }
}
