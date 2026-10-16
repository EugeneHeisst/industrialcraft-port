package ic2.core.block.generator.container;

import ic2.core.ContainerBase;
import ic2.core.block.generator.tileentity.TileEntityGeoGenerator;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public class ContainerGeoGenerator extends ContainerBase<TileEntityGeoGenerator> {
    public ContainerGeoGenerator(int id, Inventory inventory, TileEntityGeoGenerator base) {
        super(id, inventory, base);
        addPlayerInventorySlots(inventory, 166);
        addSlot(new SlotInvSlot(base.chargeSlot, 0, 117, 49));
        addSlot(new SlotInvSlot(base.fluidSlot, 0, 27, 21));
        addSlot(new SlotInvSlot(base.outputSlot, 0, 27, 54));
        sync(() -> base.storage, v -> base.storage = v);
        sync(() -> base.getTankAmount(), v -> base.getFluidTank().setFluid(v <= 0 ? FluidStack.EMPTY : new FluidStack(Fluids.LAVA, (int)v)));
    }
}
