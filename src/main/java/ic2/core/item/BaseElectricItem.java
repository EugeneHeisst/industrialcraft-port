package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public abstract class BaseElectricItem extends Item implements IElectricItem {
    protected final double maxCharge, transferLimit;
    protected final int tier;

    protected BaseElectricItem(Properties properties, double maxCharge, double transferLimit, int tier) {
        super(properties.setNoRepair());
        this.maxCharge = maxCharge;
        this.transferLimit = transferLimit;
        this.tier = tier;
    }

    @Override public boolean canProvideEnergy(ItemStack stack) { return false; }
    @Override public double getMaxCharge(ItemStack stack) { return maxCharge; }
    @Override public int getTier(ItemStack stack) { return tier; }
    @Override public double getTransferLimit(ItemStack stack) { return transferLimit; }

    /** Durability-style charge bar like the damage values the original kept in sync with the charge. */
    @Override public boolean isBarVisible(ItemStack stack) { return true; }
    @Override public int getBarWidth(ItemStack stack) { return Math.round(13f * (float)Mth.clamp(ElectricItemManager.getRawCharge(stack) / getMaxCharge(stack), 0, 1)); }
    @Override public int getBarColor(ItemStack stack) {
        float f = (float)Mth.clamp(ElectricItemManager.getRawCharge(stack) / getMaxCharge(stack), 0, 1);
        return Mth.hsvToRgb(f / 3f, 1, 1);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) {
        String tip = ElectricItem.manager.getToolTip(stack);
        if (tip != null) list.add(Component.literal(tip).withStyle(ChatFormatting.GRAY));
    }

    public ItemStack charged() {
        ItemStack ret = new ItemStack(this);
        ElectricItem.manager.charge(ret, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, false);
        return ret;
    }

    /** Texture index 0..4 of the battery icons (empty .. full), same steps as the original damage mapping. */
    public static int chargeLevel(ItemStack stack, double maxCharge) {
        int maxDamage = 27;
        double charge = ElectricItemManager.getRawCharge(stack);
        int meta = maxDamage - 1 - (int)ic2.core.util.Util.map(charge, maxCharge, maxDamage - 2);
        if (meta <= 1) return 4;
        if (meta >= maxDamage - 1) return 0;
        return 3 - 3 * (meta - 2) / (maxDamage - 4 + 1);
    }
}
