package ic2.core.audit;

import ic2.core.IC2;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/** Real recipe ingredients and generated vanilla chest contents from the loaded mod JAR. */
public final class IssueResourceAudit {
    public static void run(ServerLevel level, BlockPos pos, CropAudit.Check check) {
        int recipes = 0;
        var empty = new java.util.ArrayList<String>();
        for (var holder : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            if (!holder.id().getNamespace().equals("ic2")) continue;
            recipes++;
            for (var ingredient : holder.value().getIngredients())
                if (!ingredient.isEmpty() && ingredient.getItems().length == 0) empty.add(holder.id().toString());
        }
        check.test("every bundled IC2 crafting ingredient resolves without other mods/resourcepacks (" + recipes + " recipes)", empty.isEmpty(), empty);
        for (String name : new String[]{"uranium_block_1", "uranium_fuel_2", "uranium_fuel_3", "mox_fuel_2", "mox_fuel_3"}) {
            var recipe = level.getRecipeManager().byKey(IC2.id(name));
            check.test(name + " shared nuclear tag branches resolve", recipe.isPresent() && recipe.get().value().getIngredients().stream().allMatch(i -> i.getItems().length > 0), name);
            if (recipe.isPresent() && recipe.get().value() instanceof net.minecraft.world.item.crafting.CraftingRecipe crafting) {
                var stacks = new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
                for (var ingredient : crafting.getIngredients()) stacks.add(ingredient.getItems().length == 0 ? net.minecraft.world.item.ItemStack.EMPTY : ingredient.getItems()[0].copy());
                var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, stacks);
                check.test(name + " crafts through its tag alternative", crafting.matches(input, level) && !crafting.assemble(input, level.registryAccess()).isEmpty(), stacks);
            }
        }
        for (String name : new String[]{"abandoned_mineshaft", "desert_pyramid", "jungle_temple", "stronghold_corridor", "stronghold_crossing", "village/village_weaponsmith", "spawn_bonus_chest", "simple_dungeon"}) {
            var table = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse("minecraft:chests/" + name)));
            var params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).create(LootContextParamSets.CHEST);
            var found = new HashSet<String>();
            boolean vanilla = false, ranges = true;
            for (int seed = 0; seed < 512; seed++) {
                for (var stack : table.getRandomItems(params, seed)) {
                    String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                    if (id.startsWith("ic2:")) found.add(id);
                    else vanilla = true;
                    ranges &= stack.getCount() > 0 && stack.getCount() <= stack.getMaxStackSize();
                }
            }
            check.test("native chest loot " + name + " includes original IC2 entries and retains vanilla loot", !found.isEmpty() && vanilla && ranges, found);
        }
    }
    private IssueResourceAudit() {}
}
