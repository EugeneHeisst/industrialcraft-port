package ic2.core.jei;

import java.util.List;
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

/** Input → output(s) of one IC2 machine, with the recipe's metadata value (heat, water, hardness, amplification) underneath. */
public class MachineCategory implements IRecipeCategory<MachineCategory.Recipe> {
    public record Recipe(List<ItemStack> inputs, List<ItemStack> outputs, int extra) {}

    private final IC2JeiPlugin.Machine machine;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    public MachineCategory(IGuiHelper gui, IC2JeiPlugin.Machine machine) {
        this.machine = machine;
        this.icon = gui.createDrawableItemLike(machine.block().get());
        this.arrow = gui.getRecipeArrow();
    }

    @Override public RecipeType<Recipe> getRecipeType() { return machine.type(); }
    @Override public Component getTitle() { return machine.title(); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 130; }
    @Override public int getHeight() { return machine.extra() == null ? 26 : 38; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder b, Recipe r, IFocusGroup focuses) {
        b.addInputSlot(4, 5).setStandardSlotBackground().addItemStacks(r.inputs());
        for (int i = 0; i < r.outputs().size(); i++) b.addOutputSlot(56 + i * 20, 5).setOutputSlotBackground().addItemStack(r.outputs().get(i));
    }

    @Override
    public void draw(Recipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
        arrow.draw(g, 25, 4);
        if (machine.extra() != null) g.drawString(Minecraft.getInstance().font, Component.translatable(machine.extraLabel(), r.extra()), 4, 28, 0xFF404040, false);
    }
}
