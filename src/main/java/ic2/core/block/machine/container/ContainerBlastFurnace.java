package ic2.core.block.machine.container;

import ic2.core.ContainerBase;
import ic2.core.block.machine.tileentity.TileEntityBlastFurnace;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerBlastFurnace extends ContainerBase<TileEntityBlastFurnace> {
    public ContainerBlastFurnace(int id, Inventory inventory, TileEntityBlastFurnace base) {
        super(id, inventory, base);
        addPlayerInventorySlots(inventory, 166);
        addSlot(new SlotInvSlot(base.inputSlot, 0, 35, 33));
        addSlot(new SlotInvSlot(base.outputSlot, 0, 134, 56));
        addSlot(new SlotInvSlot(base.slagOutputSlot, 0, 152, 56));
        addSlot(new SlotInvSlot(base.airSlot, 0, 26, 56));
        addSlot(new SlotInvSlot(base.airOutputSlot, 0, 44, 56));
        for (int i = 0; i < 2; i++) addSlot(new SlotInvSlot(base.upgradeSlot, i, 152, 8 + i * 18));
        sync(() -> base.heat, v -> base.heat = (int)v);
        sync(() -> base.progress, v -> base.progress = (int)v);
        sync(() -> base.outOfAir ? 1 : 0, v -> base.outOfAir = v != 0);
    }
}
