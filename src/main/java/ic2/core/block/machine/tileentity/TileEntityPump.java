package ic2.core.block.machine.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerPump;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Sounds;
import ic2.core.upgrade.IUpgradableBlock;
import ic2.core.upgrade.IUpgradeItem;
import ic2.core.upgrade.UpgradableProperty;
import ic2.core.util.PumpUtil;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.mutable.MutableObject;

/**
 * Pumps 1 B per operation (20 ticks, 1 EU/t) into an 8 B tank: from the liquid source a neighbouring miner ran into, or
 * else from the block or fluid handler in front of it. Fills containers in its container slot.
 */
public class TileEntityPump extends TileEntityLiquidTankElectricMachine implements IHasGui, IUpgradableBlock {
    public final int defaultTier = 1, defaultEnergyConsume = 1, defaultOperationLength = 20, defaultEnergyStorage = 20;
    public int energyConsume = 1, operationsPerTick, operationLength = 20;
    public short progress;
    public float guiProgress;
    private TileEntityMiner miner;
    private AudioSource audioSource;
    public final InvSlotConsumableLiquid containerSlot = new InvSlotConsumableLiquid(this, "containerSlot", InvSlot.Access.I, 1, InvSlot.InvSide.TOP);
    public final InvSlotOutput outputSlot = new InvSlotOutput(this, "output", 1);
    public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);

    public TileEntityPump(BlockPos pos, BlockState state) {
        super(Ic2BlockEntities.PUMP.get(), pos, state, 20, 1, 8);
        containerSlot.fillMode = true;
    }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); progress = nbt.getShort("progress"); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putShort("progress", progress); }

    @Override public void onLoaded() { super.onLoaded(); if (!level.isClientSide) setUpgradestat(); }
    @Override public void onUnloaded() { if (audioSource != null) { audioSource.remove(); audioSource = null; } miner = null; super.onUnloaded(); }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        boolean active = false;
        if (energy >= energyConsume * operationLength && operate(true)) {
            active = true;
            if (progress < operationLength) { progress++; energy -= energyConsume; }
            else { progress = 0; operate(false); }
        }
        setActive(active);
        MutableObject<ItemStack> output = new MutableObject<>();
        if (containerSlot.transferFromTank(fluidTank, output, true) && (output.getValue() == null || outputSlot.canAdd(output.getValue()))) {
            containerSlot.transferFromTank(fluidTank, output, false);
            if (output.getValue() != null) outputSlot.add(output.getValue());
        }
        for (int i = 0; i < upgradeSlot.size(); i++) {
            ItemStack s = upgradeSlot.get(i);
            if (s.getItem() instanceof IUpgradeItem u && u.onTick(s, this)) needsInvUpdate = true;
        }
        guiProgress = (float)progress / operationLength;
        if (needsInvUpdate) setChanged();
    }

    public boolean operate(boolean sim) {
        if (miner == null || miner.isRemoved()) {
            miner = null;
            for (Direction d : Direction.values())
                if (d != Direction.UP && level.getBlockEntity(worldPosition.relative(d)) instanceof TileEntityMiner m) { miner = m; break; }
        }
        FluidStack liquid = null;
        if (miner != null) { if (miner.canProvideLiquid) liquid = pump(miner.liquidPos, sim, miner); }
        else liquid = pump(worldPosition.relative(getFacing()), sim, null);
        if (liquid != null && fluidTank.fill(liquid, FluidAction.SIMULATE) > 0) {
            if (!sim) fluidTank.fill(liquid, FluidAction.EXECUTE);
            return true;
        }
        return false;
    }

    public FluidStack pump(BlockPos pos, boolean sim, TileEntityMiner miner) {
        int freespace = fluidTank.getCapacity() - fluidTank.getFluidAmount();
        if (miner == null && freespace > 0) {
            var handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, getFacing().getOpposite());
            if (handler != null) {
                FluidStack ret = handler.drain(Math.min(freespace, 1000), sim ? FluidAction.SIMULATE : FluidAction.EXECUTE);
                return ret.isEmpty() ? null : ret;
            }
        }
        if (freespace < 1000) return null;
        BlockPos src = PumpUtil.searchFluidSource(level, pos);
        if (src == null) return null;
        BlockState state = level.getBlockState(src);
        if (!(state.getBlock() instanceof LiquidBlock) || !state.getFluidState().isSource()) return null;
        Fluid fluid = state.getFluidState().getType();
        if (!sim) level.setBlock(src, Blocks.AIR.defaultBlockState(), 3);
        return new FluidStack(fluid, 1000);
    }

    @Override public void setChanged() { super.setChanged(); if (level != null && !level.isClientSide) setUpgradestat(); }

    public void setUpgradestat() {
        upgradeSlot.onChanged();
        double previousProgress = (double)progress / operationLength;
        double stackOpLen = (defaultOperationLength + upgradeSlot.extraProcessTime) * 64.0 * upgradeSlot.processTimeMultiplier;
        operationsPerTick = (int)Math.min(Math.ceil(64.0 / stackOpLen), Integer.MAX_VALUE);
        operationLength = (int)Math.round(stackOpLen * operationsPerTick / 64.0);
        energyConsume = TileEntityStandardMachine.applyModifier(defaultEnergyConsume, upgradeSlot.extraEnergyDemand, upgradeSlot.energyDemandMultiplier);
        setTier(TileEntityStandardMachine.applyModifier(defaultTier, upgradeSlot.extraTier, 1));
        maxEnergy = TileEntityStandardMachine.applyModifier(defaultEnergyStorage, upgradeSlot.extraEnergyStorage + operationLength * energyConsume, upgradeSlot.energyStorageMultiplier);
        if (operationLength < 1) operationLength = 1;
        progress = (short)Math.floor(previousProgress * operationLength + 0.1);
    }

    private boolean lastClientActive;
    @Override
    protected void updateEntityClient() {
        boolean active = getActive();
        if (active == lastClientActive) return;
        lastClientActive = active;
        if (audioSource == null) audioSource = AudioSource.create(this, Ic2Sounds.PUMP_OP.get());
        if (audioSource != null) { if (active) audioSource.play(); else audioSource.stop(); }
    }

    @Override public boolean canFill(Direction from, Fluid fluid) { return false; }
    @Override public boolean canDrain(Direction from, Fluid fluid) { return true; }
    @Override public double getEnergy() { return energy; }
    @Override public boolean useEnergy(double amount) { if (energy >= amount) { energy -= amount; return true; } return false; }
    @Override public Set<UpgradableProperty> getUpgradableProperties() { return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidProducing); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerPump(id, inv, this); }
}
