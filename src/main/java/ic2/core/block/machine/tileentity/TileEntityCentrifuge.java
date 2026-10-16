package ic2.core.block.machine.tileentity;

import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.block.machine.container.ContainerCentrifuge;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.upgrade.UpgradableProperty;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;

/** Thermal centrifuge: heats by 1 per EU up to the recipe's minimum heat (max 5000) before it works. */
public class TileEntityCentrifuge extends TileEntityStandardMachine {
    public short maxHeat = 5000, heat, workheat = maxHeat;

    public TileEntityCentrifuge(BlockPos pos, BlockState state) {
        super(Ic2BlockEntities.THERMAL_CENTRIFUGE.get(), pos, state, 48, 500, 3, 2);
        inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.centrifuge);
    }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); heat = nbt.getShort("heat"); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putShort("heat", heat); }

    public int gaugeHeatScaled(int i) { return i * heat / workheat; }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean heating = false;
        if (energy >= 1) {
            int heatRequested = Integer.MIN_VALUE;
            RecipeOutput output = super.getOutput();
            if (output != null) {
                heatRequested = Math.min(maxHeat, output.metadata.getInt("heat"));
                workheat = (short)heatRequested;
                if (heat > heatRequested) heat = (short)heatRequested;
            } else if (heat < maxHeat && hasRedstoneInput()) {
                heatRequested = maxHeat;
                workheat = (short)heatRequested;
            }
            if (heat - 1 < heatRequested) { energy--; heating = true; }
        }
        if (heating) heat++;
        else heat = (short)(heat - Math.min(heat, 1));
    }

    @Override
    public RecipeOutput getOutput() {
        RecipeOutput ret = super.getOutput();
        if (ret != null && (ret.metadata == null || ret.metadata.getInt("heat") > heat)) return null;
        return ret;
    }

    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerCentrifuge(id, inventory, this); }
    @Override public float getWrenchDropRate() { return 0.8f; }
    @Override public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
}
