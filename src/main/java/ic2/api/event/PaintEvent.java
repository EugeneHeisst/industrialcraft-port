package ic2.api.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

/** Posted when a painter is used on a block; set painted when a handler recoloured it. color is the dye index 0..15. */
public class PaintEvent extends Event {
    public final Level level;
    public final BlockPos pos;
    public final Direction side;
    public final int color;
    public boolean painted;
    public PaintEvent(Level level, BlockPos pos, Direction side, int color) { this.level = level; this.pos = pos; this.side = side; this.color = color; }
}
