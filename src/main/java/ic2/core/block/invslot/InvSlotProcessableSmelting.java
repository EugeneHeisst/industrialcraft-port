package ic2.core.block.invslot;

import ic2.api.recipe.RecipeOutput;
import ic2.core.block.TileEntityInventory;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;

public class InvSlotProcessableSmelting extends InvSlotProcessable {
    public InvSlotProcessableSmelting(TileEntityInventory base, String name, int count) { super(base, name, count); }

    public static ItemStack getSmeltingResult(net.minecraft.world.level.Level level, ItemStack input) {
        if (level == null || input.isEmpty()) return ItemStack.EMPTY;
        Optional<ItemStack> ret = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level).map(r -> r.value().getResultItem(level.registryAccess()));
        return ret.orElse(ItemStack.EMPTY);
    }

    @Override public boolean accepts(ItemStack stack) { return !getSmeltingResult(base.getLevel(), stack).isEmpty(); }

    @Override
    public RecipeOutput process() {
        ItemStack input = consume(1, true, true);
        if (input.isEmpty()) return null;
        ItemStack result = getSmeltingResult(base.getLevel(), input);
        return result.isEmpty() ? null : new RecipeOutput(null, result.copy());
    }

    @Override public void consume() { consume(1, false, true); }
}
