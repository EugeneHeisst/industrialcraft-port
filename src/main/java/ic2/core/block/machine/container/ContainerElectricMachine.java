package ic2.core.block.machine.container;

import ic2.core.ContainerBase;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public abstract class ContainerElectricMachine<T extends TileEntityElectricMachine> extends ContainerBase<T> {
    protected ContainerElectricMachine(int id, Inventory inventory, T base, int height, int dischargeX, int dischargeY) {
        super(id, inventory, base);
        addPlayerInventorySlots(inventory, height);
        if (dischargeX > -1000) addSlot(new SlotInvSlot(base.dischargeSlot, 0, dischargeX, dischargeY));
        sync(() -> base.guiChargeLevel, v -> base.guiChargeLevel = (float)v);
        sync(base::getTier, v -> base.setTier((int)v));
    }
}
