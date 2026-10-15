package ic2.api.tile;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IWrenchable {
    boolean wrenchCanSetFacing(Player player, Direction side);
    Direction getFacing();
    void setFacing(Direction facing);
    boolean wrenchCanRemove(Player player);
    float getWrenchDropRate();
    ItemStack getWrenchDrop(Player player);
}
