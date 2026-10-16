package ic2.api.item;

import net.minecraft.world.level.Level;

/** Terraformer blueprint. */
public interface ITerraformingBP {
    int getConsume();
    int getRange();
    boolean terraform(Level level, int x, int z, int yCoord);
}
