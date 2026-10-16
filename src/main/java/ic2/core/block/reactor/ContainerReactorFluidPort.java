package ic2.core.block.reactor;

import ic2.core.ContainerBase;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerReactorFluidPort extends ContainerBase<TileEntityReactorPort.FluidPort> {
    public ContainerReactorFluidPort(int id, Inventory inv, TileEntityReactorPort.FluidPort base) {
        super(id, inv, base);
        addPlayerInventorySlots(inv, 166);
        addSlot(new SlotInvSlot(base.upgradeSlot, 0, 80, 43));
    }
}
