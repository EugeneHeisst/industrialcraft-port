package ic2.api.recipe;

import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public interface IMachineRecipeManager {
    void addRecipe(IRecipeInput input, CompoundTag metadata, ItemStack... outputs);
    /** @param adjustInput consume the input amount from the given stack */
    RecipeOutput getOutputFor(ItemStack input, boolean adjustInput);
    Map<IRecipeInput, RecipeOutput> getRecipes();
}
