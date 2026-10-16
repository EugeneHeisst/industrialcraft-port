package ic2.api.recipe;

import java.util.Map;
import net.minecraft.world.item.ItemStack;

/** Solid canning: container + fill -> output (tin cans, fuel rods). */
public interface ICannerBottleRecipeManager {
    void addRecipe(IRecipeInput container, IRecipeInput fill, ItemStack output);
    /** @param acceptTest with an empty container or fill: whether the other one could be part of any recipe */
    RecipeOutput getOutputFor(ItemStack container, ItemStack fill, boolean adjustInput, boolean acceptTest);
    Map<Input, RecipeOutput> getRecipes();

    record Input(IRecipeInput container, IRecipeInput fill) {
        public boolean matches(ItemStack c, ItemStack f) { return (c.isEmpty() || container.matches(c)) && fill.matches(f); }
    }
}
