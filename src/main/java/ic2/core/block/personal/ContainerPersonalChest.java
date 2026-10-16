package ic2.core.block.personal;

import ic2.core.ContainerBase;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ContainerPersonalChest extends ContainerBase<TileEntityPersonalChest> {
    public ContainerPersonalChest(int id, Inventory inventory, TileEntityPersonalChest base) {
        super(id, inventory, base);
        if (!inventory.player.level().isClientSide) base.openChest();
        for (int y = 0; y < base.contentSlot.size() / 9; y++)
            for (int x = 0; x < 9; x++) addSlot(new SlotInvSlot(base.contentSlot, x + y * 9, 8 + x * 18, 18 + y * 18));
        addPlayerInventorySlots(inventory, 222);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) base.closeChest();
    }
}
