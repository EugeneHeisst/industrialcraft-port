package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.tile.IEnergyStorage;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotCharge;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.ref.Ic2Components;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** BatBox, CESU, MFE and MFSU: output on the front face, input on all others. */
public abstract class TileEntityElectricBlock extends TileEntityInventory implements IEnergySink, IEnergySource, IHasGui, IEnergyStorage {
    public static final float energyRetainedInStorageBlockDrops = 0.8f;
    public static final byte redstoneModes = 7;
    protected byte redstoneModeCount() { return redstoneModes; }
    public final int tier, output, maxStorage;
    public double energy;
    public boolean hasRedstone;
    public byte redstoneMode;
    private boolean isEmittingRedstone;
    private int redstoneUpdateInhibit = 5;
    public boolean addedToEnergyNet;
    public final InvSlotCharge chargeSlot;
    public final InvSlotDischarge dischargeSlot;

    protected TileEntityElectricBlock(BlockEntityType<?> type, BlockPos pos, BlockState state, int tier, int output, int maxStorage) {
        super(type, pos, state);
        this.tier = tier;
        this.output = output;
        this.maxStorage = maxStorage;
        chargeSlot = new InvSlotCharge(this, tier);
        dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.IO, tier, InvSlot.InvSide.BOTTOM);
    }

    public float getChargeLevel() { return Math.min(1, (float)energy / maxStorage); }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) {
        super.readFromNBT(nbt, r);
        energy = Util.limit(nbt.getDouble("energy"), 0, maxStorage + EnergyNet.instance.getPowerFromTier(tier));
        redstoneMode = nbt.getByte("redstoneMode");
    }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putDouble("energy", energy); nbt.putByte("redstoneMode", redstoneMode); }

    @Override public void onLoaded() { super.onLoaded(); EnergyNet.instance.addTile(this); addedToEnergyNet = true; }
    @Override public void onUnloaded() {
        if (addedToEnergyNet && level != null && !level.isClientSide) { EnergyNet.instance.removeTile(this); addedToEnergyNet = false; }
        super.onUnloaded();
    }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        if (energy >= 1) { double sent = chargeSlot.charge(energy); energy -= sent; needsInvUpdate = sent > 0; }
        if (getDemandedEnergy() > 0 && !dischargeSlot.isEmpty()) { double gain = dischargeSlot.discharge(maxStorage - energy, false); energy += gain; needsInvUpdate = gain > 0; }
        updateRedstone();
        if (needsInvUpdate) setChanged();
    }

    protected void updateRedstone() {
        if (redstoneMode == 5 || redstoneMode == 6) hasRedstone = level.hasNeighborSignal(worldPosition);
        boolean shouldEmit = shouldEmitRedstone();
        if (shouldEmit != isEmittingRedstone) {
            isEmittingRedstone = shouldEmit;
            setActive(isEmittingRedstone);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    @Override public boolean acceptsEnergyFrom(BlockEntity emitter, Direction direction) { return direction != getFacing(); }
    @Override public boolean emitsEnergyTo(BlockEntity receiver, Direction direction) { return direction == getFacing(); }

    @Override
    public double getOfferedEnergy() {
        if (energy < output || redstoneMode == 5 && hasRedstone || redstoneMode == 6 && hasRedstone && energy < maxStorage) return 0;
        return Math.min(energy, output);
    }
    @Override public void drawEnergy(double amount) { energy -= amount; }
    @Override public double getDemandedEnergy() { return maxStorage - energy; }
    @Override public double injectEnergy(Direction from, double amount, double voltage) { if (energy >= maxStorage) return amount; energy += amount; return 0; }
    @Override public int getSourceTier() { return tier; }
    @Override public int getSinkTier() { return tier; }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerElectricBlock(id, inventory, this); }
    @Override public boolean wrenchCanSetFacing(Player player, Direction side) { return getFacing() != side; }

    @Override
    public void setFacing(Direction facing) {
        if (addedToEnergyNet) EnergyNet.instance.removeTile(this);
        super.setFacing(facing);
        if (addedToEnergyNet) EnergyNet.instance.addTile(this);
    }

    public boolean isEmittingRedstone() { return isEmittingRedstone; }

    public boolean shouldEmitRedstone() {
        boolean should = switch (redstoneMode) {
            case 1 -> energy >= maxStorage - output * 20;
            case 2 -> energy > output && energy < maxStorage;
            case 3 -> energy > output && energy < maxStorage || energy < output;
            case 4 -> energy < output;
            default -> false;
        };
        if (isEmittingRedstone != should && redstoneUpdateInhibit != 0) { redstoneUpdateInhibit--; return isEmittingRedstone; }
        redstoneUpdateInhibit = 5;
        return should;
    }

    /** GUI redstone button. */
    public void cycleRedstoneMode(Player player) {
        redstoneMode++;
        if (redstoneMode >= redstoneModeCount()) redstoneMode = 0;
        player.displayClientMessage(getRedstoneMode(), false);
    }

    public Component getRedstoneMode() { return redstoneMode >= 0 && redstoneMode <= 6 ? Component.translatable("ic2.EUStorage.gui.mod.redstone" + redstoneMode) : Component.empty(); }

    @Override
    public ItemStack getWrenchDrop(Player player) {
        ItemStack ret = super.getWrenchDrop(player);
        if (energyRetainedInStorageBlockDrops > 0) ret.set(Ic2Components.ENERGY.get(), energy * energyRetainedInStorageBlockDrops);
        return ret;
    }

    @Override public int getStored() { return (int)energy; }
    @Override public int getCapacity() { return maxStorage; }
    @Override public int getOutput() { return output; }
    @Override public double getOutputEnergyUnitsPerTick() { return output; }
    @Override public void setStored(int energy) { this.energy = energy; }
    @Override public int addEnergy(int amount) { energy += amount; return amount; }
    @Override public boolean isTeleporterCompatible(Direction side) { return true; }

    public static class BatBox extends TileEntityElectricBlock { public BatBox(BlockPos p, BlockState s) { super(ic2.core.ref.Ic2BlockEntities.BATBOX.get(), p, s, 1, 32, 40000); } }
    public static class CESU extends TileEntityElectricBlock { public CESU(BlockPos p, BlockState s) { super(ic2.core.ref.Ic2BlockEntities.CESU.get(), p, s, 2, 128, 300000); } }
    public static class MFE extends TileEntityElectricBlock { public MFE(BlockPos p, BlockState s) { super(ic2.core.ref.Ic2BlockEntities.MFE.get(), p, s, 3, 512, 4000000); } }
    public static class MFSU extends TileEntityElectricBlock { public MFSU(BlockPos p, BlockState s) { super(ic2.core.ref.Ic2BlockEntities.MFSU.get(), p, s, 4, 2048, 40000000); } }
}
