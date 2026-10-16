package ic2.core.block.heatgenerator;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityHeatSourceInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableFuel;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class TileEntityHeatGenerators {
    private TileEntityHeatGenerators() {}

    /** 20 hU/t from furnace fuel (burn time / 4 ticks), sometimes leaving ashes. */
    public static class Solid extends TileEntityHeatSourceInventory implements IHasGui {
        private int heatbuffer;
        public int fuel, itemFuelTime;
        public static final int emittedHU = 20;
        public final InvSlotConsumableFuel fuelSlot = new InvSlotConsumableFuel(this, "fuel", 1, false);
        public final InvSlotOutput outputslot = new InvSlotOutput(this, "output", 1);

        public Solid(BlockPos pos, BlockState state) { super(Ic2BlockEntities.SOLID_HEAT_GENERATOR.get(), pos, state); }

        @Override
        protected void updateEntityServer() {
            super.updateEntityServer();
            boolean needsInvUpdate = false;
            if (fuel <= 0 && HeatBuffer == 0) needsInvUpdate = gainFuel();
            boolean newActive = gainheat();
            if (needsInvUpdate) setChanged();
            setActive(newActive);
        }

        public boolean gainheat() {
            if (fuel <= 0) return false;
            heatbuffer += getMaxHeatEmittedPerTick();
            fuel--;
            if (fuel == 0 && level.random.nextInt(2) == 1) outputslot.add(new ItemStack(Ic2Items.get("ashes")));
            return true;
        }

        public boolean gainFuel() {
            if (!outputslot.canAdd(new ItemStack(Ic2Items.get("ashes")))) return false;
            int fuelValue = fuelSlot.consumeFuel() / 4;
            if (fuelValue == 0) return false;
            fuel += fuelValue;
            itemFuelTime = fuelValue;
            return true;
        }

        public int gaugeFuelScaled(int i) {
            if (fuel <= 0) return 0;
            if (itemFuelTime <= 0) itemFuelTime = fuel;
            return Math.min(fuel * i / itemFuelTime, i);
        }

        @Override protected int fillHeatBuffer(int maxAmount) { int ret = Math.min(maxAmount, heatbuffer); heatbuffer -= ret; return ret; }
        @Override public int getMaxHeatEmittedPerTick() { return emittedHU; }
        @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); fuel = nbt.getInt("fuel"); }
        @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putInt("fuel", fuel); }
        @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerHeatGenerators.Solid(id, inv, this); }
    }

    /** RTG pellets: 2^(n-1) x 2 hU/t for n pellets. */
    public static class RT extends TileEntityHeatSourceInventory implements IHasGui {
        public static final float outputMultiplier = 2;
        public final InvSlotConsumable fuelSlot = new InvSlotConsumable(this, "fuelSlot", 6) { @Override public boolean accepts(ItemStack s) { return s.is(Ic2Items.get("rtg_pellet")); } };

        public RT(BlockPos pos, BlockState state) { super(Ic2BlockEntities.RT_HEAT_GENERATOR.get(), pos, state); }

        @Override protected void updateEntityServer() { super.updateEntityServer(); setActive(HeatBuffer > 0); }
        @Override protected int fillHeatBuffer(int maxAmount) { return Math.min(maxAmount, getMaxHeatEmittedPerTick()); }
        @Override public int getMaxHeatEmittedPerTick() {
            int counter = 0;
            for (int i = 0; i < fuelSlot.size(); i++) if (!fuelSlot.get(i).isEmpty()) counter++;
            return counter == 0 ? 0 : (int)(Math.pow(2, counter - 1) * outputMultiplier);
        }
        @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerHeatGenerators.RT(id, inv, this); }
    }

    /** 1 EU per hU, 10 hU/t per coil (up to 10 coils), 10000 EU buffer, tier 4 sink. */
    public static class Electric extends TileEntityHeatSourceInventory implements IHasGui, IEnergySink {
        public final InvSlotConsumable CoilSlot = new InvSlotConsumable(this, "CoilSlot", 10) { @Override public boolean accepts(ItemStack s) { return s.is(Ic2Items.get("coil")); } };
        public final InvSlotDischarge dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, 4);
        public double energy;
        public final double capacity = 10000;
        private boolean newActive, addedToEnergyNet;

        public Electric(BlockPos pos, BlockState state) { super(Ic2BlockEntities.ELECTRIC_HEAT_GENERATOR.get(), pos, state); CoilSlot.setStackSizeLimit(1); }

        @Override
        protected void updateEntityServer() {
            super.updateEntityServer();
            if (capacity - energy >= 1 && !dischargeSlot.isEmpty()) energy += dischargeSlot.discharge(capacity - energy, false);
            if (getActive() != newActive) setActive(newActive);
        }

        @Override
        protected int fillHeatBuffer(int maxAmount) {
            int amount = Math.min(maxAmount, (int)energy);
            if (amount > 0) { energy -= amount; newActive = true; } else newActive = false;
            return amount;
        }

        @Override public int getMaxHeatEmittedPerTick() { int c = 0; for (int i = 0; i < CoilSlot.size(); i++) if (!CoilSlot.get(i).isEmpty()) c++; return c * 10; }
        public float getChargeLevel() { return (float)Math.min(1, energy / capacity); }
        @Override public void onLoaded() { super.onLoaded(); EnergyNet.instance.addTile(this); addedToEnergyNet = true; }
        @Override public void onUnloaded() { if (addedToEnergyNet && level != null && !level.isClientSide) { EnergyNet.instance.removeTile(this); addedToEnergyNet = false; } super.onUnloaded(); }
        @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); energy = nbt.getDouble("energy"); }
        @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putDouble("energy", energy); }
        @Override public double getDemandedEnergy() { return capacity - energy; }
        @Override public int getSinkTier() { return 4; }
        @Override public double injectEnergy(Direction from, double amount, double voltage) { if (energy >= capacity) return amount; energy += amount; return 0; }
        @Override public boolean acceptsEnergyFrom(BlockEntity emitter, Direction direction) { return true; }
        @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerHeatGenerators.Electric(id, inv, this); }
    }
}
