package ic2.core.item.reactor;

import ic2.core.ref.Ic2Components;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Item with a wear counter (custom damage) shown as a durability bar; stack size 1 unless the subclass changes it. */
public class ItemGradualInt extends Item {
    protected final int maxDmg;
    public ItemGradualInt(Properties properties, int maxDmg) { this(properties, maxDmg, 1); }
    protected ItemGradualInt(Properties properties, int maxDmg, int stackSize) { super(properties.stacksTo(stackSize).setNoRepair()); this.maxDmg = maxDmg; }

    public int getCustomDamage(ItemStack stack) { return stack.getOrDefault(Ic2Components.CUSTOM_DAMAGE.get(), 0); }
    public int getMaxCustomDamage(ItemStack stack) { return maxDmg; }
    public void setCustomDamage(ItemStack stack, int damage) { if (damage <= 0) stack.remove(Ic2Components.CUSTOM_DAMAGE.get()); else stack.set(Ic2Components.CUSTOM_DAMAGE.get(), damage); }
    public void applyCustomDamage(ItemStack stack, int damage) { setCustomDamage(stack, getCustomDamage(stack) + damage); }

    @Override public boolean isBarVisible(ItemStack stack) { return getCustomDamage(stack) > 0; }
    @Override public int getBarWidth(ItemStack stack) { return Math.round(13f - 13f * getCustomDamage(stack) / maxDmg); }
    @Override public int getBarColor(ItemStack stack) { return Mth.hsvToRgb(Math.max(0, 1f - (float)getCustomDamage(stack) / maxDmg) / 3f, 1, 1); }
}
