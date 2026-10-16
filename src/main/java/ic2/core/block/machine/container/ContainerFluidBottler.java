package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityFluidBottler;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerFluidBottler extends ContainerStandardMachine<TileEntityFluidBottler> {
    public ContainerFluidBottler(int id, Inventory inv, TileEntityFluidBottler base) {
        super(id, inv, base, 184, 8, 53, 0, 0, 117, 53, 152, 26);
        addSlot(new SlotInvSlot(base.drainInputSlot, 0, 44, 35));
        addSlot(new SlotInvSlot(base.fillInputSlot, 0, 44, 72));
        FluidSync.tank(this, base.getFluidTank());
    }
}
