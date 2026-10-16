package ic2.core.item.resources;

import ic2.api.item.IBlockCuttingBlade;
import net.minecraft.world.item.Item;

public class ItemBlockCuttingBlade extends Item implements IBlockCuttingBlade {
    private final int hardness;
    public ItemBlockCuttingBlade(Properties properties, int hardness) { super(properties); this.hardness = hardness; }
    @Override public int gethardness() { return hardness; }
}
