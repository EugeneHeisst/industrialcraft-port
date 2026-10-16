package ic2.api.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Armor counted as metal, e.g. boots that let the player ride magnetized iron fences up at full speed. */
public interface IMetalArmor {
    boolean isMetalArmor(ItemStack stack, Player player);
}
