package ic2.core.block.machine.tileentity;

import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.block.machine.CannerRecipes;
import ic2.core.block.machine.container.ContainerCanner;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.upgrade.UpgradableProperty;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.apache.commons.lang3.mutable.MutableObject;

/**
 * Canning machine with four modes: bottle solids (tin cans, fuel rods), empty fluid containers into the output tank, fill
 * containers from the input tank, and enrich the input fluid with an additive into the output tank.
 */
public class TileEntityCanner extends TileEntityStandardMachine implements IFluidHandler {
    public enum Mode { BottleSolid, EmptyLiquid, BottleLiquid, EnrichLiquid }

    private Mode mode = Mode.BottleSolid;
    public final FluidTank inputTank = new FluidTank(8000) { @Override protected void onContentsChanged() { setChanged(); } };
    public final FluidTank outputTank = new FluidTank(8000) { @Override protected void onContentsChanged() { setChanged(); } };
    public final InvSlotConsumableLiquid canInputSlot;

    public TileEntityCanner(BlockPos pos, BlockState state) {
        super(Ic2BlockEntities.CANNER.get(), pos, state, 4, 200, 1);
        inputSlot = new InvSlotProcessableGeneric(this, "input", 1, null) {
            @Override public boolean accepts(ItemStack stack) { return (mode == Mode.BottleSolid || mode == Mode.EnrichLiquid) && getOutputFor(stack, false, true) != null; }
            @Override protected boolean allowEmptyInput() { return true; }
            @Override protected RecipeOutput getOutputFor(ItemStack input, boolean adjustInput, boolean forAccept) { return output(canInputSlot.get(), input, adjustInput, forAccept); }
            @Override public void consume() {
                super.consume();
                if (!canInputSlot.get().isEmpty() && canInputSlot.get().getCount() <= 0) canInputSlot.put(ItemStack.EMPTY);
            }
        };
        canInputSlot = new InvSlotConsumableLiquid(this, "canInput", InvSlot.Access.I, 1, InvSlot.InvSide.TOP) {
            @Override public boolean accepts(ItemStack stack) {
                return switch (mode) {
                    case BottleSolid -> output(stack, inputSlot.get(), false, true) != null;
                    case BottleLiquid -> fillable(stack);
                    case EmptyLiquid -> !contained(stack).isEmpty();
                    case EnrichLiquid -> fillable(stack) || !contained(stack).isEmpty();
                };
            }
        };
    }

    private static boolean fillable(ItemStack stack) {
        var h = stack.copyWithCount(1).getCapability(Capabilities.FluidHandler.ITEM);
        return h != null && h.getTanks() > 0 && h.getFluidInTank(0).getAmount() < h.getTankCapacity(0);
    }

    RecipeOutput output(ItemStack container, ItemStack fill, boolean adjustInput, boolean forAccept) {
        return switch (mode) {
            case BottleSolid -> Recipes.cannerBottle.getOutputFor(container, fill, adjustInput, forAccept);
            case EnrichLiquid -> {
                FluidStack f = inputTank.getFluid();
                if (!adjustInput) f = f.copy();
                RecipeOutput r = Recipes.cannerEnrich.getOutputFor(f, fill, adjustInput, forAccept);
                if (adjustInput && f.isEmpty()) inputTank.setFluid(FluidStack.EMPTY);
                yield r;
            }
            case BottleLiquid -> fillContainer(adjustInput);
            case EmptyLiquid -> drainContainer(adjustInput);
        };
    }

    private RecipeOutput fillContainer(boolean adjustInput) {
        MutableObject<ItemStack> out = new MutableObject<>();
        if (!canInputSlot.transferFromTank(inputTank, out, !adjustInput)) return null;
        return out.getValue() != null ? new RecipeOutput(null, out.getValue()) : new RecipeOutput(null);
    }

    private RecipeOutput drainContainer(boolean adjustInput) {
        int max = outputTank.getCapacity() - outputTank.getFluidAmount();
        ItemStack stack = canInputSlot.get();
        if (max <= 0 || stack.isEmpty()) return null;
        var h = stack.copyWithCount(1).getCapability(Capabilities.FluidHandler.ITEM);
        if (h == null) return null;
        FluidStack fluid = h.drain(max, FluidAction.EXECUTE);
        if (fluid.isEmpty()) return null;
        ItemStack container = h.getContainer();
        if (adjustInput) { stack.shrink(1); if (stack.isEmpty()) canInputSlot.put(ItemStack.EMPTY); }
        return container.isEmpty() ? CannerRecipes.fluidOutput(fluid) : CannerRecipes.fluidOutput(fluid, container);
    }

    @Override
    public RecipeOutput getOutput() {
        if (mode == Mode.EmptyLiquid || mode == Mode.BottleLiquid ? canInputSlot.isEmpty() : inputSlot.isEmpty()) return null;
        RecipeOutput output = inputSlot.process();
        if (output == null || !outputSlot.canAdd(output.items)) return null;
        if (mode == Mode.EmptyLiquid || mode == Mode.EnrichLiquid) {
            FluidStack fluid = CannerRecipes.fluidOf(output);
            if (outputTank.fill(fluid, FluidAction.SIMULATE) != fluid.getAmount()) return null;
        }
        return output;
    }

    @Override
    public void operateOnce(RecipeOutput output, List<ItemStack> processResult) {
        super.operateOnce(output, processResult);
        if (mode == Mode.EmptyLiquid || mode == Mode.EnrichLiquid) outputTank.fill(CannerRecipes.fluidOf(output), FluidAction.EXECUTE);
        if (mode == Mode.EnrichLiquid) {
            MutableObject<ItemStack> out = new MutableObject<>();
            if (canInputSlot.transferFromTank(outputTank, out, true) && (out.getValue() == null || outputSlot.canAdd(out.getValue()))) {
                canInputSlot.transferFromTank(outputTank, out, false);
                if (out.getValue() != null) outputSlot.add(out.getValue());
            }
        }
    }

    public Mode getMode() { return mode; }
    public void setMode(Mode m) { mode = m; }

    public void onNetworkEvent(int event) {
        if (event == 0) mode = Mode.values()[(mode.ordinal() + 1) % 4];
        else if (event == 1 && progress == 0) { FluidStack in = inputTank.getFluid(); inputTank.setFluid(outputTank.getFluid()); outputTank.setFluid(in); }
        setChanged();
    }

    public int gaugeLiquidScaled(int i, int tank) {
        FluidTank t = tank == 0 ? inputTank : outputTank;
        return t.getFluidAmount() <= 0 ? 0 : t.getFluidAmount() * i / t.getCapacity();
    }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) {
        super.readFromNBT(nbt, r);
        inputTank.readFromNBT(r, nbt.getCompound("inputTank"));
        outputTank.readFromNBT(r, nbt.getCompound("outputTank"));
        mode = Mode.values()[Math.floorMod(nbt.getInt("mode"), 4)];
    }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) {
        super.writeToNBT(nbt, r);
        nbt.put("inputTank", inputTank.writeToNBT(r, new CompoundTag()));
        nbt.put("outputTank", outputTank.writeToNBT(r, new CompoundTag()));
        nbt.putInt("mode", mode.ordinal());
    }

    @Override public int getTanks() { return 2; }
    @Override public FluidStack getFluidInTank(int tank) { return (tank == 0 ? inputTank : outputTank).getFluid(); }
    @Override public int getTankCapacity(int tank) { return 8000; }
    @Override public boolean isFluidValid(int tank, FluidStack stack) { return tank == 0; }
    @Override public int fill(FluidStack resource, FluidAction action) { return inputTank.fill(resource, action); }
    @Override public FluidStack drain(FluidStack resource, FluidAction action) { return outputTank.drain(resource, action); }
    @Override public FluidStack drain(int maxDrain, FluidAction action) { return outputTank.drain(maxDrain, action); }

    @Override public float getWrenchDropRate() { return 0.85f; }
    @Override public Set<UpgradableProperty> getUpgradableProperties() { return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerCanner(id, inv, this); }
}
