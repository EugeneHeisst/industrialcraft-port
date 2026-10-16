package ic2.core.block.generator.container;

import ic2.core.ContainerBase;
import ic2.core.block.generator.tileentity.TileEntityGenerators2;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;

public final class ContainerGenerators2 {
    private ContainerGenerators2() {}

    public static class RTG extends ContainerBase<TileEntityGenerators2.RTG> {
        public RTG(int id, Inventory inv, TileEntityGenerators2.RTG base) {
            super(id, inv, base);
            addPlayerInventorySlots(inv, 166);
            for (int i = 0; i < 6; i++) addSlot(new SlotInvSlot(base.fuelSlot, i, 36 + i % 3 * 18, i < 3 ? 36 : 54));
            sync(() -> base.storage, v -> base.storage = v);
        }
    }

    public static class Semifluid extends ContainerBase<TileEntityGenerators2.Semifluid> {
        public Semifluid(int id, Inventory inv, TileEntityGenerators2.Semifluid base) {
            super(id, inv, base);
            addPlayerInventorySlots(inv, 166);
            addSlot(new SlotInvSlot(base.chargeSlot, 0, 117, 49));
            addSlot(new SlotInvSlot(base.fluidSlot, 0, 27, 21));
            addSlot(new SlotInvSlot(base.outputSlot, 0, 27, 54));
            sync(() -> base.storage, v -> base.storage = v);
            sync(() -> base.getTankAmount(), v -> { var f = base.getFluidTank().getFluid(); base.getFluidTank().setFluid(v <= 0 || f.isEmpty() && fluidId < 0 ? FluidStack.EMPTY : new FluidStack(f.isEmpty() ? BuiltInRegistries.FLUID.byId(fluidId) : f.getFluid(), (int)v)); });
            sync(() -> base.getFluidTank().isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(base.getFluidTank().getFluid().getFluid()), v -> { fluidId = (int)v; if (v >= 0 && !base.getFluidTank().isEmpty()) base.getFluidTank().setFluid(new FluidStack(BuiltInRegistries.FLUID.byId((int)v), base.getTankAmount())); });
        }
        private int fluidId = -1;
    }

    public static class Stirling extends ContainerBase<TileEntityGenerators2.Stirling> {
        public Stirling(int id, Inventory inv, TileEntityGenerators2.Stirling base) {
            super(id, inv, base);
            addPlayerInventorySlots(inv, 166);
            sync(() -> base.EUstorage, v -> base.EUstorage = v);
            sync(() -> base.receivedheat, v -> base.receivedheat = (int)v);
            sync(() -> base.production, v -> base.production = v);
        }
    }

    public static class Kinetic extends ContainerBase<TileEntityGenerators2.Kinetic> {
        public Kinetic(int id, Inventory inv, TileEntityGenerators2.Kinetic base) {
            super(id, inv, base);
            addPlayerInventorySlots(inv, 166);
            sync(() -> base.EUstorage, v -> base.EUstorage = v);
            sync(() -> base.guiproduction, v -> base.guiproduction = v);
        }
    }
}
