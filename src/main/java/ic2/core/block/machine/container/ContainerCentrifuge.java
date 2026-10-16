package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityCentrifuge;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerCentrifuge extends ContainerStandardMachine<TileEntityCentrifuge> {
    public ContainerCentrifuge(int id, Inventory inventory, TileEntityCentrifuge base) {
        super(id, inventory, base, 166, 11, 60, 11, 21, -1000, 0, 152, 8);
        for (int i = 0; i < 3; i++) addSlot(new SlotInvSlot(base.outputSlot, i, 124, 18 + i * 18));
        sync(() -> base.workheat, v -> base.workheat = (short)v);
        sync(() -> base.heat, v -> base.heat = (short)v);
    }
}
