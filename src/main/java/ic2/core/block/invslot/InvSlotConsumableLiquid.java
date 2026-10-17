package ic2.core.block.invslot;

import ic2.core.block.TileEntityInventory;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.mutable.MutableObject;

/** Fluid container slot (InvSlotConsumableLiquidByList): accepts containers of the listed fluids and empties them into a tank. */
public class InvSlotConsumableLiquid extends InvSlotConsumable {
    private final Set<Fluid> fluids;
    /** Fill mode: accepts containers that can take one of the fluids, or any fluid when none are listed (the original OpType.Fill). */
    public boolean fillMode;

    public InvSlotConsumableLiquid(TileEntityInventory base, String name, Access access, int count, InvSide side, Fluid... fluids) {
        super(base, name, access, count, side);
        this.fluids = Set.of(fluids);
    }

    public static FluidStack contained(ItemStack stack) {
        var handler = stack.copyWithCount(1).getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) return FluidStack.EMPTY;
        return handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
    }

    @Override
    public boolean accepts(ItemStack stack) {
        if (fillMode) {
            var handler = stack.copyWithCount(1).getCapability(Capabilities.FluidHandler.ITEM);
            if (handler == null) return false;
            if (fluids.isEmpty()) return handler.getTanks() > 0 && handler.getFluidInTank(0).getAmount() < handler.getTankCapacity(0);
            for (Fluid fl : fluids) if (handler.fill(new FluidStack(fl, Integer.MAX_VALUE), IFluidHandler.FluidAction.SIMULATE) > 0) return true;
            return false;
        }
        FluidStack f = contained(stack);
        return !f.isEmpty() && (fluids.contains(f.getFluid()) || f.getFluid() == Fluids.FLOWING_WATER && fluids.contains(Fluids.WATER));
    }

    /** Fills one container from the tank; the filled container goes to output. */
    public boolean transferFromTank(IFluidHandler tank, MutableObject<ItemStack> output, boolean simulate) {
        ItemStack stack = get();
        if (stack.isEmpty() || !accepts(stack)) return false;
        ItemStack single = stack.copyWithCount(1);
        var handler = single.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) return false;
        FluidStack inTank = tank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (inTank.isEmpty()) return false;
        int accepted = handler.fill(inTank.copy(), IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0 || accepted > inTank.getAmount()) return false;
        FluidStack drained = tank.drain(inTank.copyWithAmount(accepted), IFluidHandler.FluidAction.SIMULATE);
        if (drained.getAmount() < accepted) return false;
        handler.fill(drained.copy(), IFluidHandler.FluidAction.EXECUTE);
        output.setValue(handler.getContainer());
        if (!simulate) {
            tank.drain(inTank.copyWithAmount(accepted), IFluidHandler.FluidAction.EXECUTE);
            stack.shrink(1);
            if (stack.isEmpty()) put(ItemStack.EMPTY);
        }
        return true;
    }

    /** Empties one container into the tank; the emptied container goes to output. */
    public boolean transferToTank(IFluidHandler tank, MutableObject<ItemStack> output, boolean simulate) {
        ItemStack stack = get();
        if (stack.isEmpty() || !accepts(stack)) return false;
        ItemStack single = stack.copyWithCount(1);
        var handler = single.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) return false;
        FluidStack available = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        int accepted = tank.fill(available, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) return false;
        FluidStack drained = handler.drain(available.copyWithAmount(accepted), IFluidHandler.FluidAction.EXECUTE);
        // Buckets are atomic; large fluid items may return only the amount the tank can actually accept.
        if (drained.isEmpty() || drained.getAmount() > accepted) return false;
        ItemStack container = handler.getContainer();
        output.setValue(container.isEmpty() ? null : container);
        if (!simulate) {
            tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            stack.shrink(1);
            if (stack.isEmpty()) put(ItemStack.EMPTY);
        }
        return true;
    }
}
