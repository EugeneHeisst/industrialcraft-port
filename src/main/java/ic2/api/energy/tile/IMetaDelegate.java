package ic2.api.energy.tile;

import java.util.List;
import net.minecraft.world.level.block.entity.BlockEntity;

/** A tile spanning several block positions, all represented by the returned sub tiles. */
public interface IMetaDelegate extends IEnergyTile {
    List<BlockEntity> getSubTiles();
}
