package ic2.core.block.wiring;

import ic2.core.ContainerBase;
import net.minecraft.world.entity.player.Inventory;

public class ContainerTransformer extends ContainerBase<TileEntityTransformer> {
    public ContainerTransformer(int id, Inventory inventory, TileEntityTransformer base) {
        super(id, inventory, base);
        addPlayerInventorySlots(inventory, 219);
        sync(() -> base.mode, v -> base.mode = (int)v);
        sync(() -> base.inputflow, v -> base.inputflow = v);
        sync(() -> base.outputflow, v -> base.outputflow = v);
        sync(() -> base.redstone ? 1 : 0, v -> base.redstone = v != 0);
    }

    @Override public void onContainerEvent(String event) { base.onNetworkEvent(Integer.parseInt(event)); }
}
