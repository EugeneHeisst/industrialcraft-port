package ic2.core.block.machine.container;

import ic2.core.ContainerBase;
import ic2.core.block.machine.tileentity.TileEntityIronFurnace;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerIronFurnace extends ContainerBase<TileEntityIronFurnace> {
    public ContainerIronFurnace(int id, Inventory inventory, TileEntityIronFurnace base) {
        super(id, inventory, base);
        addPlayerInventorySlots(inventory, 166);
        addSlot(new SlotInvSlot(base.inputSlot, 0, 56, 17));
        addSlot(new SlotInvSlot(base.fuelSlot, 0, 56, 53));
        addSlot(new SlotInvSlot(base.outputSlot, 0, 116, 35));
        sync(() -> base.progress, v -> base.progress = (short)v);
        sync(() -> base.fuel, v -> base.fuel = (int)v);
        sync(() -> base.maxFuel, v -> base.maxFuel = (int)v);
    }
}
