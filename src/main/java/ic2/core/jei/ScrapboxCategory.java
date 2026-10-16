package ic2.core.jei;

import ic2.core.IC2;
import ic2.core.ref.Ic2Items;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Scrap box drops with their chance. */
public class ScrapboxCategory implements IRecipeCategory<ScrapboxCategory.Recipe> {
    public record Recipe(ItemStack drop, float chance) {}
    public static final RecipeType<Recipe> TYPE = RecipeType.create(IC2.MODID, "scrap_box", Recipe.class);
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    public ScrapboxCategory(IGuiHelper gui) { icon = gui.createDrawableItemLike(Ic2Items.SCRAP_BOX.get()); arrow = gui.getRecipeArrow(); }

    @Override public RecipeType<Recipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("item.ic2.scrap_box"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 130; }
    @Override public int getHeight() { return 26; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder b, Recipe r, IFocusGroup focuses) {
        b.addInputSlot(4, 5).setStandardSlotBackground().addItemStack(new ItemStack(Ic2Items.SCRAP_BOX.get()));
        b.addOutputSlot(56, 5).setOutputSlotBackground().addItemStack(r.drop());
    }

    @Override
    public void draw(Recipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
        arrow.draw(g, 25, 4);
        g.drawString(Minecraft.getInstance().font, String.format(java.util.Locale.ROOT, "%.2f%%", r.chance() * 100), 80, 9, 0xFF404040, false);
    }
}
