package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityMetalFormer;
import net.minecraft.world.entity.player.Inventory;

public class ContainerMetalFormer extends ContainerStandardMachine<TileEntityMetalFormer> {
    public ContainerMetalFormer(int id, Inventory inventory, TileEntityMetalFormer base) {
        super(id, inventory, base, 166, 17, 53, 17, 17, 116, 35, 152, 8);
        sync(base::getMode, v -> base.setMode((int)v));
    }
    @Override public void onContainerEvent(String event) { if ("0".equals(event)) base.cycleMode(); }
}
