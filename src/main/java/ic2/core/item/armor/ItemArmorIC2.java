package ic2.core.item.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** IC2 armor piece; onArmorTick runs every player tick on both sides while worn (ArmorEvents). */
public class ItemArmorIC2 extends ArmorItem {
    public ItemArmorIC2(Holder<ArmorMaterial> material, Type type, Properties properties) { super(material, type, properties); }
    public void onArmorTick(Level level, Player player, ItemStack stack) {}
    /** 0 helmet, 1 chest, 2 legs, 3 boots like armorType of the original. */
    public int armorType() { return switch (getType()) { case HELMET -> 0; case CHESTPLATE, BODY -> 1; case LEGGINGS -> 2; case BOOTS -> 3; }; }
}
