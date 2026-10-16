package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityOreWashing;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public class ContainerOreWashing extends ContainerStandardMachine<TileEntityOreWashing> {
    public ContainerOreWashing(int id, Inventory inventory, TileEntityOreWashing base) {
        super(id, inventory, base, 166, 8, 62, 104, 17, -1000, 0, 152, 8);
        for (int i = 0; i < 3; i++) addSlot(new SlotInvSlot(base.outputSlot, i, 86 + i * 18, 62));
        addSlot(new SlotInvSlot(base.fluidSlot, 0, 38, 17));
        addSlot(new SlotInvSlot(base.cellSlot, 0, 38, 62));
        sync(base::getTankAmount, v -> base.getFluidTank().setFluid(v <= 0 ? FluidStack.EMPTY : new FluidStack(Fluids.WATER, (int)v)));
    }
}
