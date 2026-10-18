package ic2.core.audit;

import ic2.api.recipe.Recipes;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Modern ore drops: tin/lead ores drop raw metal with fortune, silk touch keeps the ore, uranium stays a block; raw ores process like ores. */
public final class RawOreAudit {
    private RawOreAudit() {}

    private static int drops(ServerLevel l, BlockPos p, BlockState state, ItemStack tool, Item item) {
        int n = 0;
        for (ItemStack s : Block.getDrops(state, l, p, null, null, tool)) if (s.is(item)) n += s.getCount();
        return n;
    }

    public static void run(ServerLevel l, BlockPos p, CropAudit.Check check) {
        var enchants = l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemStack pick = new ItemStack(Items.IRON_PICKAXE), silk = new ItemStack(Items.IRON_PICKAXE), fortune = new ItemStack(Items.IRON_PICKAXE);
        silk.enchant(enchants.getOrThrow(Enchantments.SILK_TOUCH), 1);
        fortune.enchant(enchants.getOrThrow(Enchantments.FORTUNE), 3);
        Object[][] ores = {{Ic2Blocks.TIN_ORE.get(), Ic2Items.RAW_TIN.get()}, {Ic2Blocks.DEEPSLATE_TIN_ORE.get(), Ic2Items.RAW_TIN.get()},
            {Ic2Blocks.LEAD_ORE.get(), Ic2Items.RAW_LEAD.get()}, {Ic2Blocks.DEEPSLATE_LEAD_ORE.get(), Ic2Items.RAW_LEAD.get()}};
        for (Object[] o : ores) {
            Block ore = (Block)o[0];
            Item raw = (Item)o[1];
            BlockState state = ore.defaultBlockState();
            String name = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(ore).getPath();
            check.test(name + " drops one raw metal, not the ore", drops(l, p, state, pick, raw) == 1 && drops(l, p, state, pick, ore.asItem()) == 0, drops(l, p, state, pick, raw));
            check.test(name + " keeps the ore with silk touch", drops(l, p, state, silk, ore.asItem()) == 1 && drops(l, p, state, silk, raw) == 0, "silk");
            int total = 0, max = 0;
            for (int i = 0; i < 200; i++) { int n = drops(l, p, state, fortune, raw); total += n; max = Math.max(max, n); }
            check.test(name + " fortune III gives more raw metal (vanilla ore_drops)", total > 300 && max == 4, "avg " + total / 200.0 + " max " + max);
        }
        for (Block u : new Block[]{Ic2Blocks.URANIUM_ORE.get(), Ic2Blocks.DEEPSLATE_URANIUM_ORE.get()})
            check.test(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(u).getPath() + " still drops itself (no fortune)", drops(l, p, u.defaultBlockState(), fortune, u.asItem()) == 1, "uranium");

        Item[][] macerate = {{Ic2Items.RAW_TIN.get(), Ic2Items.get("crushed_tin_ore")}, {Ic2Items.RAW_LEAD.get(), Ic2Items.get("crushed_lead_ore")},
            {Items.RAW_IRON, Ic2Items.get("crushed_iron_ore")}, {Items.RAW_GOLD, Ic2Items.get("crushed_gold_ore")}, {Items.RAW_COPPER, Ic2Items.get("crushed_copper_ore")}};
        for (Item[] m : macerate) {
            var out = Recipes.macerator.getOutputFor(new ItemStack(m[0]), false);
            check.test("macerator turns " + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(m[0]) + " into 2 crushed ore like the ore", out != null && out.items.size() == 1 && out.items.get(0).is(m[1]) && out.items.get(0).getCount() == 2, out == null ? "none" : out.items);
        }
        for (Item[] s : new Item[][]{{Ic2Items.RAW_TIN.get(), Ic2Items.get("tin_ingot")}, {Ic2Items.RAW_LEAD.get(), Ic2Items.get("lead_ingot")}}) {
            var input = new SingleRecipeInput(new ItemStack(s[0]));
            boolean furnace = l.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, l).map(r -> r.value().getResultItem(l.registryAccess()).is(s[1])).orElse(false);
            boolean blast = l.getRecipeManager().getRecipeFor(RecipeType.BLASTING, input, l).map(r -> r.value().getResultItem(l.registryAccess()).is(s[1])).orElse(false);
            check.test(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s[0]) + " smelts and blasts to the ingot", furnace && blast, furnace + "/" + blast);
        }
        boolean blocks = true;
        for (Item b : new Item[]{Ic2Blocks.RAW_TIN_BLOCK.get().asItem(), Ic2Blocks.RAW_LEAD_BLOCK.get().asItem(), Ic2Items.RAW_TIN.get(), Ic2Items.RAW_LEAD.get()})
            blocks &= l.getRecipeManager().getRecipes().stream().anyMatch(r -> r.value().getResultItem(l.registryAccess()).is(b));
        check.test("raw metal blocks craft both ways and raw metals carry c:raw_materials", blocks && new ItemStack(Ic2Items.RAW_TIN.get()).is(net.minecraft.tags.ItemTags.create(net.minecraft.resources.ResourceLocation.parse("c:raw_materials/tin")))
            && new ItemStack(Ic2Items.RAW_LEAD.get()).is(net.minecraft.tags.ItemTags.create(net.minecraft.resources.ResourceLocation.parse("c:raw_materials/lead"))), "blocks");
    }
}
