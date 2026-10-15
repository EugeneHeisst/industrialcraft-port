package ic2.core;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/** Block entity with a GUI; the same container class is built on both sides from the block entity. */
public interface IHasGui {
    ContainerBase<?> getGuiContainer(int containerId, Inventory inventory);
    default void onGuiClosed(Player player) {}
}
