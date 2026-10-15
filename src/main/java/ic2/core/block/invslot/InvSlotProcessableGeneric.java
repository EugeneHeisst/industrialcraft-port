package ic2.core.block.invslot;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.RecipeOutput;
import ic2.core.block.TileEntityInventory;
import ic2.core.item.ItemUpgradeModule;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class InvSlotProcessableGeneric extends InvSlotProcessable {
    public IMachineRecipeManager recipeManager;

    public InvSlotProcessableGeneric(TileEntityInventory base, String name, int count, IMachineRecipeManager recipeManager) {
        super(base, name, count);
        this.recipeManager = recipeManager;
    }

    @Override
    public boolean accepts(ItemStack stack) {
        if (stack.getItem() instanceof ItemUpgradeModule) return false;
        return getOutputFor(stack.copyWithCount(Integer.MAX_VALUE), false, true) != null;
    }

    @Override
    public RecipeOutput process() {
        ItemStack input = get();
        if (input.isEmpty() && !allowEmptyInput()) return null;
        RecipeOutput output = getOutputFor(input, false, false);
        if (output == null) return null;
        List<ItemStack> copy = new ArrayList<>(output.items.size());
        for (ItemStack s : output.items) copy.add(s.copy());
        return new RecipeOutput(output.metadata, copy);
    }

    @Override
    public void consume() {
        ItemStack input = get();
        if (input.isEmpty() && !allowEmptyInput()) throw new IllegalStateException("consume from empty slot");
        ItemStack remainder = input.hasCraftingRemainingItem() ? input.getCraftingRemainingItem() : ItemStack.EMPTY;
        if (getOutputFor(input, true, false) == null) throw new IllegalStateException("consume without a processing result");
        if (input.getCount() <= 0) put(remainder);
        else onChanged();
    }

    public void setRecipeManager(IMachineRecipeManager recipeManager) { this.recipeManager = recipeManager; }
    protected RecipeOutput getOutputFor(ItemStack input, boolean adjustInput, boolean forAccept) { return recipeManager.getOutputFor(input, adjustInput); }
    protected boolean allowEmptyInput() { return false; }
}
