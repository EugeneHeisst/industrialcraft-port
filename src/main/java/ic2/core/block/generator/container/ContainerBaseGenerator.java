package ic2.core.block.generator.container;

import ic2.core.ContainerBase;
import ic2.core.block.generator.tileentity.TileEntityBaseGenerator;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public abstract class ContainerBaseGenerator<T extends TileEntityBaseGenerator> extends ContainerBase<T> {
    protected ContainerBaseGenerator(int id, Inventory inventory, T base, int height, int chargeX, int chargeY) {
        super(id, inventory, base);
        addPlayerInventorySlots(inventory, height);
        addSlot(new SlotInvSlot(base.chargeSlot, 0, chargeX, chargeY));
        sync(() -> base.storage, v -> base.storage = v);
        sync(() -> base.fuel, v -> base.fuel = (int)v);
    }
}
