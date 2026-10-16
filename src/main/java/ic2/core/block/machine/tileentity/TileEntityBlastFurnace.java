package ic2.core.block.machine.tileentity;

import ic2.api.energy.tile.IHeatSource;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerBlastFurnace;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Heated through its front by an IHeatSource up to 50000 hU; 6000 ticks per item, consuming a compressed air cell every 1000 ticks. */
public class TileEntityBlastFurnace extends TileEntityInventory implements IHasGui, IUpgradableBlock {
    public boolean outOfAir;
    public int progress;
    public final int maxprogress = 6000;
    private boolean newActive;
    public int heat;
    public static int maxHeat = 50000;
    public final InvSlotConsumable inputSlot = new InvSlotProcessableGeneric(this, "InputSlot", 1, Recipes.blastfurance);
    public final InvSlotConsumable airSlot = new InvSlotConsumable(this, "AirSlot", 1) { @Override public boolean accepts(ItemStack stack) { return stack.is(Ic2Items.get("air_cell")); } };
    public final InvSlotOutput outputSlot = new InvSlotOutput(this, "OuputSlot", 1);
    public final InvSlotOutput airOutputSlot = new InvSlotOutput(this, "AirOutputSlot", 1);
    public final InvSlotOutput slagOutputSlot = new InvSlotOutput(this, "OuputslagSlot", 1);
    public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 2);

    public TileEntityBlastFurnace(BlockPos pos, BlockState state) { super(Ic2BlockEntities.BLAST_FURNACE.get(), pos, state); }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); heat = nbt.getInt("heat"); progress = nbt.getInt("progress"); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putInt("heat", heat); nbt.putInt("progress", progress); }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        heatup();
        if (isHot()) newActive = work();
        if (getActive() != newActive) setActive(newActive);
        for (int i = 0; i < upgradeSlot.size(); i++) {
            ItemStack stack = upgradeSlot.get(i);
            if (stack.getItem() instanceof IUpgradeItem u && u.onTick(stack, this)) super.setChanged();
        }
    }

    private boolean isAirStep() { return progress == 1 || progress % 1000 == 0 && progress >= 1000 && progress <= 5000; }

    private boolean work() {
        if (inputSlot.isEmpty()) return false;
        RecipeOutput ro = Recipes.blastfurance.getOutputFor(inputSlot.get(), false);
        if (ro == null) return false;
        ItemStack first = ro.items.get(0), second = ro.items.size() > 1 ? ro.items.get(1) : ItemStack.EMPTY;
        if (!outputSlot.canAdd(first) || !second.isEmpty() && !outputSlot.canAdd(second)) return false;
        if (progress == 0) { progress++; return true; }
        ItemStack cell = new ItemStack(Ic2Items.get("empty_cell"));
        if (isAirStep() && !airSlot.isEmpty() && airOutputSlot.canAdd(cell)) {
            airSlot.consume(1);
            airOutputSlot.add(cell);
            progress++;
            outOfAir = false;
            return true;
        }
        if (progress >= maxprogress) {
            outputSlot.add(first.copy());
            if (!second.isEmpty()) slagOutputSlot.add(second.copy());
            inputSlot.consume(1);
            progress = 0;
            return false;
        }
        if (!isAirStep()) { progress++; return true; }
        outOfAir = true;
        return false;
    }

    private void heatup() {
        int heatRequested = 0;
        if ((!inputSlot.isEmpty() || progress >= 1) && heat <= maxHeat) heatRequested = maxHeat - heat + 100;
        else if (upgradeSlot.getRedstoneInput(level.getBestNeighborSignal(worldPosition)) > 0 && heat <= maxHeat) heatRequested = maxHeat - heat + 100;
        int gain = 0;
        if (heatRequested > 0) {
            Direction dir = getFacing();
            if (level.getBlockEntity(worldPosition.relative(dir)) instanceof IHeatSource source) {
                gain = source.requestHeat(dir.getOpposite(), heatRequested);
                heat += gain;
            }
        }
        if (gain == 0) heat -= Math.min(heat, 1);
    }

    public boolean isHot() { return heat >= maxHeat; }
    public int gaugeHeatScaled(int i) { return i * heat / maxHeat; }
    public int gaugeprogress(int i) { return progress * i / maxprogress; }
    public int getprogresspercent() { return (int)((float)progress / maxprogress * 100); }
    public int getheatpercent() { return (int)((float)heat / maxHeat * 100); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerBlastFurnace(id, inventory, this); }
    @Override public boolean wrenchCanSetFacing(Player player, Direction side) { return getFacing() != side; }
    @Override public double getEnergy() { return 40; }
    @Override public boolean useEnergy(double amount) { return true; }
    @Override public Set<UpgradableProperty> getUpgradableProperties() { return EnumSet.of(UpgradableProperty.RedstoneSensitive, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing); }
}
