package ic2.core.block.machine.container;

import ic2.core.ContainerBase;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerElectrolyzer extends ContainerBase<TileEntityElectrolyzer> {
    public ContainerElectrolyzer(int id, Inventory inv, TileEntityElectrolyzer base) {
        super(id, inv, base);
        addPlayerInventorySlots(inv, 166);
        addSlot(new SlotInvSlot(base.waterSlot, 0, 54, 35));
        addSlot(new SlotInvSlot(base.hydrogenSlot, 0, 112, 35));
        sync(() -> base.energy, v -> base.energy = (short)v);
    }
}
