package ic2.core.block.generator.tileentity;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.item.ElectricItem;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.block.TileEntityLiquidTankInventory;
import ic2.core.block.generator.container.ContainerGeoGenerator;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotCharge;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.mutable.MutableObject;

/** 20 EU/t from 2 mB lava per tick, 8 bucket tank, 24000 EU buffer. */
public class TileEntityGeoGenerator extends TileEntityLiquidTankInventory implements IEnergySource, IHasGui {
    public final InvSlotCharge chargeSlot = new InvSlotCharge(this, 1);
    public final InvSlotConsumableLiquid fluidSlot = new InvSlotConsumableLiquid(this, "fluidSlot", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, Fluids.LAVA);
    public final InvSlotOutput outputSlot = new InvSlotOutput(this, "output", 1);
    public double storage;
    public final short maxStorage = 24000;
    public final int production = 20;
    public boolean addedToEnergyNet;
    public AudioSource audioSource;
    private boolean lastActiveClient;

    public TileEntityGeoGenerator(BlockPos pos, BlockState state) { super(Ic2BlockEntities.GEOTHERMAL_GENERATOR.get(), pos, state, 8); }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); storage = nbt.getDouble("storage"); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putDouble("storage", storage); }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        if (needsFluid()) {
            MutableObject<ItemStack> output = new MutableObject<>();
            if (fluidSlot.transferToTank(fluidTank, output, true) && (output.getValue() == null || outputSlot.canAdd(output.getValue()))) {
                needsInvUpdate = fluidSlot.transferToTank(fluidTank, output, false);
                if (output.getValue() != null) outputSlot.add(output.getValue());
            }
        }
        boolean newActive = gainEnergy();
        if (storage > maxStorage) storage = maxStorage;
        if (storage >= 1 && !chargeSlot.get().isEmpty()) {
            double used = ElectricItem.manager.charge(chargeSlot.get(), storage, 1, false, false);
            storage -= used;
            if (used > 0) needsInvUpdate = true;
        }
        if (needsInvUpdate) setChanged();
        if (getActive() != newActive) setActive(newActive);
    }

    @Override
    protected void updateEntityClient() {
        boolean active = getActive();
        if (active == lastActiveClient) return;
        lastActiveClient = active;
        if (audioSource == null) audioSource = AudioSource.create(this, Ic2Sounds.GEOTHERMAL_LOOP.get());
        if (audioSource != null) { if (active) audioSource.play(); else audioSource.stop(); }
    }

    @Override public void onLoaded() { super.onLoaded(); EnergyNet.instance.addTile(this); addedToEnergyNet = true; }
    @Override public void onUnloaded() {
        if (addedToEnergyNet && level != null && !level.isClientSide) { EnergyNet.instance.removeTile(this); addedToEnergyNet = false; }
        if (audioSource != null) { audioSource.remove(); audioSource = null; }
        super.onUnloaded();
    }

    @Override public boolean emitsEnergyTo(BlockEntity receiver, Direction direction) { return true; }
    @Override public double getOfferedEnergy() { return Math.min(storage, EnergyNet.instance.getPowerFromTier(getSourceTier())); }
    @Override public int getSourceTier() { return 1; }
    @Override public void drawEnergy(double amount) { storage -= amount; }
    @Override public float getWrenchDropRate() { return 0.9f; }

    public boolean gainEnergy() {
        if (!isConverting()) return false;
        storage += production;
        fluidTank.drain(2, IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    @Override public boolean canFill(Direction from, Fluid fluid) { return fluid == Fluids.LAVA; }
    @Override public boolean canDrain(Direction from, Fluid fluid) { return false; }
    public boolean isConverting() { return getTankAmount() > 0 && storage + production <= maxStorage; }
    public int gaugeStorageScaled(int i) { return (int)(storage * i / maxStorage); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerGeoGenerator(id, inventory, this); }
}
