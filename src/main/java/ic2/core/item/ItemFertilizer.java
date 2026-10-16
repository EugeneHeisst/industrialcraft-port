package ic2.core.item;
import ic2.api.item.IBoxable;
import net.minecraft.world.item.*;
public class ItemFertilizer extends Item implements IBoxable {
    public ItemFertilizer(Properties p){super(p);}
    @Override public boolean canBeStoredInToolbox(ItemStack s){return true;}
}
