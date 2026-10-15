package ic2.api.energy.tile;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IEnergyAcceptor extends IEnergyTile {
    /** @param direction side of this tile the emitter is on */
    boolean acceptsEnergyFrom(BlockEntity emitter, Direction direction);
}
