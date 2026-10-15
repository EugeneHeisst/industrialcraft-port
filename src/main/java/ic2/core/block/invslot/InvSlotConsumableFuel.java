package ic2.core.block.invslot;

import ic2.api.info.Info;
import ic2.core.block.TileEntityInventory;
import net.minecraft.world.item.ItemStack;

public class InvSlotConsumableFuel extends InvSlotConsumable {
    public final boolean allowLava;
    public InvSlotConsumableFuel(TileEntityInventory base, String name, int count, boolean allowLava) { super(base, name, Access.I, count, InvSide.SIDE); this.allowLava = allowLava; }
    @Override public boolean accepts(ItemStack stack) { return Info.itemFuel.getFuelValue(stack, allowLava) > 0; }
    public int consumeFuel() {
        ItemStack fuel = consume(1);
        return fuel.isEmpty() ? 0 : Info.itemFuel.getFuelValue(fuel, allowLava);
    }
}
