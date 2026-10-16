package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IPatternStorage;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerUu;
import ic2.core.fluid.Ic2Fluids;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.upgrade.IUpgradableBlock;
import ic2.core.upgrade.IUpgradeItem;
import ic2.core.upgrade.UpgradableProperty;
import ic2.core.uu.UuGraph;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.mutable.MutableObject;

/** Replicator: rebuilds the selected pattern from UU matter, 0.1 mB and 512 EU per tick; single or continuous mode. */
public class TileEntityReplicator extends TileEntityLiquidTankElectricMachine implements IHasGui, IUpgradableBlock {
    public enum Mode { STOPPED, SINGLE, CONTINUOUS }
    private double uuPerTick = 1e-4, euPerTick = 512, extraUuStored;
    public double uuProcessed, patternUu, patternEu;
    public ItemStack pattern = ItemStack.EMPTY;
    public Mode mode = Mode.STOPPED;
    public int index, maxIndex;
    public final InvSlotConsumableLiquid fluidSlot = new InvSlotConsumableLiquid(this, "fluid", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, Ic2Fluids.UU_MATTER.get());
    public final InvSlotOutput cellSlot = new InvSlotOutput(this, "cell", 1), outputSlot = new InvSlotOutput(this, "output", 1);
    public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);

    public TileEntityReplicator(BlockPos pos, BlockState state) { super(Ic2BlockEntities.REPLICATOR.get(), pos, state, 2000000, 4, 16); }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = fluidTank.getFluidAmount() < fluidTank.getCapacity() && gainFluid();
        boolean active = false;
        if (mode != Mode.STOPPED && energy >= euPerTick && !pattern.isEmpty() && outputSlot.canAdd(pattern)) {
            double remaining = patternUu - uuProcessed;
            boolean finish = remaining <= uuPerTick;
            if (!finish) remaining = uuPerTick;
            if (consumeUu(remaining)) {
                active = true;
                energy -= euPerTick;
                uuProcessed += remaining;
                if (finish) {
                    uuProcessed = 0;
                    if (mode == Mode.SINGLE) mode = Mode.STOPPED; else refreshInfo();
                    if (!pattern.isEmpty()) { outputSlot.add(pattern.copy()); needsInvUpdate = true; }
                }
            }
        }
        setActive(active);
        for (int i = 0; i < upgradeSlot.size(); i++) { ItemStack s = upgradeSlot.get(i); if (s.getItem() instanceof IUpgradeItem u && u.onTick(s, this)) needsInvUpdate = true; }
        if (needsInvUpdate) setChanged();
    }

    private boolean consumeUu(double amount) {
        if (amount <= extraUuStored) { extraUuStored -= amount; return true; }
        amount -= extraUuStored;
        int toDrain = (int)Math.ceil(amount * 1000);
        FluidStack drained = fluidTank.drain(toDrain, FluidAction.SIMULATE);
        if (drained.getFluid() != Ic2Fluids.UU_MATTER.get() || drained.getAmount() != toDrain) return false;
        fluidTank.drain(toDrain, FluidAction.EXECUTE);
        amount -= toDrain / 1000.0;
        extraUuStored = amount < 0 ? -amount : 0;
        return true;
    }

    public void refreshInfo() {
        IPatternStorage storage = getPatternStorage();
        ItemStack old = pattern;
        if (storage == null) pattern = ItemStack.EMPTY;
        else {
            List<ItemStack> patterns = storage.getPatterns();
            if (index < 0 || index >= patterns.size()) index = 0;
            maxIndex = patterns.size();
            if (patterns.isEmpty()) pattern = ItemStack.EMPTY;
            else {
                pattern = patterns.get(index);
                patternUu = UuGraph.getInBuckets(pattern);
                if (!pattern.is(old.getItem())) { uuProcessed = 0; mode = Mode.STOPPED; }
            }
        }
        if (pattern.isEmpty()) { uuProcessed = 0; mode = Mode.STOPPED; }
    }

    public IPatternStorage getPatternStorage() {
        for (Direction d : Direction.values()) if (ic2.core.util.Util.loadedBlockEntity(level, worldPosition.relative(d)) instanceof IPatternStorage s) return s;
        return null;
    }

    public void setOverclockRates() {
        upgradeSlot.onChanged();
        uuPerTick = 1e-4 / upgradeSlot.processTimeMultiplier;
        euPerTick = (512 + upgradeSlot.extraEnergyDemand) * upgradeSlot.energyDemandMultiplier;
        setTier(TileEntityStandardMachine.applyModifier(4, upgradeSlot.extraTier, 1));
        maxEnergy = TileEntityStandardMachine.applyModifier(2000000, upgradeSlot.extraEnergyStorage, upgradeSlot.energyStorageMultiplier);
    }

    @Override public void onLoaded() { super.onLoaded(); if (!level.isClientSide) { setOverclockRates(); refreshInfo(); } }
    @Override public void setChanged() { super.setChanged(); if (level != null && !level.isClientSide) setOverclockRates(); }

    public boolean gainFluid() {
        MutableObject<ItemStack> out = new MutableObject<>();
        if (fluidSlot.transferToTank(fluidTank, out, true) && (out.getValue() == null || cellSlot.canAdd(out.getValue()))) {
            boolean ret = fluidSlot.transferToTank(fluidTank, out, false);
            if (out.getValue() != null) cellSlot.add(out.getValue());
            return ret;
        }
        return false;
    }

    public void onNetworkEvent(int event) {
        switch (event) {
            case 0, 1 -> {
                if (mode != Mode.STOPPED) break;
                IPatternStorage storage = getPatternStorage();
                if (storage == null || storage.getPatterns().isEmpty()) break;
                int n = storage.getPatterns().size();
                index = event == 0 ? (index <= 0 ? n - 1 : index - 1) : (index >= n - 1 ? 0 : index + 1);
                refreshInfo();
            }
            case 3 -> { if (mode != Mode.STOPPED) { uuProcessed = 0; mode = Mode.STOPPED; } }
            case 4 -> { refreshInfo(); if (!pattern.isEmpty()) mode = Mode.SINGLE; }
            case 5 -> { refreshInfo(); if (!pattern.isEmpty()) mode = Mode.CONTINUOUS; }
            default -> {}
        }
        setChanged();
    }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) {
        super.readFromNBT(nbt, r);
        extraUuStored = nbt.getDouble("extraUuStored"); uuProcessed = nbt.getDouble("uuProcessed"); index = nbt.getInt("index");
        int m = nbt.getInt("mode"); mode = m < Mode.values().length ? Mode.values()[m] : Mode.STOPPED;
        pattern = ItemStack.parseOptional(r, nbt.getCompound("pattern"));
    }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) {
        super.writeToNBT(nbt, r);
        nbt.putDouble("extraUuStored", extraUuStored); nbt.putDouble("uuProcessed", uuProcessed); nbt.putInt("index", index); nbt.putInt("mode", mode.ordinal());
        if (!pattern.isEmpty()) nbt.put("pattern", pattern.save(r));
    }

    @Override public boolean canFill(Direction from, Fluid fluid) { return fluid == Ic2Fluids.UU_MATTER.get(); }
    @Override public boolean canDrain(Direction from, Fluid fluid) { return false; }
    @Override public double getEnergy() { return energy; }
    @Override public boolean useEnergy(double amount) { if (energy >= amount) { energy -= amount; return true; } return false; }
    @Override public Set<UpgradableProperty> getUpgradableProperties() { return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerUu.Replicator(id, inv, this); }
}
