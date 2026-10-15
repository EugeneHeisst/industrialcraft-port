package ic2.api.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IEnergyNet {
    BlockEntity getTileEntity(Level level, BlockPos pos);
    BlockEntity getNeighbor(BlockEntity te, Direction dir);
    NodeStats getNodeStats(BlockEntity te);
    double getPowerFromTier(int tier);
    int getTierFromPower(double power);
    /** Same as posting {@link ic2.api.energy.event.EnergyTileLoadEvent}; call on the server once the tile is in the level. */
    void addTile(BlockEntity te);
    /** Same as posting {@link ic2.api.energy.event.EnergyTileUnloadEvent}. */
    void removeTile(BlockEntity te);
}
