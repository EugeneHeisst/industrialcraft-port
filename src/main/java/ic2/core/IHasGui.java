package ic2.core;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/** Block entity with a GUI; the same container class is built on both sides from the block entity. */
public interface IHasGui {
    ContainerBase<?> getGuiContainer(int containerId, Inventory inventory);
    default void onGuiClosed(Player player) {}
    /** Server: extra data after the position in the menu open packet (owner/admin view of the personal blocks). */
    default void writeGuiData(Player player, net.minecraft.network.RegistryFriendlyByteBuf buf) {}
    /** Client: builds the container from the data written by {@link #writeGuiData}. */
    default ContainerBase<?> getGuiContainer(int containerId, Inventory inventory, net.minecraft.network.RegistryFriendlyByteBuf buf) { return getGuiContainer(containerId, inventory); }
}
