package ic2.core.block.machine.tileentity;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotDischarge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityElectricMachine extends TileEntityInventory implements IEnergySink {
    public double energy;
    public int maxEnergy;
    private boolean addedToEnergyNet;
    private int tier;
    public float guiChargeLevel;
    public final InvSlotDischarge dischargeSlot;

    protected TileEntityElectricMachine(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxEnergy, int tier) {
        super(type, pos, state);
        this.maxEnergy = maxEnergy;
        this.tier = tier;
        dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, tier);
    }

    protected TileEntityElectricMachine(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxEnergy, int tier, boolean allowRedstone) {
        super(type, pos, state);
        this.maxEnergy = maxEnergy;
        this.tier = tier;
        dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, tier, allowRedstone, InvSlot.InvSide.ANY);
    }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider registries) { super.readFromNBT(nbt, registries); energy = nbt.getDouble("energy"); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider registries) { super.writeToNBT(nbt, registries); nbt.putDouble("energy", energy); }

    @Override
    public void onLoaded() {
        super.onLoaded();
        EnergyNet.instance.addTile(this);
        addedToEnergyNet = true;
    }

    @Override
    public void onUnloaded() {
        if (addedToEnergyNet && level != null && !level.isClientSide) { EnergyNet.instance.removeTile(this); addedToEnergyNet = false; }
        super.onUnloaded();
    }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (maxEnergy - energy >= 1) {
            double amount = dischargeSlot.discharge(maxEnergy - energy, false);
            if (amount > 0) { energy += amount; setChanged(); }
        }
        guiChargeLevel = Math.min(1, (float)energy / maxEnergy);
    }

    public float getChargeLevel() { return guiChargeLevel; }
    @Override public double getDemandedEnergy() { return maxEnergy - energy; }
    @Override public double injectEnergy(Direction directionFrom, double amount, double voltage) {
        if (energy >= maxEnergy) return amount;
        energy += amount;
        return 0;
    }
    @Override public int getSinkTier() { return tier; }
    @Override public boolean acceptsEnergyFrom(BlockEntity emitter, Direction direction) { return true; }

    public void setTier(int tier) {
        if (this.tier == tier) return;
        this.tier = tier;
        dischargeSlot.setTier(tier);
    }
    public int getTier() { return tier; }
}
