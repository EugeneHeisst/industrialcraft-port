package ic2.core;

import ic2.api.recipe.IListRecipeManager;
import ic2.api.recipe.IRecipeInput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class BasicListRecipeManager implements IListRecipeManager {
    private final List<IRecipeInput> list = new ArrayList<>();
    @Override public Iterator<IRecipeInput> iterator() { return list.iterator(); }
    @Override public void add(IRecipeInput input) { list.add(input); }
    @Override public boolean contains(ItemStack stack) { for (IRecipeInput in : list) if (in.matches(stack)) return true; return false; }
    @Override public boolean isEmpty() { return list.isEmpty(); }
    @Override public List<IRecipeInput> getInputs() { return list; }
}
