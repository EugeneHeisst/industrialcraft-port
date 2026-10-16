package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.core.item.ElectricItemManager;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/** Armor absorbing base ratio x damage ratio of each hit while it can pay energyPerDamage EU per absorbed point. */
public abstract class ItemArmorElectric extends ItemArmorIC2 implements IElectricItem {
    protected final double maxCharge, transferLimit;
    protected final int tier;

    protected ItemArmorElectric(Holder<ArmorMaterial> material, Type type, Properties properties, double maxCharge, double transferLimit, int tier) {
        super(material, type, properties.stacksTo(1).setNoRepair());
        this.maxCharge = maxCharge;
        this.transferLimit = transferLimit;
        this.tier = tier;
    }

    public abstract double getDamageAbsorptionRatio();
    public abstract int getEnergyPerDamage();

    public double getBaseAbsorptionRatio() { return switch (armorType()) { case 0 -> 0.15; case 1 -> 0.4; case 2 -> 0.3; case 3 -> 0.15; default -> 0; }; }

    @Override public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) { return ItemAttributeModifiers.EMPTY; }
    @Override public boolean canProvideEnergy(ItemStack stack) { return false; }
    @Override public double getMaxCharge(ItemStack stack) { return maxCharge; }
    @Override public int getTier(ItemStack stack) { return tier; }
    @Override public double getTransferLimit(ItemStack stack) { return transferLimit; }
    @Override public int getEnchantmentValue() { return 0; }
    @Override public boolean isEnchantable(ItemStack stack) { return false; }
    @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) { return false; }
    @Override public boolean isBarVisible(ItemStack stack) { return true; }
    @Override public int getBarWidth(ItemStack stack) { return Math.round(13f * (float)Mth.clamp(ElectricItemManager.getRawCharge(stack) / maxCharge, 0, 1)); }
    @Override public int getBarColor(ItemStack stack) { return Mth.hsvToRgb((float)Mth.clamp(ElectricItemManager.getRawCharge(stack) / maxCharge, 0, 1) / 3f, 1, 1); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) {
        String tip = ElectricItem.manager.getToolTip(stack);
        if (tip != null) list.add(Component.literal(tip).withStyle(ChatFormatting.GRAY));
        list.add(Component.literal(Component.translatable("ic2.item.tooltip.PowerTier").getString() + " " + tier).withStyle(ChatFormatting.GRAY));
    }

    public ItemStack charged() {
        ItemStack ret = new ItemStack(this);
        ElectricItem.manager.charge(ret, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, false);
        return ret;
    }
}
