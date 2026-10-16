package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Leaves the energy net while powered by redstone. */
public class TileEntityCableSplitter extends TileEntityCable {
    public static final int tickRate = 20;
    public int ticksUntilNextUpdate;
    public TileEntityCableSplitter(BlockPos pos, BlockState state) { super(Ic2BlockEntities.SPLITTER_CABLE.get(), pos, state); }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (ticksUntilNextUpdate == 0) {
            ticksUntilNextUpdate = tickRate;
            if (level.hasNeighborSignal(worldPosition) == addedToEnergyNet) {
                if (addedToEnergyNet) { EnergyNet.instance.removeTile(this); addedToEnergyNet = false; }
                else { EnergyNet.instance.addTile(this); addedToEnergyNet = true; }
            }
            setActive(addedToEnergyNet);
        }
        ticksUntilNextUpdate--;
    }
}
