package ic2.api.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Lets other mods mark foreign items as toolbox-storable or metal armor. */
public final class ItemWrapper {
    private static final Map<Item, List<IBoxable>> boxable = new HashMap<>();
    private static final Map<Item, List<IMetalArmor>> metalArmor = new HashMap<>();
    private ItemWrapper() {}

    public static void registerBoxable(Item item, IBoxable b) { boxable.computeIfAbsent(item, k -> new ArrayList<>()).add(b); }
    public static boolean canBeStoredInToolbox(ItemStack stack) {
        for (IBoxable b : boxable.getOrDefault(stack.getItem(), List.of())) if (b.canBeStoredInToolbox(stack)) return true;
        return stack.getItem() instanceof IBoxable b && b.canBeStoredInToolbox(stack);
    }

    public static void registerMetalArmor(Item item, IMetalArmor a) { metalArmor.computeIfAbsent(item, k -> new ArrayList<>()).add(a); }
    public static boolean isMetalArmor(ItemStack stack, Player player) {
        for (IMetalArmor a : metalArmor.getOrDefault(stack.getItem(), List.of())) if (a.isMetalArmor(stack, player)) return true;
        return stack.getItem() instanceof IMetalArmor a && a.isMetalArmor(stack, player);
    }
}
