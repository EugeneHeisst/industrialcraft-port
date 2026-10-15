package ic2.api.recipe;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Ore dictionary input of the original; ore dictionary names are item tags now (see ic2.core.util.OreDict). */
public class RecipeInputOreDict implements IRecipeInput {
    public final TagKey<Item> input;
    public final int amount;
    public RecipeInputOreDict(TagKey<Item> input) { this(input, 1); }
    public RecipeInputOreDict(TagKey<Item> input, int amount) { this.input = input; this.amount = amount; }
    public RecipeInputOreDict(String tag, int amount) { this(TagKey.create(Registries.ITEM, ResourceLocation.parse(tag)), amount); }
    @Override public boolean matches(ItemStack subject) { return subject.is(input); }
    @Override public int getAmount() { return amount; }
    @Override public List<ItemStack> getInputs() {
        var ret = new ArrayList<ItemStack>();
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(input)) ret.add(new ItemStack(holder, amount));
        return ret;
    }
    @Override public String toString() { return "RInputOreDict<" + amount + "x#" + input.location() + ">"; }
}
