package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityInduction;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerInduction extends ContainerElectricMachine<TileEntityInduction> {
    public ContainerInduction(int id, Inventory inventory, TileEntityInduction base) {
        super(id, inventory, base, 166, 56, 53);
        addSlot(new SlotInvSlot(base.inputSlotA, 0, 47, 17));
        addSlot(new SlotInvSlot(base.inputSlotB, 0, 63, 17));
        addSlot(new SlotInvSlot(base.outputSlotA, 0, 113, 35));
        addSlot(new SlotInvSlot(base.outputSlotB, 0, 131, 35));
        for (int i = 0; i < 2; i++) addSlot(new SlotInvSlot(base.upgradeSlot, i, 153, 26 + i * 18));
        sync(() -> base.progress, v -> base.progress = (short)v);
        sync(() -> base.heat, v -> base.heat = (short)v);
    }
}
