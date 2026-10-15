package ic2.core.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class UpgradeRegistry {
    private static final List<ItemStack> upgrades = new ArrayList<>();
    private UpgradeRegistry() {}
    public static ItemStack register(ItemStack stack) {
        if (!(stack.getItem() instanceof IUpgradeItem)) throw new IllegalArgumentException("The stack must represent an IUpgradeItem.");
        upgrades.add(stack);
        return stack;
    }
    public static List<ItemStack> getUpgrades() { return Collections.unmodifiableList(upgrades); }
}
