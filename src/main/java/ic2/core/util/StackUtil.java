package ic2.core.util;

import net.minecraft.world.item.ItemStack;

public final class StackUtil {
    private StackUtil() {}
    public static boolean isStackEqual(ItemStack a, ItemStack b) { return !a.isEmpty() && !b.isEmpty() && a.is(b.getItem()); }
    public static boolean isStackEqualStrict(ItemStack a, ItemStack b) { return ItemStack.isSameItemSameComponents(a, b); }
    public static ItemStack copyWithSize(ItemStack stack, int size) { return stack.copyWithCount(size); }
}
