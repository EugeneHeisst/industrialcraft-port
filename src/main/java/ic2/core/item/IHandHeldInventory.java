package ic2.core.item;

import ic2.core.ContainerBase;
import ic2.core.ref.Ic2Menus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Items with a GUI of their own; opened through the ic2:handheld menu type. */
public interface IHandHeldInventory {
    ContainerBase<?> createContainer(int id, Inventory inv, ItemStack stack, InteractionHand hand);

    static void open(ServerPlayer player, InteractionHand hand, java.util.function.Function<Integer, ContainerBase<?>> server) {
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> {
            var menu = server.apply(id);
            // The menu may initialize item identity/data; deliver the held stack before the open-screen payload.
            player.inventoryMenu.broadcastChanges();
            return menu;
        }, Component.empty()), buf -> buf.writeVarInt(hand.ordinal()));
    }
}
