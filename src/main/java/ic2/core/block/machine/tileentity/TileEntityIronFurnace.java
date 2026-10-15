package ic2.core.block.machine.tileentity;

import ic2.api.recipe.RecipeOutput;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlotConsumableFuel;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.block.invslot.InvSlotProcessableSmelting;
import ic2.core.block.machine.container.ContainerIronFurnace;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityIronFurnace extends TileEntityInventory implements IHasGui {
    public int fuel, maxFuel;
    public short progress;
    public final short operationLength = 160;
    public final InvSlotProcessable inputSlot = new InvSlotProcessableSmelting(this, "input", 1);
    public final InvSlotOutput outputSlot = new InvSlotOutput(this, "output", 1);
    public final InvSlotConsumableFuel fuelSlot = new InvSlotConsumableFuel(this, "fuel", 1, true);

    public TileEntityIronFurnace(BlockPos pos, BlockState state) { super(Ic2BlockEntities.IRON_FURNACE.get(), pos, state); }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); fuel = nbt.getInt("fuel"); maxFuel = nbt.getInt("maxFuel"); progress = nbt.getShort("progress"); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putInt("fuel", fuel); nbt.putInt("maxFuel", maxFuel); nbt.putShort("progress", progress); }

    public int gaugeProgressScaled(int i) { return progress * i / 160; }
    public int gaugeFuelScaled(int i) {
        if (maxFuel == 0) { maxFuel = fuel; if (maxFuel == 0) maxFuel = 160; }
        return fuel * i / maxFuel;
    }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        if (fuel <= 0 && canOperate()) { fuel = maxFuel = fuelSlot.consumeFuel(); if (fuel > 0) needsInvUpdate = true; }
        if (isBurning() && canOperate()) {
            progress++;
            if (progress >= 160) { progress = 0; operate(); needsInvUpdate = true; }
        } else progress = 0;
        if (fuel > 0) fuel--;
        if (getActive() != isBurning()) { setActive(isBurning()); needsInvUpdate = true; }
        if (needsInvUpdate) setChanged();
    }

    public void operate() { outputSlot.add(inputSlot.process().items); inputSlot.consume(); }
    public boolean isBurning() { return fuel > 0; }
    public boolean canOperate() { RecipeOutput output = inputSlot.process(); return output != null && outputSlot.canAdd(output.items); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerIronFurnace(id, inventory, this); }
}
