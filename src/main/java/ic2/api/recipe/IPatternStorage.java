package ic2.api.recipe;

import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Pattern storage next to a scanner or replicator. */
public interface IPatternStorage {
    boolean addPattern(ItemStack stack);
    List<ItemStack> getPatterns();
}
