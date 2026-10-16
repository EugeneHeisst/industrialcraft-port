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
        FluidStack f = contained(stack);
        return !f.isEmpty() && (fluids.contains(f.getFluid()) || f.getFluid() == Fluids.FLOWING_WATER && fluids.contains(Fluids.WATER));
    }

    /** Empties one container into the tank; the emptied container goes to output. */
    public boolean transferToTank(IFluidHandler tank, MutableObject<ItemStack> output, boolean simulate) {
        ItemStack stack = get();
        if (stack.isEmpty() || !accepts(stack)) return false;
        ItemStack single = stack.copyWithCount(1);
        var handler = single.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) return false;
        FluidStack available = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (tank.fill(available, IFluidHandler.FluidAction.SIMULATE) < available.getAmount()) return false;
        FluidStack drained = handler.drain(available, IFluidHandler.FluidAction.EXECUTE);
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
