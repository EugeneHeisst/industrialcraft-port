package ic2.core.item.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

/** Batpack, advanced batpack, energy pack and lappack: chest storage that powers tools in use. */
public class ItemArmorBatpack extends ItemArmorElectric {
    public ItemArmorBatpack(Holder<ArmorMaterial> material, Properties properties, double maxCharge, double transferLimit, int tier) { super(material, Type.CHESTPLATE, properties, maxCharge, transferLimit, tier); }
    @Override public boolean canProvideEnergy(ItemStack stack) { return true; }
    @Override public double getDamageAbsorptionRatio() { return 0; }
    @Override public int getEnergyPerDamage() { return 0; }
}
