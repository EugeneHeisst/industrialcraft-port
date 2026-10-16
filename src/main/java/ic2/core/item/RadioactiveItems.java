package ic2.core.item;

import ic2.core.IC2;
import ic2.core.item.armor.ItemArmorUtility;
import ic2.core.ref.Ic2Effects;
import ic2.core.ref.Ic2Items;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** ItemRadioactive: every stack of these in a player's inventory keeps radiation going unless the player wears full hazmat. */
@EventBusSubscriber(modid = IC2.MODID)
public final class RadioactiveItems {
    private record Rad(int length, int amplifier) {}
    private static Map<Item, Rad> items;
    private RadioactiveItems() {}

    public static void register(Item item, int length, int amplifier) { map().put(item, new Rad(length, amplifier)); }

    private static Map<Item, Rad> map() {
        if (items == null) {
            items = new HashMap<>();
            String[][] defs = {{"uranium_fuel", "60", "100"}, {"mox_fuel", "300", "100"}, {"plutonium", "150", "100"}, {"small_plutonium", "150", "100"}, {"uranium_235", "150", "100"},
                {"small_uranium_235", "150", "100"}, {"uranium_238", "10", "90"}, {"rtg_pellet", "2", "90"}};
            for (String[] d : defs) if (Ic2Items.has(d[0])) items.put(Ic2Items.get(d[0]), new Rad(Integer.parseInt(d[1]), Integer.parseInt(d[2])));
        }
        return items;
    }

    public static boolean isRadioactive(ItemStack stack) { return map().containsKey(stack.getItem()); }

    @SubscribeEvent
    static void tick(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (p.level().isClientSide || p.isCreative() || p.isSpectator() || ItemArmorUtility.Hazmat.hasCompleteHazmat(p)) return;
        var inv = p.getInventory();
        for (var list : java.util.List.of(inv.items, inv.armor, inv.offhand))
            for (ItemStack s : list) {
                Rad r = s.isEmpty() ? null : map().get(s.getItem());
                if (r == null) continue;
                var cur = p.getEffect(Ic2Effects.RADIATION);
                p.addEffect(new MobEffectInstance(Ic2Effects.RADIATION, cur == null ? r.length * 20 : cur.getDuration() + r.length, r.amplifier));
            }
    }
}
