package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityPump;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;

public class ContainerPump extends ContainerElectricMachine<TileEntityPump> {
    public ContainerPump(int id, Inventory inv, TileEntityPump base) {
        super(id, inv, base, 166, 8, 44);
        addSlot(new SlotInvSlot(base.containerSlot, 0, 99, 17));
        addSlot(new SlotInvSlot(base.outputSlot, 0, 132, 34));
        for (int i = 0; i < 4; i++) addSlot(new SlotInvSlot(base.upgradeSlot, i, 152, 8 + i * 18));
        sync(() -> base.guiProgress, v -> base.guiProgress = (float)v);
        sync(() -> base.getFluidStackfromTank().isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(base.getFluidStackfromTank().getFluid()), v -> {
            if (v < 0) base.getFluidTank().setFluid(FluidStack.EMPTY);
            else base.getFluidTank().setFluid(new FluidStack(BuiltInRegistries.FLUID.byId((int)v), Math.max(1, base.getTankAmount())));
        });
        sync(base::getTankAmount, v -> { var f = base.getFluidStackfromTank(); if (v <= 0) base.getFluidTank().setFluid(net.neoforged.neoforge.fluids.FluidStack.EMPTY); else if (!f.isEmpty()) base.getFluidTank().setFluid(f.copyWithAmount(Math.max(1, (int)v))); });
    }
}
