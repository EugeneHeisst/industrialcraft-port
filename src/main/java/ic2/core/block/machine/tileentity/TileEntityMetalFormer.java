package ic2.core.block.machine.tileentity;

import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.block.machine.container.ContainerMetalFormer;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.upgrade.UpgradableProperty;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;

/** Modes: 0 extruding, 1 rolling, 2 cutting. */
public class TileEntityMetalFormer extends TileEntityStandardMachine {
    private int mode;

    public TileEntityMetalFormer(BlockPos pos, BlockState state) {
        super(Ic2BlockEntities.METAL_FORMER.get(), pos, state, 10, 200, 1);
        inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.metalformerExtruding);
    }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); setMode(nbt.getInt("mode")); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putInt("mode", mode); }

    public int getMode() { return mode; }

    public void setMode(int mode) {
        var slot = (InvSlotProcessableGeneric)inputSlot;
        slot.setRecipeManager(switch (mode) { case 0 -> Recipes.metalformerExtruding; case 1 -> Recipes.metalformerRolling; case 2 -> Recipes.metalformerCutting; default -> throw new RuntimeException("invalid mode: " + mode); });
        this.mode = mode;
    }

    public void cycleMode() { setMode((mode + 1) % 3); }

    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerMetalFormer(id, inventory, this); }
    @Override public float getWrenchDropRate() { return 0.85f; }
    @Override public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
}
