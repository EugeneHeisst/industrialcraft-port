package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Emits a redstone signal while energy flows through it (checked once per second). */
public class TileEntityCableDetector extends TileEntityCable {
    public static int tickRate = 20;
    public int ticker;
    public TileEntityCableDetector(BlockPos pos, BlockState state) { super(Ic2BlockEntities.DETECTOR_CABLE.get(), pos, state); }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (ticker++ % tickRate != 0) return;
        boolean flowing = EnergyNet.instance.getNodeStats(this).getEnergyIn() > 0;
        if (flowing != getActive()) {
            setActive(flowing);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }
}
