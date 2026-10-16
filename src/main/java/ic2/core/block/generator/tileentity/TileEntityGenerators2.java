package ic2.core.block.generator.tileentity;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IHeatSource;
import ic2.api.energy.tile.IKineticSource;
import ic2.api.item.ElectricItem;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.TileEntityLiquidTankInventory;
import ic2.core.block.generator.container.ContainerGenerators2;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotCharge;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.fluid.Ic2Fluids;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2Sounds;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.mutable.MutableObject;

public final class TileEntityGenerators2 {
    private TileEntityGenerators2() {}

    /** Radioisotope generator: 2^(n-1) EU/t for n RTG pellets. */
    public static class RTG extends TileEntityBaseGenerator {
        public final InvSlotConsumable fuelSlot = new InvSlotConsumable(this, "fuelSlot", 6) { @Override public boolean accepts(ItemStack s) { return s.is(Ic2Items.get("rtg_pellet")); } };
        public RTG(BlockPos pos, BlockState state) { super(Ic2BlockEntities.RT_GENERATOR.get(), pos, state, 16, 1, 20000); }
        @Override public int gaugeFuelScaled(int i) { return i; }
        @Override public boolean gainEnergy() {
            int counter = 0;
            for (int i = 0; i < fuelSlot.size(); i++) if (!fuelSlot.get(i).isEmpty()) counter++;
            if (counter == 0) return false;
            storage += (int)Math.pow(2, counter - 1);
            return true;
        }
        @Override public boolean gainFuel() { return false; }
        @Override public boolean needsFuel() { return true; }
        @Override public boolean delayActiveUpdate() { return true; }
        @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerGenerators2.RTG(id, inv, this); }
    }

    /** Semi fluid fuels: mB burnt per 20 ticks and EU/t (Recipes.semiFluidGenerator of the original). */
    public record BurnProperty(int amount, double power) {}
    public static final Map<Object, BurnProperty> SEMIFLUID_FUELS = new LinkedHashMap<>();
    static {
        SEMIFLUID_FUELS.put(tag("c:oil"), new BurnProperty(10, 8));
        SEMIFLUID_FUELS.put(tag("c:fuel"), new BurnProperty(5, 32));
        SEMIFLUID_FUELS.put(tag("c:biomass"), new BurnProperty(20, 8));
        SEMIFLUID_FUELS.put(tag("c:ethanol"), new BurnProperty(10, 16));
        SEMIFLUID_FUELS.put("ic2:biogas", new BurnProperty(10, 16));
    }
    private static TagKey<Fluid> tag(String id) { return TagKey.create(Registries.FLUID, ResourceLocation.parse(id)); }

    @SuppressWarnings("unchecked")
    public static BurnProperty getBurnProperty(Fluid fluid) {
        for (var e : SEMIFLUID_FUELS.entrySet()) {
            if (e.getKey() instanceof TagKey<?> t && fluid.defaultFluidState().is((TagKey<Fluid>)t)) return e.getValue();
            if (e.getKey() instanceof String s && net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluid).toString().equals(s)) return e.getValue();
        }
        return null;
    }

    public static class Semifluid extends TileEntityLiquidTankInventory implements IEnergySource, IHasGui {
        public final InvSlotCharge chargeSlot = new InvSlotCharge(this, 1);
        public final InvSlotConsumableLiquid fluidSlot = new InvSlotConsumableLiquid(this, "fluidSlot", InvSlot.Access.I, 1, InvSlot.InvSide.TOP) {
            @Override public boolean accepts(ItemStack stack) { FluidStack f = contained(stack); return !f.isEmpty() && getBurnProperty(f.getFluid()) != null; }
        };
        public final InvSlotOutput outputSlot = new InvSlotOutput(this, "output", 1);
        private short ticker;
        protected int burnAmount;
        protected final short maxStorage = 32000;
        public double storage, production;
        protected boolean addedToEnergyNet;
        private AudioSource audioSource;
        private boolean lastActiveClient;

        public Semifluid(BlockPos pos, BlockState state) { super(Ic2BlockEntities.SEMIFLUID_GENERATOR.get(), pos, state, 10); }

        @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); storage = nbt.getDouble("storage"); }
        @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putDouble("storage", storage); }

        @Override
        protected void updateEntityServer() {
            super.updateEntityServer();
            boolean needsInvUpdate = false;
            if (needsFluid()) needsInvUpdate = gainFuel();
            boolean newActive = gainEnergy();
            if (storage > maxStorage) storage = maxStorage;
            if (storage >= 1 && !chargeSlot.get().isEmpty()) { double used = ElectricItem.manager.charge(chargeSlot.get(), storage, 1, false, false); storage -= used; if (used > 0) needsInvUpdate = true; }
            if (needsInvUpdate) setChanged();
            if (getActive() != newActive) setActive(newActive);
        }

        @Override protected void updateEntityClient() {
            boolean active = getActive();
            if (active == lastActiveClient) return;
            lastActiveClient = active;
            if (audioSource == null) audioSource = AudioSource.create(this, Ic2Sounds.GEOTHERMAL_LOOP.get());
            if (audioSource != null) { if (active) audioSource.play(); else audioSource.stop(); }
        }

        protected boolean gainEnergy() {
            if (!isConverting()) return false;
            storage += production;
            if (ticker >= 19) { fluidTank.drain(burnAmount, IFluidHandler.FluidAction.EXECUTE); ticker = 0; } else ticker++;
            return true;
        }

        public boolean isConverting() { return getTankAmount() > 0 && storage + production <= maxStorage; }

        protected boolean gainFuel() {
            if (!fluidTank.isEmpty()) {
                BurnProperty p = getBurnProperty(fluidTank.getFluid().getFluid());
                production = p == null ? 0 : p.power;
                burnAmount = p == null ? 0 : p.amount;
            }
            MutableObject<ItemStack> output = new MutableObject<>();
            if (fluidSlot.transferToTank(fluidTank, output, true) && (output.getValue() == null || outputSlot.canAdd(output.getValue()))) {
                boolean ret = fluidSlot.transferToTank(fluidTank, output, false);
                if (output.getValue() != null) outputSlot.add(output.getValue());
                return ret;
            }
            return false;
        }

        @Override public boolean canFill(Direction from, Fluid fluid) {
            if (getBurnProperty(fluid) == null) return false;
            return fluidTank.isEmpty() || fluidTank.getFluid().getFluid() == fluid && getTankAmount() < fluidTank.getCapacity();
        }
        @Override public boolean canDrain(Direction from, Fluid fluid) { return false; }
        public int gaugeStorageScaled(int i) { return (int)(storage * i / maxStorage); }
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
        @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerGenerators2.Semifluid(id, inv, this); }
    }

    /** Pulls heat from the heat source it faces: 0.5 EU per hU, 30000 EU buffer, tier 2 output. */
    public static class Stirling extends TileEntityInventory implements IEnergySource, IHasGui {
        public double production, EUstorage;
        public int receivedheat;
        public final short maxEUStorage = 30000;
        public final double productionpeerheat = 0.5;
        public boolean addedToEnergyNet;

        public Stirling(BlockPos pos, BlockState state) { super(Ic2BlockEntities.STIRLING_GENERATOR.get(), pos, state); }

        @Override protected void updateEntityServer() {
            super.updateEntityServer();
            boolean newActive = gainEnergy();
            if (EUstorage > maxEUStorage) EUstorage = maxEUStorage;
            if (getActive() != newActive) setActive(newActive);
        }

        protected boolean gainEnergy() {
            Direction dir = getFacing();
            if (ic2.core.util.Util.loadedBlockEntity(level, worldPosition.relative(dir)) instanceof IHeatSource source) {
                int band = source.maxrequestHeatTick(dir.getOpposite());
                if (maxEUStorage - EUstorage >= productionpeerheat * band) {
                    receivedheat = source.requestHeat(dir.getOpposite(), band);
                    if (receivedheat != 0) { production = receivedheat * productionpeerheat; EUstorage += production; return true; }
                }
            }
            production = 0;
            receivedheat = 0;
            return false;
        }

        @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); EUstorage = nbt.getDouble("EUstorage"); }
        @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putDouble("EUstorage", EUstorage); }
        @Override public boolean emitsEnergyTo(BlockEntity receiver, Direction direction) { return true; }
        @Override public double getOfferedEnergy() { return Math.min(EUstorage, EnergyNet.instance.getPowerFromTier(getSourceTier())); }
        @Override public int getSourceTier() { return 2; }
        @Override public void drawEnergy(double amount) { EUstorage -= amount; }
        public int gaugeEUStorageScaled(int i) { return (int)(EUstorage * i / maxEUStorage); }
        @Override public boolean wrenchCanSetFacing(Player player, Direction side) { return getFacing() != side; }
        @Override public void setFacing(Direction facing) { super.setFacing(facing); }
        @Override public void onLoaded() { super.onLoaded(); EnergyNet.instance.addTile(this); addedToEnergyNet = true; }
        @Override public void onUnloaded() { if (addedToEnergyNet && level != null && !level.isClientSide) { EnergyNet.instance.removeTile(this); addedToEnergyNet = false; } super.onUnloaded(); }
        @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerGenerators2.Stirling(id, inv, this); }
    }

    /** Pulls kinetic energy from the kinetic source it faces: 0.25 EU per kU, 200000 EU buffer, tier 3 output. */
    public static class Kinetic extends TileEntityInventory implements IEnergySource, IHasGui {
        public int updateTicker = (int)(Math.random() * 20);
        public double guiproduction, production, EUstorage;
        private int receivedkinetic;
        private final int maxEUStorage = 200000;
        private final double productionpeerkineticunit = 0.25;
        public boolean addedToEnergyNet;

        public Kinetic(BlockPos pos, BlockState state) { super(Ic2BlockEntities.KINETIC_GENERATOR.get(), pos, state); }

        @Override protected void updateEntityServer() {
            super.updateEntityServer();
            boolean newActive = gainEnergy();
            if (updateTicker++ >= 20) { guiproduction = production; updateTicker = 0; }
            if (EUstorage > maxEUStorage) EUstorage = maxEUStorage;
            if (getActive() != newActive) setActive(newActive);
        }

        protected boolean gainEnergy() {
            Direction dir = getFacing();
            if (ic2.core.util.Util.loadedBlockEntity(level, worldPosition.relative(dir)) instanceof IKineticSource source) {
                int band = source.maxrequestkineticenergyTick(dir.getOpposite());
                double free = maxEUStorage - EUstorage;
                if (free > 0 && free < productionpeerkineticunit * band) free = productionpeerkineticunit * band;
                if (free >= productionpeerkineticunit * band) {
                    receivedkinetic = source.requestkineticenergy(dir.getOpposite(), band);
                    if (receivedkinetic != 0) { production = receivedkinetic * productionpeerkineticunit; EUstorage += production; return true; }
                }
            }
            production = 0;
            receivedkinetic = 0;
            return false;
        }

        public double getproduction() { return guiproduction; }
        @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); EUstorage = nbt.getDouble("EUstorage"); }
        @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putDouble("EUstorage", EUstorage); }
        @Override public boolean emitsEnergyTo(BlockEntity receiver, Direction direction) { return true; }
        @Override public double getOfferedEnergy() { return Math.min(EUstorage, EnergyNet.instance.getPowerFromTier(getSourceTier())); }
        @Override public int getSourceTier() { return 3; }
        @Override public void drawEnergy(double amount) { EUstorage -= amount; }
        public int gaugeEUStorageScaled(int i) { return (int)(EUstorage * i / maxEUStorage); }
        @Override public boolean wrenchCanSetFacing(Player player, Direction side) { return getFacing() != side; }
        @Override public float getWrenchDropRate() { return 0.9f; }
        @Override public void onLoaded() { super.onLoaded(); EnergyNet.instance.addTile(this); addedToEnergyNet = true; }
        @Override public void onUnloaded() { if (addedToEnergyNet && level != null && !level.isClientSide) { EnergyNet.instance.removeTile(this); addedToEnergyNet = false; } super.onUnloaded(); }
        @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerGenerators2.Kinetic(id, inv, this); }
    }
}
