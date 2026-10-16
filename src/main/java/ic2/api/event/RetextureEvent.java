package ic2.api.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Posted when an obscurator is used on the side of a block: the side should look like the referencedSide face of
 * referencedState. Set applied when a handler retextured it.
 */
public class RetextureEvent extends Event implements ICancellableEvent {
    public final Level level;
    public final BlockPos pos;
    public final Direction side;
    public final BlockState referencedState;
    public final Direction referencedSide;
    public boolean applied;

    public RetextureEvent(Level level, BlockPos pos, Direction side, BlockState referencedState, Direction referencedSide) {
        this.level = level;
        this.pos = pos;
        this.side = side;
        this.referencedState = referencedState;
        this.referencedSide = referencedSide;
    }
}
