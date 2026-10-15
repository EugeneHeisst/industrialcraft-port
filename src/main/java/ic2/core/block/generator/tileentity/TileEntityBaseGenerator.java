package ic2.core.block.generator.tileentity;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.item.ElectricItem;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlotCharge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityBaseGenerator extends TileEntityInventory implements IEnergySource, IHasGui {
    public int fuel;
    public double storage;
    public int tier;
    public double power;
    public final short maxStorage;
    public int production;
    public int ticksSinceLastActiveUpdate;
    public int activityMeter;
    public boolean addedToEnergyNet;
    public AudioSource audioSource;
    private boolean lastActiveClient;
    public InvSlotCharge chargeSlot;

    protected TileEntityBaseGenerator(BlockEntityType<?> type, BlockPos pos, BlockState state, int production, int tier, int maxStorage) {
        super(type, pos, state);
        this.production = production;
        this.tier = tier;
        power = EnergyNet.instance.getPowerFromTier(tier);
        this.maxStorage = (short)maxStorage;
        ticksSinceLastActiveUpdate = (int)(Math.random() * 256);
        chargeSlot = new InvSlotCharge(this, 1);
    }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); fuel = nbt.getInt("fuel"); storage = nbt.getDouble("storage"); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putInt("fuel", fuel); nbt.putDouble("storage", storage); }

    public int gaugeStorageScaled(int i) { return (int)(storage * i / maxStorage); }
    public abstract int gaugeFuelScaled(int i);

    @Override public void onLoaded() { super.onLoaded(); EnergyNet.instance.addTile(this); addedToEnergyNet = true; }

    @Override
    public void onUnloaded() {
        if (addedToEnergyNet && level != null && !level.isClientSide) { EnergyNet.instance.removeTile(this); addedToEnergyNet = false; }
        if (audioSource != null) { audioSource.remove(); audioSource = null; }
        super.onUnloaded();
    }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        if (needsFuel()) needsInvUpdate = gainFuel();
        boolean newActive = gainEnergy();
        if (storage > maxStorage) storage = maxStorage;
        if (storage >= 1 && !chargeSlot.get().isEmpty()) {
            double used = ElectricItem.manager.charge(chargeSlot.get(), storage, 1, false, false);
            storage -= used;
            if (used > 0) needsInvUpdate = true;
        }
        if (needsInvUpdate) setChanged();
        if (!delayActiveUpdate()) setActive(newActive);
        else {
            if (ticksSinceLastActiveUpdate % 256 == 0) { setActive(activityMeter > 0); activityMeter = 0; }
            activityMeter += newActive ? 1 : -1;
            ticksSinceLastActiveUpdate++;
        }
    }

    /** Operation sound follows the active flag like onNetworkUpdate("active") of the original. */
    @Override
    protected void updateEntityClient() {
        boolean active = getActive();
        if (active == lastActiveClient) return;
        lastActiveClient = active;
        if (audioSource == null && getOperationSoundFile() != null) audioSource = AudioSource.create(this, getOperationSoundFile());
        if (audioSource != null) { if (active) audioSource.play(); else audioSource.stop(); }
    }

    public boolean gainEnergy() {
        if (!isConverting()) return false;
        storage += production;
        fuel--;
        return true;
    }

    public boolean isConverting() { return fuel > 0 && storage + production <= maxStorage; }
    public boolean needsFuel() { return fuel <= 0 && storage + production <= maxStorage; }
    public abstract boolean gainFuel();
    @Override public boolean emitsEnergyTo(BlockEntity receiver, Direction direction) { return true; }
    @Override public double getOfferedEnergy() { return Math.min(storage, power); }
    @Override public int getSourceTier() { return tier; }
    @Override public void drawEnergy(double amount) { storage -= amount; }
    public SoundEvent getOperationSoundFile() { return null; }
    public boolean delayActiveUpdate() { return false; }
    @Override public float getWrenchDropRate() { return 0.9f; }
}
