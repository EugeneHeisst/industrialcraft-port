package ic2.core.block.machine.tileentity;

import ic2.api.recipe.RecipeOutput;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.block.invslot.InvSlotProcessableSmelting;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerInduction;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.upgrade.IUpgradableBlock;
import ic2.core.upgrade.IUpgradeItem;
import ic2.core.upgrade.UpgradableProperty;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Two-slot smelter that heats up with energy (1 EU per heat, max 10000); progress per tick = heat/30, operation at 4000. */
public class TileEntityInduction extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock {
    public static short maxHeat = 10000;
    public short heat, progress;
    public final InvSlotProcessable inputSlotA = new InvSlotProcessableSmelting(this, "inputA", 1);
    public final InvSlotProcessable inputSlotB = new InvSlotProcessableSmelting(this, "inputB", 1);
    public final InvSlotOutput outputSlotA = new InvSlotOutput(this, "outputA", 1);
    public final InvSlotOutput outputSlotB = new InvSlotOutput(this, "outputB", 1);
    public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 2);

    public TileEntityInduction(BlockPos pos, BlockState state) { super(Ic2BlockEntities.INDUCTION_FURNACE.get(), pos, state, maxHeat, 2); }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); heat = nbt.getShort("heat"); progress = nbt.getShort("progress"); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putShort("heat", heat); nbt.putShort("progress", progress); }

    public String getHeat() { return heat * 100 / maxHeat + "%"; }
    public int gaugeProgressScaled(int i) { return i * progress / 4000; }

    private boolean hasRedstoneInput() { return upgradeSlot.getRedstoneInput(level.getBestNeighborSignal(worldPosition)) > 0; }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        boolean newActive = getActive();
        if (heat == 0) newActive = false;
        if (progress >= 4000) { operate(); needsInvUpdate = true; progress = 0; newActive = false; }
        boolean canOperate = canOperate();
        if (energy > 0 && (canOperate || hasRedstoneInput())) {
            energy--;
            if (heat < maxHeat) heat++;
            newActive = true;
        } else heat = (short)(heat - Math.min(heat, 4));
        if (newActive && progress != 0) {
            if (!canOperate || energy < 15) { if (!canOperate) progress = 0; newActive = false; }
        } else if (canOperate) { if (energy >= 15) newActive = true; }
        else progress = 0;
        if (newActive && canOperate) { progress = (short)(progress + heat / 30); energy -= 15; }
        if (needsInvUpdate) setChanged();
        if (newActive != getActive()) setActive(newActive);
        for (int i = 0; i < upgradeSlot.size(); i++) {
            ItemStack stack = upgradeSlot.get(i);
            if (stack.getItem() instanceof IUpgradeItem u && u.onTick(stack, this)) super.setChanged();
        }
    }

    public void operate() { operate(inputSlotA, outputSlotA); operate(inputSlotB, outputSlotB); }
    public void operate(InvSlotProcessable in, InvSlotOutput out) { if (canOperate(in, out)) { out.add(in.process().items); in.consume(); } }
    public boolean canOperate() { return canOperate(inputSlotA, outputSlotA) || canOperate(inputSlotB, outputSlotB); }
    public boolean canOperate(InvSlotProcessable in, InvSlotOutput out) {
        if (in.isEmpty()) return false;
        RecipeOutput output = in.process();
        return output != null && out.canAdd(output.items);
    }

    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerInduction(id, inventory, this); }
    @Override public float getWrenchDropRate() { return 0.8f; }
    @Override public double getEnergy() { return energy; }
    @Override public boolean useEnergy(double amount) { if (energy >= amount) { energy -= amount; return true; } return false; }
    @Override public Set<UpgradableProperty> getUpgradableProperties() { return EnumSet.of(UpgradableProperty.RedstoneSensitive, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing); }
}
