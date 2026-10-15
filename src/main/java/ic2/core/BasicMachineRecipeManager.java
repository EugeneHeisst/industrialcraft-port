package ic2.core;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.RecipeOutput;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class BasicMachineRecipeManager implements IMachineRecipeManager {
    private final Map<IRecipeInput, RecipeOutput> recipes = new LinkedHashMap<>();

    @Override
    public void addRecipe(IRecipeInput input, CompoundTag metadata, ItemStack... outputs) {
        for (IRecipeInput existing : recipes.keySet())
            for (ItemStack s : input.getInputs()) if (existing.matches(s)) { IC2.LOG.warn("ambiguous recipe: {} -> {}", input, java.util.Arrays.asList(outputs)); return; }
        recipes.put(input, new RecipeOutput(metadata, outputs));
    }

    @Override
    public RecipeOutput getOutputFor(ItemStack input, boolean adjustInput) {
        if (input.isEmpty()) return null;
        for (var e : recipes.entrySet()) {
            IRecipeInput in = e.getKey();
            if (!in.matches(input)) continue;
            boolean container = input.hasCraftingRemainingItem();
            if (input.getCount() < in.getAmount() || container && input.getCount() != in.getAmount()) return null;
            if (adjustInput) {
                if (container) input.setCount(0);
                else input.shrink(in.getAmount());
            }
            return e.getValue();
        }
        return null;
    }

    @Override public Map<IRecipeInput, RecipeOutput> getRecipes() { return recipes; }
}
