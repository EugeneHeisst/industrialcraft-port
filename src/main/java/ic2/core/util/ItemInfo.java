package ic2.core.util;

import ic2.api.info.IEnergyValueProvider;
import ic2.api.info.IFuelValueProvider;
import ic2.core.ref.Ic2Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemInfo implements IEnergyValueProvider, IFuelValueProvider {
    @Override
    public double getEnergyValue(ItemStack stack) {
        if (stack.is(Ic2Items.SU_BATTERY.get())) return 1200;
        if (stack.is(Items.REDSTONE)) return 800;
        return stack.is(Ic2Items.get("energium_dust")) ? 16000 : 0;
    }

    @Override
    public int getFuelValue(ItemStack stack, boolean allowLava) {
        if (stack.is(Items.LAVA_BUCKET)) return allowLava ? 2000 : 0;
        return stack.getBurnTime(null);
    }
}
