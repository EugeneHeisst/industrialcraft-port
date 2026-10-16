package ic2.core.block.generator.container;

import ic2.core.block.generator.tileentity.TileEntityWaterGenerator;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerWaterGenerator extends ContainerBaseGenerator<TileEntityWaterGenerator> {
    public ContainerWaterGenerator(int id, Inventory inventory, TileEntityWaterGenerator base) {
        super(id, inventory, base, 166, 80, 17);
        addSlot(new SlotInvSlot(base.fuelSlot, 0, 80, 53));
    }
}
