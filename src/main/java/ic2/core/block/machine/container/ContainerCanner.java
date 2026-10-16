package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerCanner extends ContainerStandardMachine<TileEntityCanner> {
    public ContainerCanner(int id, Inventory inv, TileEntityCanner base) {
        super(id, inv, base, 184, 8, 80, 80, 44, 119, 17, 152, 26);
        addSlot(new SlotInvSlot(base.canInputSlot, 0, 41, 17));
        sync(() -> base.getMode().ordinal(), v -> base.setMode(TileEntityCanner.Mode.values()[(int)v]));
        FluidSync.tank(this, base.inputTank);
        FluidSync.tank(this, base.outputTank);
    }
    @Override public void onContainerEvent(String event) { base.onNetworkEvent(Integer.parseInt(event)); }
}
