package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityBlockCutter;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerBlockCutter extends ContainerStandardMachine<TileEntityBlockCutter> {
    public ContainerBlockCutter(int id, Inventory inventory, TileEntityBlockCutter base) {
        super(id, inventory, base, 166, 26, 53, 26, 17, 116, 34, 152, 8);
        addSlot(new SlotInvSlot(base.cutterSlot, 0, 71, 35));
        sync(() -> base.bladetoweak ? 1 : 0, v -> base.bladetoweak = v != 0);
    }
}
