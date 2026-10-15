package ic2.api.energy.tile;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IEnergyEmitter extends IEnergyTile {
    /** @param direction side of this tile the receiver is on */
    boolean emitsEnergyTo(BlockEntity receiver, Direction direction);
}
