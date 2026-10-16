package ic2.core.block.machine.tileentity;

import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerMatter;
import ic2.core.fluid.Ic2Fluids;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Sounds;
import ic2.core.upgrade.IUpgradableBlock;
import ic2.core.upgrade.IUpgradeItem;
import ic2.core.upgrade.UpgradableProperty;
import java.util.EnumSet;
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

/** 1 mB UU matter per 1 MEU; amplifiers (scrap 5000, scrap box 45000) make each EU count 6x while it lasts. Redstone stops it. */
public class TileEntityMatter extends TileEntityLiquidTankElectricMachine implements IHasGui, IUpgradableBlock {
    public final int defaultTier = 3;
    public int scrap, state, prevState;
    public final InvSlotProcessableGeneric amplifierSlot = new InvSlotProcessableGeneric(this, "scrap", 1, Recipes.matterAmplifier);
    public final InvSlotOutput outputSlot = new InvSlotOutput(this, "output", 1);
    public final InvSlotConsumableLiquid containerslot = new InvSlotConsumableLiquid(this, "containerslot", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, Ic2Fluids.UU_MATTER.get());
    public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
    private AudioSource audioSource, audioSourceScrap;

    public TileEntityMatter(BlockPos pos, BlockState state) {
        super(Ic2BlockEntities.MASS_FABRICATOR.get(), pos, state, 1000000, 3, 8);
        containerslot.fillMode = true;
    }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); scrap = nbt.getInt("scrap"); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putInt("scrap", scrap); }
    @Override protected void writeClientData(CompoundTag nbt, HolderLookup.Provider r) { nbt.putInt("state", state); }
    @Override protected void readClientData(CompoundTag nbt, HolderLookup.Provider r) { state = nbt.getInt("state"); }

    private boolean redstone() { return upgradeSlot.getRedstoneInput(level.getBestNeighborSignal(worldPosition)) > 0; }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        for (int i = 0; i < upgradeSlot.size(); i++) {
            ItemStack s = upgradeSlot.get(i);
            if (s.getItem() instanceof IUpgradeItem u && u.onTick(s, this)) needsInvUpdate = true;
        }
        if (!redstone() && energy > 0) {
            setState(scrap > 0 ? 2 : 1);
            setActive(true);
            if (scrap < 10000) {
                RecipeOutput amp = amplifierSlot.process();
                if (amp != null) { amplifierSlot.consume(); scrap += amp.metadata.getInt("amplification"); }
            }
            if (energy >= maxEnergy) needsInvUpdate = attemptGeneration();
            MutableObject<ItemStack> output = new MutableObject<>();
            if (containerslot.transferFromTank(fluidTank, output, true) && (output.getValue() == null || outputSlot.canAdd(output.getValue()))) {
                containerslot.transferFromTank(fluidTank, output, false);
                if (output.getValue() != null) outputSlot.add(output.getValue());
            }
            if (needsInvUpdate) setChanged();
        } else { setState(0); setActive(false); }
    }

    public boolean attemptGeneration() {
        if (fluidTank.getFluidAmount() + 1 > fluidTank.getCapacity()) return false;
        fluidTank.fill(new FluidStack(Ic2Fluids.UU_MATTER.get(), 1), FluidAction.EXECUTE);
        energy -= maxEnergy;
        return true;
    }

    private void setState(int s) { if (state != s) { state = s; sendClientUpdate(); } }

    private int lastClientState = -1;
    @Override
    protected void updateEntityClient() {
        if (state == lastClientState) return;
        lastClientState = state;
        if (state == 0) { if (audioSource != null) audioSource.stop(); if (audioSourceScrap != null) audioSourceScrap.stop(); return; }
        if (audioSource == null) audioSource = AudioSource.create(this, Ic2Sounds.MASS_FAB_LOOP.get());
        if (audioSource != null) audioSource.play();
        if (state == 2) { if (audioSourceScrap == null) audioSourceScrap = AudioSource.create(this, Ic2Sounds.MASS_FAB_SCRAP.get()); if (audioSourceScrap != null) audioSourceScrap.play(); }
        else if (audioSourceScrap != null) audioSourceScrap.stop();
    }

    @Override public void onUnloaded() { if (audioSource != null) audioSource.remove(); if (audioSourceScrap != null) audioSourceScrap.remove(); audioSource = audioSourceScrap = null; super.onUnloaded(); }

    @Override public double getDemandedEnergy() { return level != null && redstone() ? 0 : maxEnergy - energy; }

    @Override
    public double injectEnergy(Direction from, double amount, double voltage) {
        if (energy >= maxEnergy || redstone()) return amount;
        int bonus = Math.min((int)amount, scrap);
        scrap -= bonus;
        energy += amount + 5 * bonus;
        return 0;
    }

    public String getProgressAsString() { return Math.min((int)(energy * 100 / maxEnergy), 100) + "%"; }
    @Override public void setChanged() { super.setChanged(); if (level != null && !level.isClientSide) { upgradeSlot.onChanged(); setTier(Math.max(0, defaultTier + upgradeSlot.extraTier)); } }
    @Override public boolean canFill(Direction from, Fluid fluid) { return fluid == Ic2Fluids.UU_MATTER.get(); }
    @Override public boolean canDrain(Direction from, Fluid fluid) { return true; }
    @Override public float getWrenchDropRate() { return 0.7f; }
    @Override public double getEnergy() { return energy; }
    @Override public boolean useEnergy(double amount) { if (energy >= amount) { energy -= amount; return true; } return false; }
    @Override public Set<UpgradableProperty> getUpgradableProperties() { return EnumSet.of(UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidProducing); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerMatter(id, inv, this); }
}
