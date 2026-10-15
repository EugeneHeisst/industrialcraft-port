package ic2.api.recipe;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public class RecipeInputItemStack implements IRecipeInput {
    public final ItemStack input;
    public final int amount;
    public RecipeInputItemStack(ItemStack input) { this(input, input.getCount()); }
    public RecipeInputItemStack(ItemStack input, int amount) {
        if (input.isEmpty()) throw new IllegalArgumentException("Invalid item stack specified");
        this.input = input.copyWithCount(1);
        this.amount = amount;
    }
    @Override public boolean matches(ItemStack subject) { return subject.is(input.getItem()); }
    @Override public int getAmount() { return amount; }
    @Override public List<ItemStack> getInputs() { return List.of(input.copyWithCount(amount)); }
    @Override public String toString() { return "RInputItemStack<" + amount + "x" + input + ">"; }
}
