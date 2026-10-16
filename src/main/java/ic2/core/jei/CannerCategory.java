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

/** Solid canning: container + filling → canned item. */
public class CannerCategory implements IRecipeCategory<CannerCategory.Recipe> {
    public record Recipe(List<ItemStack> container, List<ItemStack> fill, ItemStack output) {}
    public static final RecipeType<Recipe> TYPE = RecipeType.create(IC2.MODID, "solid_canning", Recipe.class);
    private final IDrawable icon;
    private final IDrawableStatic arrow, plus;

    public CannerCategory(IGuiHelper gui) { icon = gui.createDrawableItemLike(Ic2Blocks.SOLID_CANNER.get()); arrow = gui.getRecipeArrow(); plus = gui.getRecipePlusSign(); }

    @Override public RecipeType<Recipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("block.ic2.solid_canner"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 110; }
    @Override public int getHeight() { return 26; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder b, Recipe r, IFocusGroup focuses) {
        b.addInputSlot(4, 5).setStandardSlotBackground().addItemStacks(r.container());
        b.addInputSlot(36, 5).setStandardSlotBackground().addItemStacks(r.fill());
        b.addOutputSlot(88, 5).setOutputSlotBackground().addItemStack(r.output());
    }

    @Override
    public void draw(Recipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) { plus.draw(g, 24, 8); arrow.draw(g, 58, 4); }
}
