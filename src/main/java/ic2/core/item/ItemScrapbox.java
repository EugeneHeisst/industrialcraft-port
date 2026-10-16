package ic2.core.item;

import ic2.api.recipe.IScrapboxManager;
import ic2.api.recipe.Recipes;
import ic2.core.ref.Ic2Items;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/** Right click opens the box into one weighted random drop. */
public class ItemScrapbox extends Item {
    public ItemScrapbox(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            ItemStack drop = Recipes.scrapboxDrops.getDrop(stack, !player.getAbilities().instabuild);
            if (drop != null && !drop.isEmpty()) player.drop(drop, false);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static void init() {
        var m = new Manager();
        Recipes.scrapboxDrops = m;
        add(m, Items.WOODEN_HOE, 5.01f); add(m, Blocks.DIRT, 5); add(m, Items.STICK, 4); add(m, Blocks.GRASS_BLOCK, 3); add(m, Blocks.GRAVEL, 3); add(m, Blocks.NETHERRACK, 2);
        add(m, Items.ROTTEN_FLESH, 2); add(m, Items.APPLE, 1.5f); add(m, Items.BREAD, 1.5f); add(m, Ic2Items.get("filled_tin_can"), 1.5f); add(m, Items.WOODEN_SWORD, 1);
        add(m, Items.WOODEN_SHOVEL, 1); add(m, Items.WOODEN_PICKAXE, 1); add(m, Blocks.SOUL_SAND, 1); add(m, Items.OAK_SIGN, 1); add(m, Items.LEATHER, 1); add(m, Items.FEATHER, 1);
        add(m, Items.BONE, 1); add(m, Items.COOKED_PORKCHOP, 0.9f); add(m, Items.COOKED_BEEF, 0.9f); add(m, Blocks.PUMPKIN, 0.9f); add(m, Items.COOKED_CHICKEN, 0.9f);
        add(m, Items.MINECART, 0.01f); add(m, Items.REDSTONE, 0.9f); add(m, Ic2Items.get("rubber"), 0.8f); add(m, Items.GLOWSTONE_DUST, 0.8f); add(m, Ic2Items.get("coal_dust"), 0.8f);
        add(m, Ic2Items.get("copper_dust"), 0.8f); add(m, Ic2Items.get("tin_dust"), 0.8f); add(m, Ic2Items.SU_BATTERY.get(), 0.7f); add(m, Ic2Items.get("iron_dust"), 0.7f);
        add(m, Ic2Items.get("gold_dust"), 0.7f); add(m, Items.SLIME_BALL, 0.6f); add(m, Blocks.IRON_ORE, 0.5f); add(m, Items.GOLDEN_HELMET, 0.01f); add(m, Blocks.GOLD_ORE, 0.5f);
        add(m, Items.CAKE, 0.5f); add(m, Items.DIAMOND, 0.1f); add(m, Items.EMERALD, 0.05f); add(m, Items.ENDER_PEARL, 0.08f); add(m, Items.BLAZE_ROD, 0.04f); add(m, Items.EGG, 0.8f);
        add(m, Blocks.COPPER_ORE, 0.7f); add(m, Ic2Items.get("tin_ore"), 0.7f);
    }

    private static void add(Manager m, ItemLike item, float chance) { if (item.asItem() != Items.AIR) m.addDrop(new ItemStack(item), chance); }

    public static class Manager implements IScrapboxManager {
        private final List<ItemStack> items = new ArrayList<>();
        private final List<Float> chances = new ArrayList<>(), bounds = new ArrayList<>();
        private float topChance;
        private final RandomSource random = RandomSource.create();

        @Override public void addDrop(ItemStack drop, float rawChance) { items.add(drop); chances.add(rawChance); topChance += rawChance; bounds.add(topChance); }

        @Override
        public ItemStack getDrop(ItemStack input, boolean adjustInput) {
            if (items.isEmpty()) return null;
            if (adjustInput) input.shrink(1);
            float chance = random.nextFloat() * topChance;
            int low = 0, high = items.size() - 1;
            while (low < high) { int mid = (high + low) / 2; if (chance < bounds.get(mid)) high = mid; else low = mid + 1; }
            return items.get(low).copy();
        }

        @Override public Map<ItemStack, Float> getDrops() { var ret = new LinkedHashMap<ItemStack, Float>(); for (int i = 0; i < items.size(); i++) ret.put(items.get(i), chances.get(i) / topChance); return ret; }
    }
}
