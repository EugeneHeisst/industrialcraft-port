package ic2.api.recipe;

import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/** Fluid enrichment in the canning machine: fluid + additive item -> fluid. Outputs carry the fluid in metadata ("fluid", "amount"). */
public interface ICannerEnrichRecipeManager {
    void addRecipe(FluidStack fluid, IRecipeInput additive, FluidStack output);
    RecipeOutput getOutputFor(FluidStack fluid, ItemStack additive, boolean adjustInput, boolean acceptTest);
    Map<Input, FluidStack> getRecipes();

    record Input(FluidStack fluid, IRecipeInput additive) {
        public boolean matches(FluidStack f, ItemStack a) { return (fluid.isEmpty() || FluidStack.isSameFluidSameComponents(fluid, f)) && additive.matches(a); }
    }
}
