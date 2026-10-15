package ic2.api.recipe;

import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class RecipeOutput {
    public final List<ItemStack> items;
    public final CompoundTag metadata;
    public RecipeOutput(CompoundTag metadata, List<ItemStack> items) { this.metadata = metadata; this.items = items; }
    public RecipeOutput(CompoundTag metadata, ItemStack... items) { this(metadata, Arrays.asList(items)); }
    @Override public String toString() { return "ROutput<" + items + "," + metadata + ">"; }
}
