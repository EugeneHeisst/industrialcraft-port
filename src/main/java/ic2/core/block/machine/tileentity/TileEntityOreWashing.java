package ic2.core.block.machine.tileentity;

import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.block.machine.container.ContainerOreWashing;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.upgrade.UpgradableProperty;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableObject;

/** 16 EU/t, 500 ticks, 8 bucket water tank; each recipe drains its "amount" (fluid) of water. */
public class TileEntityOreWashing extends TileEntityLiquidTankStandardMaschine {
    public final InvSlotConsumableLiquid fluidSlot = new InvSlotConsumableLiquid(this, "fluid", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, Fluids.WATER);
    public final InvSlotOutput cellSlot = new InvSlotOutput(this, "cell", 1);

    public TileEntityOreWashing(BlockPos pos, BlockState state) {
        super(Ic2BlockEntities.ORE_WASHING_PLANT.get(), pos, state, 16, 500, 3, 8);
        inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.oreWashing);
    }

    @Override protected void updateEntityServer() { super.updateEntityServer(); if (needsFluid()) gainFluid(); }

    @Override
    public void operateOnce(RecipeOutput output, List<ItemStack> processResult) {
        super.operateOnce(output, processResult);
        fluidTank.drain(output.metadata.getInt("fluid"), FluidAction.EXECUTE);
    }

    @Override
    public RecipeOutput getOutput() {
        RecipeOutput ret = super.getOutput();
        if (ret != null && (ret.metadata == null || ret.metadata.getInt("fluid") > fluidTank.getFluidAmount())) return null;
        return ret;
    }

    public boolean gainFluid() {
        MutableObject<ItemStack> output = new MutableObject<>();
        if (fluidSlot.transferToTank(fluidTank, output, true) && (output.getValue() == null || cellSlot.canAdd(output.getValue()))) {
            boolean ret = fluidSlot.transferToTank(fluidTank, output, false);
            if (output.getValue() != null) cellSlot.add(output.getValue());
            return ret;
        }
        return false;
    }

    @Override public boolean canFill(Direction from, Fluid fluid) { return fluid == Fluids.WATER; }
    @Override public boolean canDrain(Direction from, Fluid fluid) { return false; }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerOreWashing(id, inventory, this); }
    @Override public float getWrenchDropRate() { return 0.85f; }
    @Override public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming);
    }
}
