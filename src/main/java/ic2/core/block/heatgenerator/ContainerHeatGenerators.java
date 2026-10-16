package ic2.core.block.heatgenerator;

import ic2.core.ContainerBase;
import ic2.core.block.TileEntityHeatSourceInventory;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public final class ContainerHeatGenerators {
    private ContainerHeatGenerators() {}

    static abstract class Base<T extends TileEntityHeatSourceInventory> extends ContainerBase<T> {
        Base(int id, Inventory inv, T base) {
            super(id, inv, base);
            addPlayerInventorySlots(inv, 166);
            sync(() -> base.transmitHeat, v -> base.transmitHeat = (int)v);
            sync(() -> base.maxHeatEmitpeerTick, v -> base.maxHeatEmitpeerTick = (int)v);
        }
    }

    public static class Solid extends Base<TileEntityHeatGenerators.Solid> {
        public Solid(int id, Inventory inv, TileEntityHeatGenerators.Solid base) {
            super(id, inv, base);
            addSlot(new SlotInvSlot(base.fuelSlot, 0, 80, 45));
            addSlot(new SlotInvSlot(base.outputslot, 0, 113, 45));
            sync(() -> base.fuel, v -> base.fuel = (int)v);
            sync(() -> base.itemFuelTime, v -> base.itemFuelTime = (int)v);
        }
    }

    public static class RT extends Base<TileEntityHeatGenerators.RT> {
        public RT(int id, Inventory inv, TileEntityHeatGenerators.RT base) {
            super(id, inv, base);
            for (int i = 0; i < 6; i++) addSlot(new SlotInvSlot(base.fuelSlot, i, 62 + i % 3 * 18, i < 3 ? 27 : 45));
        }
    }

    public static class Electric extends Base<TileEntityHeatGenerators.Electric> {
        public Electric(int id, Inventory inv, TileEntityHeatGenerators.Electric base) {
            super(id, inv, base);
            for (int i = 0; i < 10; i++) addSlot(new SlotInvSlot(base.CoilSlot, i, 44 + i % 5 * 18, i < 5 ? 27 : 45));
            addSlot(new SlotInvSlot(base.dischargeSlot, 0, 8, 62));
            sync(() -> base.energy, v -> base.energy = v);
        }
    }
}
