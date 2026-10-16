package ic2.core.jei;

import ic2.core.IC2;
import ic2.core.ref.Ic2Blocks;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/** Canning machine enrichment: fluid + additive → fluid (coolant, biomass, construction foam...). */
public class EnrichCategory implements IRecipeCategory<EnrichCategory.Recipe> {
    public record Recipe(FluidStack fluid, List<ItemStack> additive, FluidStack output) {}
    public static final RecipeType<Recipe> TYPE = RecipeType.create(IC2.MODID, "fluid_enrichment", Recipe.class);
    private final IDrawable icon;
    private final IDrawableStatic arrow, plus;

    public EnrichCategory(IGuiHelper gui) { icon = gui.createDrawableItemLike(Ic2Blocks.CANNER.get()); arrow = gui.getRecipeArrow(); plus = gui.getRecipePlusSign(); }

    @Override public RecipeType<Recipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("block.ic2.canner"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 110; }
    @Override public int getHeight() { return 26; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder b, Recipe r, IFocusGroup focuses) {
        var in = b.addInputSlot(4, 5).setStandardSlotBackground();
        if (!r.fluid().isEmpty()) in.addFluidStack(r.fluid().getFluid(), r.fluid().getAmount());
        b.addInputSlot(36, 5).setStandardSlotBackground().addItemStacks(r.additive());
        b.addOutputSlot(88, 5).setOutputSlotBackground().addFluidStack(r.output().getFluid(), r.output().getAmount());
    }

    @Override
    public void draw(Recipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) { plus.draw(g, 24, 8); arrow.draw(g, 58, 4); }
}
