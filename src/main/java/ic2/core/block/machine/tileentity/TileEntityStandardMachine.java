package ic2.core.block.machine.tileentity;

import ic2.api.recipe.RecipeOutput;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerStandardMachine;
import ic2.core.network.TileEventPayload;
import ic2.core.upgrade.IUpgradableBlock;
import ic2.core.upgrade.IUpgradeItem;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityStandardMachine extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, TileEventPayload.Listener {
    protected short progress;
    public final int defaultEnergyConsume, defaultOperationLength, defaultTier, defaultEnergyStorage;
    public int energyConsume, operationLength, operationsPerTick;
    public float guiProgress;
    public AudioSource audioSource;
    private static final int EventStart = 0, EventInterrupt = 1, EventStop = 2;
    public InvSlotProcessable inputSlot;
    public final InvSlotOutput outputSlot;
    public final InvSlotUpgrade upgradeSlot;

    protected TileEntityStandardMachine(BlockEntityType<?> type, BlockPos pos, BlockState state, int energyPerTick, int length, int outputSlots) { this(type, pos, state, energyPerTick, length, outputSlots, 1); }

    protected TileEntityStandardMachine(BlockEntityType<?> type, BlockPos pos, BlockState state, int energyPerTick, int length, int outputSlots, int defaultTier) {
        super(type, pos, state, energyPerTick * length, 1);
        defaultEnergyConsume = energyConsume = energyPerTick;
        defaultOperationLength = operationLength = length;
        this.defaultTier = defaultTier;
        defaultEnergyStorage = energyPerTick * length;
        outputSlot = new InvSlotOutput(this, "output", outputSlots);
        upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
    }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider registries) { super.readFromNBT(nbt, registries); progress = nbt.getShort("progress"); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider registries) { super.writeToNBT(nbt, registries); nbt.putShort("progress", progress); }

    public float getProgress() { return guiProgress; }

    @Override public void onLoaded() { super.onLoaded(); setOverclockRates(); }

    @Override
    public void onUnloaded() {
        super.onUnloaded();
        if (audioSource != null) { audioSource.remove(); audioSource = null; }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) setOverclockRates();
    }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        RecipeOutput output = getOutput();
        if (output != null && energy >= energyConsume) {
            setActive(true);
            if (progress == 0) TileEventPayload.send(this, EventStart);
            progress++;
            energy -= energyConsume;
            if (progress >= operationLength) {
                operate(output);
                needsInvUpdate = true;
                progress = 0;
                TileEventPayload.send(this, EventStop);
            }
        } else {
            if (progress != 0 && getActive()) TileEventPayload.send(this, EventInterrupt);
            if (output == null) progress = 0;
            setActive(false);
        }
        for (int i = 0; i < upgradeSlot.size(); i++) {
            ItemStack stack = upgradeSlot.get(i);
            if (stack.getItem() instanceof IUpgradeItem upgrade && upgrade.onTick(stack, this)) needsInvUpdate = true;
        }
        guiProgress = (float)progress / operationLength;
        if (needsInvUpdate) super.setChanged();
    }

    public void setOverclockRates() {
        upgradeSlot.onChanged();
        double previousProgress = (double)progress / operationLength;
        double stackOpLen = (defaultOperationLength + upgradeSlot.extraProcessTime) * 64.0 * upgradeSlot.processTimeMultiplier;
        operationsPerTick = (int)Math.min(Math.ceil(64 / stackOpLen), Integer.MAX_VALUE);
        operationLength = (int)Math.round(stackOpLen * operationsPerTick / 64);
        energyConsume = applyModifier(defaultEnergyConsume, upgradeSlot.extraEnergyDemand, upgradeSlot.energyDemandMultiplier);
        setTier(applyModifier(defaultTier, upgradeSlot.extraTier, 1));
        maxEnergy = applyModifier(defaultEnergyStorage, upgradeSlot.extraEnergyStorage + operationLength * energyConsume, upgradeSlot.energyStorageMultiplier);
        if (operationLength < 1) operationLength = 1;
        progress = (short)Math.floor(previousProgress * operationLength + 0.1);
    }

    public void operate(RecipeOutput output) {
        for (int i = 0; i < operationsPerTick; i++) {
            List<ItemStack> processResult = output.items;
            for (int j = 0; j < upgradeSlot.size(); j++) {
                ItemStack stack = upgradeSlot.get(j);
                if (stack.getItem() instanceof IUpgradeItem upgrade) upgrade.onProcessEnd(stack, this, processResult);
            }
            operateOnce(output, processResult);
            output = getOutput();
            if (output == null) break;
        }
    }

    public void operateOnce(RecipeOutput output, List<ItemStack> processResult) {
        inputSlot.consume();
        outputSlot.add(processResult);
    }

    public RecipeOutput getOutput() {
        if (inputSlot.isEmpty()) return null;
        RecipeOutput output = inputSlot.process();
        if (output == null) return null;
        return outputSlot.canAdd(output.items) ? output : null;
    }

    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerStandardMachine<>(id, inventory, this); }

    public SoundEvent getStartSoundFile() { return null; }
    public SoundEvent getInterruptSoundFile() { return null; }

    @Override
    public void onNetworkEvent(int event) {
        if (audioSource == null && getStartSoundFile() != null) audioSource = AudioSource.create(this, getStartSoundFile());
        switch (event) {
            case EventStart -> { if (audioSource != null) audioSource.play(); }
            case EventInterrupt -> {
                if (audioSource != null) {
                    audioSource.stop();
                    if (getInterruptSoundFile() != null) AudioSource.playOnce(this, getInterruptSoundFile());
                }
            }
            case EventStop -> { if (audioSource != null) audioSource.stop(); }
        }
    }

    public static int applyModifier(int base, int extra, double multiplier) {
        double ret = Math.round((base + (double)extra) * multiplier);
        return ret > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)ret;
    }

    @Override public double getEnergy() { return energy; }
    @Override public boolean useEnergy(double amount) { if (energy >= amount) { energy -= amount; return true; } return false; }
}
