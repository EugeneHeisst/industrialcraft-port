package ic2.core.jei;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/** JEI pages for the IC2 machine recipe managers (the original's NEI integration): machines, solid canner, canner enrichment, scrap box. */
@JeiPlugin
public class IC2JeiPlugin implements IModPlugin {
    /** One page per machine recipe manager; extra is the metadata key shown under the recipe (heat, water, hardness...). */
    record Machine(String id, Supplier<IMachineRecipeManager> manager, Supplier<? extends ItemLike> block, Component title, String extra, String extraLabel, RecipeType<MachineCategory.Recipe> type) {
        Machine(String id, Supplier<IMachineRecipeManager> manager, Supplier<? extends ItemLike> block, Component title, String extra, String extraLabel) {
            this(id, manager, block, title, extra, extraLabel, RecipeType.create(IC2.MODID, id, MachineCategory.Recipe.class));
        }
    }

    static List<Machine> machines() {
        return List.of(
            new Machine("macerator", () -> Recipes.macerator, Ic2Blocks.MACERATOR, Component.translatable("block.ic2.macerator"), null, null),
            new Machine("extractor", () -> Recipes.extractor, Ic2Blocks.EXTRACTOR, Component.translatable("block.ic2.extractor"), null, null),
            new Machine("compressor", () -> Recipes.compressor, Ic2Blocks.COMPRESSOR, Component.translatable("block.ic2.compressor"), null, null),
            new Machine("thermal_centrifuge", () -> Recipes.centrifuge, Ic2Blocks.THERMAL_CENTRIFUGE, Component.translatable("block.ic2.thermal_centrifuge"), "heat", "ic2.jei.heat"),
            new Machine("ore_washing", () -> Recipes.oreWashing, Ic2Blocks.ORE_WASHING_PLANT, Component.translatable("block.ic2.ore_washing_plant"), "fluid", "ic2.jei.water"),
            new Machine("block_cutter", () -> Recipes.blockcutter, Ic2Blocks.BLOCK_CUTTER, Component.translatable("block.ic2.block_cutter"), "hardness", "ic2.jei.hardness"),
            new Machine("blast_furnace", () -> Recipes.blastfurance, Ic2Blocks.BLAST_FURNACE, Component.translatable("block.ic2.blast_furnace"), null, null),
            new Machine("metal_former_extruding", () -> Recipes.metalformerExtruding, Ic2Blocks.METAL_FORMER, Component.translatable("block.ic2.metal_former").append(" - ").append(Component.translatable("ic2.MetalFormer.gui.switch.Extruding")), null, null),
            new Machine("metal_former_cutting", () -> Recipes.metalformerCutting, Ic2Blocks.METAL_FORMER, Component.translatable("block.ic2.metal_former").append(" - ").append(Component.translatable("ic2.MetalFormer.gui.switch.Cutting")), null, null),
            new Machine("metal_former_rolling", () -> Recipes.metalformerRolling, Ic2Blocks.METAL_FORMER, Component.translatable("block.ic2.metal_former").append(" - ").append(Component.translatable("ic2.MetalFormer.gui.switch.Rolling")), null, null),
            new Machine("uu_amplifier", () -> Recipes.matterAmplifier, Ic2Blocks.MASS_FABRICATOR, Component.translatable("block.ic2.mass_fabricator"), "amplification", "ic2.jei.amplification"));
    }

    private static final List<Machine> MACHINES = machines();

    @Override public ResourceLocation getPluginUid() { return IC2.id("jei"); }

    private static mezz.jei.api.runtime.IJeiRuntime runtime;
    @Override public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime rt) { runtime = rt; }

    /** Client audit: opens the JEI page of an IC2 category, or the recipes producing an item when id names an item. */
    public static boolean showForAudit(String id) {
        if (runtime == null) return false;
        for (Machine m : MACHINES) if (m.id().equals(id)) { runtime.getRecipesGui().showTypes(List.of(m.type())); return true; }
        for (var t : List.of(CannerCategory.TYPE, EnrichCategory.TYPE, ScrapboxCategory.TYPE)) if (t.getUid().getPath().equals(id)) { runtime.getRecipesGui().showTypes(List.of(t)); return true; }
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(IC2.id(id));
        runtime.getRecipesGui().show(runtime.getJeiHelpers().getFocusFactory().createFocus(mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT, mezz.jei.api.constants.VanillaTypes.ITEM_STACK, new ItemStack(item)));
        return true;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        var gui = reg.getJeiHelpers().getGuiHelper();
        for (Machine m : MACHINES) reg.addRecipeCategories(new MachineCategory(gui, m));
        reg.addRecipeCategories(new CannerCategory(gui), new EnrichCategory(gui), new ScrapboxCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        for (Machine m : MACHINES) {
            IMachineRecipeManager manager = m.manager().get();
            if (manager == null) continue;
            List<MachineCategory.Recipe> list = new ArrayList<>();
            for (var e : manager.getRecipes().entrySet()) {
                List<ItemStack> inputs = new ArrayList<>();
                for (ItemStack s : e.getKey().getInputs()) inputs.add(s.copyWithCount(e.getKey().getAmount()));
                if (inputs.isEmpty()) continue;
                var meta = e.getValue().metadata;
                list.add(new MachineCategory.Recipe(inputs, e.getValue().items, m.extra() != null && meta != null ? meta.getInt(m.extra()) : 0));
            }
            reg.addRecipes(m.type(), list);
        }
        if (Recipes.cannerBottle != null) {
            List<CannerCategory.Recipe> list = new ArrayList<>();
            for (var e : Recipes.cannerBottle.getRecipes().entrySet())
                list.add(new CannerCategory.Recipe(withAmount(e.getKey().container()), withAmount(e.getKey().fill()), e.getValue().items.get(0)));
            reg.addRecipes(CannerCategory.TYPE, list);
        }
        if (Recipes.cannerEnrich != null) {
            List<EnrichCategory.Recipe> list = new ArrayList<>();
            for (var e : Recipes.cannerEnrich.getRecipes().entrySet()) list.add(new EnrichCategory.Recipe(e.getKey().fluid(), withAmount(e.getKey().additive()), e.getValue()));
            reg.addRecipes(EnrichCategory.TYPE, list);
        }
        if (Recipes.scrapboxDrops != null) {
            float total = 0;
            for (float f : Recipes.scrapboxDrops.getDrops().values()) total += f;
            List<ScrapboxCategory.Recipe> list = new ArrayList<>();
            for (Map.Entry<ItemStack, Float> e : Recipes.scrapboxDrops.getDrops().entrySet()) list.add(new ScrapboxCategory.Recipe(e.getKey(), e.getValue() / total));
            list.sort((a, b) -> Float.compare(b.chance(), a.chance()));
            reg.addRecipes(ScrapboxCategory.TYPE, list);
        }
    }

    private static List<ItemStack> withAmount(ic2.api.recipe.IRecipeInput in) {
        List<ItemStack> ret = new ArrayList<>();
        for (ItemStack s : in.getInputs()) ret.add(s.copyWithCount(in.getAmount()));
        return ret;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        for (Machine m : MACHINES) reg.addRecipeCatalyst(m.block().get(), m.type());
        reg.addRecipeCatalyst(Ic2Blocks.INDUCTION_FURNACE.get(), mezz.jei.api.constants.RecipeTypes.SMELTING);
        reg.addRecipeCatalyst(Ic2Blocks.ELECTRIC_FURNACE.get(), mezz.jei.api.constants.RecipeTypes.SMELTING);
        reg.addRecipeCatalyst(Ic2Blocks.IRON_FURNACE.get(), mezz.jei.api.constants.RecipeTypes.SMELTING, mezz.jei.api.constants.RecipeTypes.FUELING);
        reg.addRecipeCatalyst(Ic2Blocks.SOLID_CANNER.get(), CannerCategory.TYPE);
        reg.addRecipeCatalyst(Ic2Blocks.CANNER.get(), EnrichCategory.TYPE, CannerCategory.TYPE);
        reg.addRecipeCatalyst(Ic2Items.SCRAP_BOX.get(), ScrapboxCategory.TYPE);
    }
}
