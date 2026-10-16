package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityMiner;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerMiner extends ContainerElectricMachine<TileEntityMiner> {
    public ContainerMiner(int id, Inventory inv, TileEntityMiner base) {
        super(id, inv, base, 166, 152, 58);
        addSlot(new SlotInvSlot(base.scannerSlot, 0, 8, 58));
        addSlot(new SlotInvSlot(base.pipeSlot, 0, 8, 40));
        addSlot(new SlotInvSlot(base.drillSlot, 0, 8, 22));
        addSlot(new SlotInvSlot(base.upgradeSlot, 0, 152, 22));
        for (int y = 0; y < base.buffer.size() / 5; y++) for (int x = 0; x < 5; x++) addSlot(new SlotInvSlot(base.buffer, x + y * 5, 44 + x * 18, 22 + y * 18));
        sync(() -> base.progress, v -> base.progress = (int)v);
    }
}
