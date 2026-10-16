package ic2.core.block.reactor;

import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityBlock;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Reactor chamber: adds a grid column, shares the reactor's GUI, inventory, fluids and energy output; redstone on it powers the reactor. */
public class TileEntityReactorChamber extends TileEntityBlock implements IHasGui, IReactorChamber, IEnergyEmitter {
    private boolean redpowered;
    private int ticker;

    public TileEntityReactorChamber(BlockPos pos, BlockState state) { super(Ic2BlockEntities.REACTOR_CHAMBER.get(), pos, state); }

    @Override public void onLoaded() { super.onLoaded(); if (!level.isClientSide) { var r = getReactor(); if (r != null) r.refreshChambers(); } }
    @Override public void onUnloaded() { if (level != null && !level.isClientSide && level.getServer() != null && level.getServer().isRunning()) { var r = getReactor(); if (r != null) r.refreshChambers(); } super.onUnloaded(); }

    @Override
    protected void updateEntityServer() {
        if (++ticker < 20) return;
        ticker = 0;
        boolean powered = level.hasNeighborSignal(worldPosition);
        if (powered != redpowered) { redpowered = powered; setRedstoneSignal(powered); }
    }

    @Override public boolean emitsEnergyTo(BlockEntity receiver, Direction direction) { return true; }

    @Override
    public TileEntityNuclearReactor getReactor() {
        if (level == null) return null;
        for (Direction d : Direction.values()) { BlockPos p = worldPosition.relative(d); if (level.isLoaded(p) && level.getBlockEntity(p) instanceof TileEntityNuclearReactor r && !r.isRemoved()) return r; }
        return null;
    }

    @Override public void setRedstoneSignal(boolean redstone) { var r = getReactor(); if (r != null) r.setRedstoneSignal(redstone); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { var r = getReactor(); return r == null ? null : r.getGuiContainer(id, inv); }
    @Override public float getWrenchDropRate() { return 0.8f; }
}
