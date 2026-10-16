package ic2.core.block.kineticgenerator;

import ic2.core.ContainerBase;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public final class ContainerKineticGenerators {
    private ContainerKineticGenerators() {}

    public static class Wind extends ContainerBase<TileEntityKineticGenerators.Wind> {
        public Wind(int id, Inventory inv, TileEntityKineticGenerators.Wind base) {
            super(id, inv, base);
            addPlayerInventorySlots(inv, 166);
            addSlot(new SlotInvSlot(base.rotorSlot, 0, 80, 26));
            sync(() -> base.windStrength, v -> base.windStrength = v);
        }
    }

    public static class Water extends ContainerBase<TileEntityKineticGenerators.Water> {
        public Water(int id, Inventory inv, TileEntityKineticGenerators.Water base) {
            super(id, inv, base);
            addPlayerInventorySlots(inv, 166);
            addSlot(new SlotInvSlot(base.rotorSlot, 0, 80, 26));
            sync(() -> base.waterFlow, v -> base.waterFlow = (int)v);
            sync(() -> base.type, v -> base.type = (int)v);
            sync(() -> base.checkSpace(base.getRotorDiameter(), true), v -> spaceBlocked = (int)v);
        }
        public int spaceBlocked;
    }

    public static class Electric extends ContainerBase<TileEntityKineticGenerators.Electric> {
        public Electric(int id, Inventory inv, TileEntityKineticGenerators.Electric base) {
            super(id, inv, base);
            addPlayerInventorySlots(inv, 166);
            for (int i = 0; i < 10; i++) addSlot(new SlotInvSlot(base.slotMotor, i, 44 + i % 5 * 18, i < 5 ? 27 : 45));
            addSlot(new SlotInvSlot(base.dischargeSlot, 0, 8, 62));
            sync(() -> base.energy, v -> base.energy = v);
        }
    }
}
