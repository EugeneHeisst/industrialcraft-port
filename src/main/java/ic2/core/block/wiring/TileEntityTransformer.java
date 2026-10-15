package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Step down: input on the front from the higher tier. With redstone (or forced step up) the direction reverses. */
public abstract class TileEntityTransformer extends TileEntityInventory implements IEnergySink, IEnergySource, IHasGui {
    public int mode;
    public final int tier;
    public double power;
    public final double maxStorage;
    public double energy;
    public boolean redstone;
    private boolean needrefresh;
    public double inputflow, outputflow;
    public boolean addedToEnergyNet;

    protected TileEntityTransformer(BlockEntityType<?> type, BlockPos pos, BlockState state, int tier) {
        super(type, pos, state);
        this.tier = tier;
        power = EnergyNet.instance.getPowerFromTier(tier);
        maxStorage = power * 8;
    }

    public String getTyp() { return switch (tier) { case 1 -> "LV"; case 2 -> "MV"; case 3 -> "HV"; case 4 -> "EV"; default -> ""; }; }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); mode = nbt.getInt("mode"); energy = nbt.getDouble("energy"); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putDouble("energy", energy); nbt.putInt("mode", mode); }

    public int getMode() { return mode; }

    /** GUI buttons 0-2 select the mode, 3 refreshes the flow display. */
    public void onNetworkEvent(int event) {
        switch (event) {
            case 0, 1, 2 -> mode = event;
            case 3 -> { outputflow = EnergyNet.instance.getPowerFromTier(getSourceTier()); inputflow = EnergyNet.instance.getPowerFromTier(getSinkTier()); needrefresh = true; }
        }
    }

    @Override public void onLoaded() { super.onLoaded(); EnergyNet.instance.addTile(this); addedToEnergyNet = true; }
    @Override public void onUnloaded() {
        if (addedToEnergyNet && level != null && !level.isClientSide) { EnergyNet.instance.removeTile(this); addedToEnergyNet = false; }
        super.onUnloaded();
    }

    @Override protected void updateEntityServer() { super.updateEntityServer(); updateRedstone(); }

    public void updateRedstone() {
        boolean red = switch (mode) { case 0 -> level.hasNeighborSignal(worldPosition); case 1 -> false; case 2 -> true; default -> throw new RuntimeException("invalid mode: " + mode); };
        if (red == redstone) return;
        if (addedToEnergyNet) EnergyNet.instance.removeTile(this);
        addedToEnergyNet = false;
        energy = 0;
        redstone = red;
        EnergyNet.instance.addTile(this);
        addedToEnergyNet = true;
        setActive(redstone);
        power = EnergyNet.instance.getPowerFromTier(getSourceTier());
    }

    @Override public boolean acceptsEnergyFrom(BlockEntity emitter, Direction direction) { return redstone != (direction == getFacing()); }
    @Override public boolean emitsEnergyTo(BlockEntity receiver, Direction direction) { return redstone == (direction == getFacing()); }
    @Override public double getOfferedEnergy() { return energy >= power ? power : 0; }
    @Override public void drawEnergy(double amount) { outputflow = amount; energy -= amount; }
    @Override public double getDemandedEnergy() { if (needrefresh) { needrefresh = false; return 1; } return maxStorage - energy; }
    @Override public double injectEnergy(Direction from, double amount, double voltage) { inputflow = amount; energy += amount; return 0; }
    @Override public int getSourceTier() { return redstone ? tier + 1 : tier; }
    @Override public int getSinkTier() { return redstone ? tier : tier < 4 ? tier + 1 : Integer.MAX_VALUE; }
    @Override public boolean wrenchCanSetFacing(Player player, Direction side) { return getFacing() != side; }

    @Override
    public void setFacing(Direction facing) {
        if (addedToEnergyNet) EnergyNet.instance.removeTile(this);
        energy = 0;
        super.setFacing(facing);
        if (addedToEnergyNet) EnergyNet.instance.addTile(this);
    }

    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerTransformer(id, inventory, this); }
    public double getinputflow() { return !redstone ? inputflow : outputflow; }
    public double getoutputflow() { return redstone ? inputflow : outputflow; }

    public static class LV extends TileEntityTransformer { public LV(BlockPos p, BlockState s) { super(Ic2BlockEntities.LV_TRANSFORMER.get(), p, s, 1); } }
    public static class MV extends TileEntityTransformer { public MV(BlockPos p, BlockState s) { super(Ic2BlockEntities.MV_TRANSFORMER.get(), p, s, 2); } }
    public static class HV extends TileEntityTransformer { public HV(BlockPos p, BlockState s) { super(Ic2BlockEntities.HV_TRANSFORMER.get(), p, s, 3); } }
    public static class EV extends TileEntityTransformer { public EV(BlockPos p, BlockState s) { super(Ic2BlockEntities.EV_TRANSFORMER.get(), p, s, 4); } }
}
