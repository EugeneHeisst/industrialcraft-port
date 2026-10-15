package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityStandardMachine;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerStandardMachine<T extends TileEntityStandardMachine> extends ContainerElectricMachine<T> {
    public ContainerStandardMachine(int id, Inventory inventory, T base) { this(id, inventory, base, 166, 56, 53, 56, 17, 116, 35, 152, 8); }

    public ContainerStandardMachine(int id, Inventory inventory, T base, int height, int dischargeX, int dischargeY, int inputX, int inputY, int outputX, int outputY, int upgradeX, int upgradeY) {
        super(id, inventory, base, height, dischargeX, dischargeY);
        if (base.inputSlot != null) addSlot(new SlotInvSlot(base.inputSlot, 0, inputX, inputY));
        if (base.outputSlot != null) addSlot(new SlotInvSlot(base.outputSlot, 0, outputX, outputY));
        for (int i = 0; i < 4; i++) addSlot(new SlotInvSlot(base.upgradeSlot, i, upgradeX, upgradeY + i * 18));
        sync(() -> base.guiProgress, v -> base.guiProgress = (float)v);
    }
}
