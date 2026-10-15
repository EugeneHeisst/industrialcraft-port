package ic2.core.block.generator.container;

import ic2.core.block.generator.tileentity.TileEntityGenerator;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerGenerator extends ContainerBaseGenerator<TileEntityGenerator> {
    public ContainerGenerator(int id, Inventory inventory, TileEntityGenerator base) {
        super(id, inventory, base, 166, 65, 17);
        addSlot(new SlotInvSlot(base.fuelSlot, 0, 65, 53));
        sync(() -> base.itemFuelTime, v -> base.itemFuelTime = (int)v);
    }
}
