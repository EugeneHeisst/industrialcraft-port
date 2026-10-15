package ic2.core.block.wiring;

import ic2.core.ContainerBase;
import ic2.core.slot.SlotArmor;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerElectricBlock extends ContainerBase<TileEntityElectricBlock> {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    public ContainerElectricBlock(int id, Inventory inventory, TileEntityElectricBlock base) {
        super(id, inventory, base);
        addPlayerInventorySlots(inventory, 196);
        for (int col = 0; col < 4; col++) addSlot(new SlotArmor(inventory, ARMOR[col], 8 + col * 18, 84));
        addSlot(new SlotInvSlot(base.chargeSlot, 0, 56, 17));
        addSlot(new SlotInvSlot(base.dischargeSlot, 0, 56, 53));
        sync(() -> base.energy, v -> base.energy = v);
        sync(() -> base.redstoneMode, v -> base.redstoneMode = (byte)v);
    }

    @Override public void onContainerEvent(String event) { if ("redstone".equals(event)) base.cycleRedstoneMode(player); }
}
