package ic2.core.item.armor;

import ic2.core.ref.Ic2ArmorMaterials;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

/** 1 MEU, 1600 EU/t, tier 3, 90% of the base absorption, 5000 EU per damage; boots stop short falls, helmet has night vision. */
public class ItemArmorNanoSuit extends ItemArmorElectric {
    public ItemArmorNanoSuit(Type type, Properties properties) { super(Ic2ArmorMaterials.NANO, type, properties.rarity(Rarity.UNCOMMON), 1000000, 1600, 3); }

    @Override public double getDamageAbsorptionRatio() { return 0.9; }
    @Override public int getEnergyPerDamage() { return 5000; }

    @Override
    public void onArmorTick(Level level, Player player, ItemStack stack) {
        if (armorType() != 0) return;
        ArmorUtil.hudMode(level, player, stack);
        if (ArmorUtil.nightvision(level, player, stack, "Nightvision")) player.inventoryMenu.broadcastChanges();
    }
}
