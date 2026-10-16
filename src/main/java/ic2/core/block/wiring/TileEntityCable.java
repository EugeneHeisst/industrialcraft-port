package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.block.TileEntityBlock;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCable extends TileEntityBlock implements IEnergyConductor {
    public boolean addedToEnergyNet;

    public TileEntityCable(BlockPos pos, BlockState state) { super(Ic2BlockEntities.CABLE.get(), pos, state); }
    protected TileEntityCable(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    public CableType getCableType() { return ((BlockCable)getBlockState().getBlock()).type; }
    public int getColor() { return getBlockState().getValue(BlockCable.COLOR); }

    @Override
    public void onLoaded() {
        super.onLoaded();
        EnergyNet.instance.addTile(this);
        addedToEnergyNet = true;
        onNeighborBlockChange();
    }

    @Override
    public void onUnloaded() {
        if (addedToEnergyNet && level != null && !level.isClientSide) { EnergyNet.instance.removeTile(this); addedToEnergyNet = false; }
        super.onUnloaded();
    }

    @Override public void onNeighborUpdate(BlockPos from) { if (level != null && !level.isClientSide && isLoaded()) onNeighborBlockChange(); }

    /** Connects to every registered energy tile that accepts or emits towards this cable. */
    public void onNeighborBlockChange() {
        BlockState state = getBlockState();
        BlockState next = state;
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = EnergyNet.instance.getNeighbor(this, dir);
            boolean connect = (neighbor instanceof IEnergyAcceptor a && a.acceptsEnergyFrom(this, dir.getOpposite())
                || neighbor instanceof IEnergyEmitter e && e.emitsEnergyTo(this, dir.getOpposite())) && canInteractWith(neighbor);
            next = next.setValue(BlockCable.PROPS.get(dir), connect);
        }
        if (next != state) level.setBlock(worldPosition, next, 3);
    }

    /** Painter: recolours insulation and reconnects. */
    public boolean changeColor(int newColor) {
        if (!getCableType().colorable || getColor() == newColor) return false;
        if (level.isClientSide) return true;
        if (addedToEnergyNet) EnergyNet.instance.removeTile(this);
        level.setBlock(worldPosition, getBlockState().setValue(BlockCable.COLOR, newColor), 3);
        EnergyNet.instance.addTile(this);
        addedToEnergyNet = true;
        onNeighborBlockChange();
        return true;
    }

    /** Rubber / cutter: swap to the (un)insulated block keeping the colour-less state. */
    public boolean changeType(CableType target) {
        if (target == null) return false;
        if (level.isClientSide) return true;
        if (addedToEnergyNet) EnergyNet.instance.removeTile(this);
        addedToEnergyNet = false;
        level.setBlock(worldPosition, BlockCable.byType(target).defaultBlockState(), 3);
        return true;
    }

    @Override public boolean acceptsEnergyFrom(BlockEntity emitter, Direction direction) { return canInteractWith(emitter); }
    @Override public boolean emitsEnergyTo(BlockEntity receiver, Direction direction) { return canInteractWith(receiver); }

    public boolean canInteractWith(BlockEntity te) {
        if (!(te instanceof IEnergyTile)) return false;
        if (te instanceof TileEntityCable cable) return getColor() == 0 || cable.getColor() == 0 || getColor() == cable.getColor();
        return true;
    }

    @Override public double getConductionLoss() { return getCableType().loss; }
    @Override public double getInsulationEnergyAbsorption() { return getCableType().insulationAbsorption; }
    @Override public double getInsulationBreakdownEnergy() { return 9001; }
    @Override public double getConductorBreakdownEnergy() { return getCableType().capacity + 1; }
    @Override public void removeInsulation() {}
    @Override public void removeConductor() { level.removeBlock(worldPosition, false); }
    @Override public boolean wrenchCanSetFacing(Player player, Direction side) { return false; }
    @Override public boolean wrenchCanRemove(Player player) { return false; }
    @Override public float getWrenchDropRate() { return 0; }
}
