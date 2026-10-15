package ic2.core.block.invslot;

import ic2.api.recipe.RecipeOutput;
import ic2.core.block.TileEntityInventory;
import net.minecraft.world.item.ItemStack;

public abstract class InvSlotProcessable extends InvSlotConsumable {
    public InvSlotProcessable(TileEntityInventory base, String name, int count) { super(base, name, count); }
    @Override public abstract boolean accepts(ItemStack stack);
    public abstract RecipeOutput process();
    public abstract void consume();
}
