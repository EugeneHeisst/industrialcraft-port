package ic2.core.block.reactor;

import ic2.core.block.invslot.InvSlot;
import net.minecraft.world.item.ItemStack;

/** 9 x 6 reactor grid (index y * 9 + x); only the first 3 + chambers columns are usable. */
public class InvSlotReactor extends InvSlot {
    private final TileEntityNuclearReactor reactor;
    public InvSlotReactor(TileEntityNuclearReactor base, String name) { super(base, name, Access.IO, 54); reactor = base; setStackSizeLimit(1); }
    @Override public boolean accepts(ItemStack stack) { return reactor.isUsefulItem(stack, true); }
    public ItemStack get(int x, int y) { return get(y * 9 + x); }
    public void put(int x, int y, ItemStack stack) { put(y * 9 + x, stack); }
}
