package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntitySolidCanner;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerSolidCanner extends ContainerStandardMachine<TileEntitySolidCanner> {
    public ContainerSolidCanner(int id, Inventory inv, TileEntitySolidCanner base) {
        super(id, inv, base, 166, 8, 62, 37, 36, 116, 36, 152, 8);
        addSlot(new SlotInvSlot(base.canInputSlot, 0, 67, 36));
    }
}
