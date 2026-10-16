package ic2.core.block.machine.tileentity;

import ic2.api.recipe.RecipeOutput;
import ic2.core.ContainerBase;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.machine.container.ContainerFluidBottler;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.mutable.MutableObject;

/** Bottling plant: empties containers from the top slot into its 8 B tank and fills containers in the bottom slot from it. */
public class TileEntityFluidBottler extends TileEntityLiquidTankStandardMaschine {
    public final InvSlotConsumableLiquid drainInputSlot, fillInputSlot;

    public TileEntityFluidBottler(BlockPos pos, BlockState state) {
        super(Ic2BlockEntities.FLUID_BOTTLER.get(), pos, state, 2, 100, 1, 8);
        drainInputSlot = new InvSlotConsumableLiquid(this, "drainInput", InvSlot.Access.I, 1, InvSlot.InvSide.TOP) {
            @Override public boolean accepts(ItemStack stack) {
                FluidStack f = contained(stack);
                return !f.isEmpty() && (fluidTank.isEmpty() || FluidStack.isSameFluidSameComponents(f, fluidTank.getFluid()));
            }
        };
        fillInputSlot = new InvSlotConsumableLiquid(this, "fillInput", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM) {
            @Override public boolean accepts(ItemStack stack) {
                var h = stack.copyWithCount(1).getCapability(Capabilities.FluidHandler.ITEM);
                if (h == null) return false;
                if (fluidTank.isEmpty()) return h.getTanks() > 0 && h.getFluidInTank(0).getAmount() < h.getTankCapacity(0);
                return h.fill(fluidTank.getFluid().copyWithAmount(Integer.MAX_VALUE), FluidAction.SIMULATE) > 0;
            }
        };
    }

    @Override public RecipeOutput getOutput() { return processInput(true); }
    @Override public void operateOnce(RecipeOutput output, List<ItemStack> processResult) { processInput(false); outputSlot.add(processResult); }

    private RecipeOutput processInput(boolean simulate) {
        MutableObject<ItemStack> out = new MutableObject<>();
        if (!drainInputSlot.isEmpty() && drainInputSlot.transferToTank(fluidTank, out, simulate) && (out.getValue() == null || outputSlot.canAdd(out.getValue())))
            return out.getValue() == null ? new RecipeOutput(null) : new RecipeOutput(null, out.getValue());
        out.setValue(null);
        if (!fillInputSlot.isEmpty() && fillInputSlot.transferFromTank(fluidTank, out, simulate) && (out.getValue() == null || outputSlot.canAdd(out.getValue())))
            return out.getValue() == null ? new RecipeOutput(null) : new RecipeOutput(null, out.getValue());
        return null;
    }

    @Override public float getWrenchDropRate() { return 0.85f; }
    @Override public boolean canFill(Direction from, Fluid fluid) { return true; }
    @Override public boolean canDrain(Direction from, Fluid fluid) { return true; }
    @Override public Set<UpgradableProperty> getUpgradableProperties() { return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerFluidBottler(id, inv, this); }
}
