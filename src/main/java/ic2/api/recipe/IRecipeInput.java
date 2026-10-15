package ic2.api.recipe;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public interface IRecipeInput {
    boolean matches(ItemStack subject);
    int getAmount();
    /** Example stacks for recipe viewers. */
    List<ItemStack> getInputs();
}
