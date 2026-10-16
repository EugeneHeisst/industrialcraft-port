package ic2.core.block.generator.container;

import ic2.core.block.generator.tileentity.TileEntityWindGenerator;
import net.minecraft.world.entity.player.Inventory;

public class ContainerWindGenerator extends ContainerBaseGenerator<TileEntityWindGenerator> {
    public ContainerWindGenerator(int id, Inventory inventory, TileEntityWindGenerator base) {
        super(id, inventory, base, 166, 80, 26);
        sync(() -> base.subproduction, v -> base.subproduction = v);
    }
}
