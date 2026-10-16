package ic2.core.uu;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import ic2.core.ref.Ic2Items;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * UU-matter values per item (1 = cobblestone, 1e5 = 1 B of UU matter): start from the world scan values and propagate through
 * crafting (+1), smelting and IC2 machine recipes (+14), canning, scrap boxes, fuel rod depletion and the recycler, keeping the
 * cheapest route. Components are ignored (all stacks of an item share one value).
 */
public final class UuGraph {
    private record Transform(double cost, List<List<ItemStack>> inputs, List<ItemStack> outputs) {}

    private static Map<Item, Double> values = new IdentityHashMap<>();
    private UuGraph() {}

    public static double get(ItemStack stack) { return stack.isEmpty() ? Double.POSITIVE_INFINITY : values.getOrDefault(stack.getItem(), Double.POSITIVE_INFINITY); }
    public static double getInBuckets(ItemStack stack) { return get(stack) * 1e-5; }
    public static ItemStack find(ItemStack stack) { return values.containsKey(stack.getItem()) ? stack.copyWithCount(1) : ItemStack.EMPTY; }
    public static Map<Item, Double> all() { return values; }
    public static void setAll(Map<Item, Double> v) { values = v; }

    private static List<ItemStack> alts(Ingredient ing) { List<ItemStack> l = new ArrayList<>(); for (ItemStack s : ing.getItems()) l.add(s.copyWithCount(1)); return l; }
    private static List<ItemStack> alts(IRecipeInput in) { List<ItemStack> l = new ArrayList<>(); for (ItemStack s : in.getInputs()) l.add(s.copyWithCount(in.getAmount())); return l; }

    public static void build(MinecraftServer server) {
        long start = System.nanoTime();
        List<Transform> transforms = new ArrayList<>();
        var rm = server.getRecipeManager();
        var access = server.registryAccess();
        for (var holder : rm.getAllRecipesFor(RecipeType.CRAFTING)) {
            CraftingRecipe r = holder.value();
            ItemStack out = r.getResultItem(access);
            if (out.isEmpty()) continue;
            List<List<ItemStack>> in = new ArrayList<>();
            for (Ingredient ing : r.getIngredients()) if (!ing.isEmpty()) in.add(alts(ing));
            if (!in.isEmpty()) transforms.add(new Transform(1, in, List.of(out.copy())));
        }
        for (var type : List.of(RecipeType.SMELTING))
            for (var holder : rm.getAllRecipesFor(type)) {
                AbstractCookingRecipe r = holder.value();
                ItemStack out = r.getResultItem(access);
                if (!out.isEmpty() && !r.getIngredients().isEmpty()) transforms.add(new Transform(14, List.of(alts(r.getIngredients().get(0))), List.of(out.copy())));
            }
        for (var mgr : new ic2.api.recipe.IMachineRecipeManager[]{Recipes.macerator, Recipes.extractor, Recipes.compressor, Recipes.centrifuge, Recipes.blockcutter, Recipes.blastfurance,
                Recipes.metalformerExtruding, Recipes.metalformerCutting, Recipes.metalformerRolling, Recipes.oreWashing}) {
            if (mgr == null) continue;
            for (Map.Entry<IRecipeInput, RecipeOutput> e : mgr.getRecipes().entrySet()) if (!e.getValue().items.isEmpty()) transforms.add(new Transform(14, List.of(alts(e.getKey())), e.getValue().items));
        }
        if (Recipes.cannerBottle != null)
            for (var e : Recipes.cannerBottle.getRecipes().entrySet()) transforms.add(new Transform(0, List.of(alts(e.getKey().container()), alts(e.getKey().fill())), e.getValue().items));
        if (Recipes.scrapboxDrops != null)
            for (var e : Recipes.scrapboxDrops.getDrops().entrySet())
                transforms.add(new Transform(1, List.of(List.of(new ItemStack(Ic2Items.SCRAP_BOX.get(), Math.max(1, Math.round(1 / e.getValue()))))), List.of(e.getKey().copy())));
        for (String rod : new String[]{"uranium_fuel_rod", "dual_uranium_fuel_rod", "quad_uranium_fuel_rod", "mox_fuel_rod", "dual_mox_fuel_rod", "quad_mox_fuel_rod"})
            transforms.add(new Transform(0, List.of(List.of(new ItemStack(Ic2Items.get(rod)))), List.of(new ItemStack(Ic2Items.get("depleted_" + rod)))));

        Map<Item, Double> v = new IdentityHashMap<>();
        loadScanValues(v);
        // recycler: every recyclable item that has a value turns into scrap (late resolver of the original)
        double recyclerCost = 55.0 * TileEntityRecycler.recycleChance() / 4000.0 * 107.0;
        Map<Item, List<Transform>> byInput = new HashMap<>();
        for (Transform t : transforms) for (var list : t.inputs) for (ItemStack s : list) byInput.computeIfAbsent(s.getItem(), k -> new ArrayList<>()).add(t);
        // relax until stable (Bellman-Ford style; values only decrease)
        boolean changed = true;
        for (int pass = 0; pass < 64 && changed; pass++) {
            changed = false;
            for (Transform t : transforms) changed |= apply(t, v);
            double bestScrap = Double.POSITIVE_INFINITY;
            for (var e : v.entrySet()) if (!TileEntityRecycler.getIsItemBlacklisted(new ItemStack(e.getKey()))) bestScrap = Math.min(bestScrap, e.getValue() * TileEntityRecycler.recycleChance());
            if (bestScrap + recyclerCost < v.getOrDefault(Ic2Items.get("scrap"), Double.POSITIVE_INFINITY) - 1e-9) { v.put(Ic2Items.get("scrap"), bestScrap + recyclerCost); changed = true; }
        }
        values = v;
        IC2.LOG.info("UU graph: {} recipe transformations, {} valued items in {} ms", transforms.size(), v.size(), (System.nanoTime() - start) / 1_000_000);
    }

    private static boolean apply(Transform t, Map<Item, Double> v) {
        double sum = t.cost;
        for (var list : t.inputs) {
            double min = Double.POSITIVE_INFINITY;
            for (ItemStack s : list) min = Math.min(min, v.getOrDefault(s.getItem(), Double.POSITIVE_INFINITY) * s.getCount());
            sum += min;
            if (sum == Double.POSITIVE_INFINITY) return false;
        }
        boolean changed = false;
        for (ItemStack out : t.outputs) {
            if (out.isEmpty()) continue;
            double val = sum / out.getCount();
            if (val < v.getOrDefault(out.getItem(), Double.POSITIVE_INFINITY) - 1e-9) { v.put(out.getItem(), val); changed = true; }
        }
        return changed;
    }

    /** ic2/uu_scan_values.json (from uu_scan_values.ini) plus the predefined iridium ore value. */
    private static void loadScanValues(Map<Item, Double> v) {
        var in = UuGraph.class.getResourceAsStream("/ic2/uu_scan_values.json");
        if (in == null) return;
        try (var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
            for (var e : obj.entrySet()) {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(e.getKey()));
                if (item != net.minecraft.world.item.Items.AIR) v.merge(item, e.getValue().getAsDouble(), Math::min);
            }
        } catch (Exception e) { IC2.LOG.warn("Can't read UU scan values", e); }
    }
}
