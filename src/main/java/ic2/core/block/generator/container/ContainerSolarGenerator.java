package ic2.core.block.generator.container;

import ic2.core.block.generator.tileentity.TileEntitySolarGenerator;
import net.minecraft.world.entity.player.Inventory;

public class ContainerSolarGenerator extends ContainerBaseGenerator<TileEntitySolarGenerator> {
    public ContainerSolarGenerator(int id, Inventory inventory, TileEntitySolarGenerator base) {
        super(id, inventory, base, 166, 80, 26);
        sync(() -> base.sunIsVisible ? 1 : 0, v -> base.sunIsVisible = v != 0);
    }
}
