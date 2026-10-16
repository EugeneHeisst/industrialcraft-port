package ic2.core.block.machine.container;

import ic2.core.ContainerBase;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/** Syncs a tank of any fluid to the client GUI as fluid id + amount. */
public final class FluidSync {
    private FluidSync() {}
    public static void tank(ContainerBase<?> c, FluidTank tank) {
        int[] id = {-1};
        c.sync(() -> tank.isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(tank.getFluid().getFluid()), v -> { id[0] = (int)v; apply(tank, id[0], tank.getFluidAmount()); });
        c.sync(tank::getFluidAmount, v -> apply(tank, id[0], (int)v));
    }
    private static void apply(FluidTank tank, int id, int amount) {
        if (id < 0 || amount <= 0) tank.setFluid(FluidStack.EMPTY);
        else tank.setFluid(new FluidStack(BuiltInRegistries.FLUID.byId(id), amount));
    }
}
