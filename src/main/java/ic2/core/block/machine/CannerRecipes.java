package ic2.core.block.machine;

import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.RecipeInputItemStack;
import ic2.api.recipe.RecipeInputOreDict;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.core.fluid.Ic2Fluids;
import ic2.core.ref.Ic2Items;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

/** Canning machine recipe managers (CannerBottleRecipeManager / CannerEnrichRecipeManager) and the original recipe set. */
public final class CannerRecipes {
    private CannerRecipes() {}

    public static void init() {
        Recipes.cannerBottle = new Bottle();
        Recipes.cannerEnrich = new Enrich();
        ItemStack can = new ItemStack(Ic2Items.get("tin_can")), filled = new ItemStack(Ic2Items.FILLED_TIN_CAN.get());
        bottle(new ItemStack(Ic2Items.get("fuel_rod")), 1, new ItemStack(Ic2Items.get("uranium_fuel")), 1, Ic2Items.has("uranium_fuel_rod") ? new ItemStack(Ic2Items.get("uranium_fuel_rod")) : ItemStack.EMPTY);
        bottle(new ItemStack(Ic2Items.get("fuel_rod")), 1, new ItemStack(Ic2Items.get("mox_fuel")), 1, Ic2Items.has("mox_fuel_rod") ? new ItemStack(Ic2Items.get("mox_fuel_rod")) : ItemStack.EMPTY);
        Object[][] food = {{Items.POTATO, 1}, {Items.COOKIE, 2}, {Items.MELON_SLICE, 2}, {Items.COD, 2}, {Items.SALMON, 2}, {Items.CHICKEN, 2}, {Items.PORKCHOP, 3}, {Items.BEEF, 3}, {Items.MUTTON, 3}, {Items.RABBIT, 3},
            {Items.APPLE, 4}, {Items.CARROT, 4}, {Items.BREAD, 5}, {Items.COOKED_COD, 5}, {Items.COOKED_SALMON, 5}, {Items.COOKED_CHICKEN, 6}, {Items.BAKED_POTATO, 6}, {Items.MUSHROOM_STEW, 6},
            {Items.PUMPKIN_PIE, 6}, {Items.COOKED_MUTTON, 6}, {Items.COOKED_RABBIT, 6}, {Items.COOKED_PORKCHOP, 8}, {Items.COOKED_BEEF, 8}, {Items.CAKE, 12}};
        for (Object[] f : food) bottle(can, (Integer)f[1], new ItemStack((Item)f[0]), 1, filled.copyWithCount((Integer)f[1]));
        bottle(can, 1, new ItemStack(Items.POISONOUS_POTATO), 2, filled);
        bottle(can, 1, new ItemStack(Items.ROTTEN_FLESH), 2, filled);
        enrich(new FluidStack(Fluids.WATER, 1000), new RecipeInputItemStack(new ItemStack(Ic2Items.get("cf_powder")), 1), new FluidStack(Ic2Fluids.CONSTRUCTION_FOAM.get(), 1000));
        enrich(new FluidStack(Fluids.WATER, 1000), new RecipeInputOreDict(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/lapis")), 8), new FluidStack(Ic2Fluids.COOLANT.get(), 1000));
        enrich(new FluidStack(Ic2Fluids.DISTILLED_WATER.get(), 1000), new RecipeInputOreDict(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/lapis")), 1), new FluidStack(Ic2Fluids.COOLANT.get(), 1000));
        enrich(new FluidStack(Fluids.WATER, 1000), new RecipeInputItemStack(new ItemStack(Ic2Items.get("biochaff")), 1), new FluidStack(Ic2Fluids.BIOMASS.get(), 1000));
        enrich(new FluidStack(Fluids.WATER, 6000), new RecipeInputItemStack(new ItemStack(Items.STICK), 1), new FluidStack(Ic2Fluids.HOT_WATER.get(), 1000));
    }

    private static void bottle(ItemStack container, int conAmount, ItemStack fill, int fillAmount, ItemStack output) {
        if (!output.isEmpty()) Recipes.cannerBottle.addRecipe(new RecipeInputItemStack(container, conAmount), new RecipeInputItemStack(fill, fillAmount), output);
    }
    private static void enrich(FluidStack in, IRecipeInput additive, FluidStack out) { Recipes.cannerEnrich.addRecipe(in, additive, out); }

    public static RecipeOutput fluidOutput(FluidStack fluid, ItemStack... items) {
        CompoundTag meta = new CompoundTag();
        meta.putString("fluid", BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
        meta.putInt("amount", fluid.getAmount());
        return new RecipeOutput(meta, items);
    }

    public static FluidStack fluidOf(RecipeOutput output) {
        if (output.metadata == null || !output.metadata.contains("fluid")) return FluidStack.EMPTY;
        return new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse(output.metadata.getString("fluid"))), output.metadata.getInt("amount"));
    }

    static final class Bottle implements ICannerBottleRecipeManager {
        private final Map<Input, RecipeOutput> recipes = new LinkedHashMap<>();
        @Override public void addRecipe(IRecipeInput container, IRecipeInput fill, ItemStack output) { recipes.put(new Input(container, fill), new RecipeOutput(null, output)); }
        @Override public Map<Input, RecipeOutput> getRecipes() { return recipes; }
        @Override
        public RecipeOutput getOutputFor(ItemStack container, ItemStack fill, boolean adjustInput, boolean acceptTest) {
            if (acceptTest ? container.isEmpty() && fill.isEmpty() : container.isEmpty() || fill.isEmpty()) return null;
            for (var e : recipes.entrySet()) {
                Input in = e.getKey();
                if (acceptTest && container.isEmpty()) { if (in.fill().matches(fill)) return e.getValue(); }
                else if (acceptTest && fill.isEmpty()) { if (in.container().matches(container)) return e.getValue(); }
                else if (in.container().matches(container) && in.fill().matches(fill)) {
                    if (!acceptTest && (container.getCount() < in.container().getAmount() || fill.getCount() < in.fill().getAmount())) continue;
                    if (adjustInput) { container.shrink(in.container().getAmount()); fill.shrink(in.fill().getAmount()); }
                    return e.getValue();
                }
            }
            return null;
        }
    }

    static final class Enrich implements ICannerEnrichRecipeManager {
        private final Map<Input, FluidStack> recipes = new LinkedHashMap<>();
        @Override public void addRecipe(FluidStack fluid, IRecipeInput additive, FluidStack output) { recipes.put(new Input(fluid, additive), output); }
        @Override public Map<Input, FluidStack> getRecipes() { return recipes; }
        @Override
        public RecipeOutput getOutputFor(FluidStack fluid, ItemStack additive, boolean adjustInput, boolean acceptTest) {
            if (!acceptTest && additive.isEmpty()) return null;
            for (var e : recipes.entrySet()) {
                Input in = e.getKey();
                if (acceptTest && fluid.isEmpty()) { if (in.additive().matches(additive)) return fluidOutput(e.getValue()); }
                else if (acceptTest && additive.isEmpty()) { if (FluidStack.isSameFluidSameComponents(in.fluid(), fluid)) return fluidOutput(e.getValue()); }
                else if (FluidStack.isSameFluidSameComponents(in.fluid(), fluid) && in.additive().matches(additive)) {
                    if (!acceptTest && (fluid.getAmount() < in.fluid().getAmount() || additive.getCount() < in.additive().getAmount())) continue;
                    if (adjustInput) { fluid.shrink(in.fluid().getAmount()); additive.shrink(in.additive().getAmount()); }
                    return fluidOutput(e.getValue());
                }
            }
            return null;
        }
    }
}
