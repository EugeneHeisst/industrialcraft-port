package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityMatter;
import ic2.core.fluid.Ic2Fluids;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;

public class ContainerMatter extends ContainerElectricMachine<TileEntityMatter> {
    public ContainerMatter(int id, Inventory inv, TileEntityMatter base) {
        super(id, inv, base, 166, -1000, 0);
        addSlot(new SlotInvSlot(base.amplifierSlot, 0, 72, 40));
        addSlot(new SlotInvSlot(base.outputSlot, 0, 125, 59));
        addSlot(new SlotInvSlot(base.containerslot, 0, 125, 23));
        for (int i = 0; i < 4; i++) addSlot(new SlotInvSlot(base.upgradeSlot, i, 152, 8 + i * 18));
        sync(() -> base.energy, v -> base.energy = v);
        sync(() -> base.scrap, v -> base.scrap = (int)v);
        sync(base::getTankAmount, v -> base.getFluidTank().setFluid(v <= 0 ? FluidStack.EMPTY : new FluidStack(Ic2Fluids.UU_MATTER.get(), (int)v)));
    }
}
