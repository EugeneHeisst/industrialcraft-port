package ic2.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/** Inventory with one fluid tank of tanksize buckets, exposed as the block fluid handler with canFill/canDrain filters. */
public abstract class TileEntityLiquidTankInventory extends TileEntityInventory implements IFluidHandler {
    protected final FluidTank fluidTank;

    protected TileEntityLiquidTankInventory(BlockEntityType<?> type, BlockPos pos, BlockState state, int tanksize) {
        super(type, pos, state);
        fluidTank = new FluidTank(1000 * tanksize) { @Override protected void onContentsChanged() { setChanged(); } };
    }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); fluidTank.readFromNBT(r, nbt.getCompound("fluidTank")); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.put("fluidTank", fluidTank.writeToNBT(r, new CompoundTag())); }

    public FluidTank getFluidTank() { return fluidTank; }
    public FluidStack getFluidStackfromTank() { return fluidTank.getFluid(); }
    public int getTankAmount() { return fluidTank.getFluidAmount(); }
    public int gaugeLiquidScaled(int i) { return fluidTank.getFluidAmount() <= 0 ? 0 : fluidTank.getFluidAmount() * i / fluidTank.getCapacity(); }
    public boolean needsFluid() { return fluidTank.getFluidAmount() <= fluidTank.getCapacity(); }

    public abstract boolean canFill(Direction from, Fluid fluid);
    public abstract boolean canDrain(Direction from, Fluid fluid);

    @Override public int getTanks() { return 1; }
    @Override public FluidStack getFluidInTank(int tank) { return fluidTank.getFluid(); }
    @Override public int getTankCapacity(int tank) { return fluidTank.getCapacity(); }
    @Override public boolean isFluidValid(int tank, FluidStack stack) { return canFill(null, stack.getFluid()); }
    @Override public int fill(FluidStack resource, FluidAction action) { return canFill(null, resource.getFluid()) ? fluidTank.fill(resource, action) : 0; }
    @Override public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, fluidTank.getFluid()) || !canDrain(null, resource.getFluid())) return FluidStack.EMPTY;
        return fluidTank.drain(resource.getAmount(), action);
    }
    @Override public FluidStack drain(int maxDrain, FluidAction action) { return canDrain(null, fluidTank.getFluid().getFluid()) ? fluidTank.drain(maxDrain, action) : FluidStack.EMPTY; }
}
