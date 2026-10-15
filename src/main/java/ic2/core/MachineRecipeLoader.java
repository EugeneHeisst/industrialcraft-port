package ic2.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.RecipeInputItemStack;
import ic2.api.recipe.RecipeInputOreDict;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Loads the machine recipes converted from the original assets/ic2/config/*.ini (bundled as ic2/recipes/*.json). */
public final class MachineRecipeLoader {
    private MachineRecipeLoader() {}

    public static void load(String name, IMachineRecipeManager manager) {
        var in = MachineRecipeLoader.class.getResourceAsStream("/ic2/recipes/" + name + ".json");
        if (in == null) { IC2.LOG.warn("Missing machine recipe file {}", name); return; }
        JsonArray recipes = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonArray();
        for (JsonElement el : recipes) {
            JsonObject r = el.getAsJsonObject();
            IRecipeInput input = input(r.getAsJsonObject("input"));
            if (input == null) continue;
            List<ItemStack> outputs = new ArrayList<>();
            boolean ok = true;
            for (JsonElement o : r.getAsJsonArray("output")) {
                ItemStack s = stack(o.getAsJsonObject());
                if (s.isEmpty()) { ok = false; break; }
                outputs.add(s);
            }
            if (!ok) continue;
            CompoundTag meta = null;
            if (r.has("meta")) {
                meta = new CompoundTag();
                for (var m : r.getAsJsonObject("meta").entrySet()) meta.putInt(m.getKey(), m.getValue().getAsInt());
            }
            manager.addRecipe(input, meta, outputs.toArray(new ItemStack[0]));
        }
    }

    public static void loadList(String name, ic2.api.recipe.IListRecipeManager manager) {
        var in = MachineRecipeLoader.class.getResourceAsStream("/ic2/recipes/" + name + ".json");
        if (in == null) return;
        for (JsonElement el : JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonArray()) {
            IRecipeInput input = input(el.getAsJsonObject());
            if (input != null) manager.add(input);
        }
    }

    static IRecipeInput input(JsonObject o) {
        int count = o.has("count") ? o.get("count").getAsInt() : 1;
        if (o.has("tag")) return new RecipeInputOreDict(o.get("tag").getAsString(), count);
        var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(o.get("item").getAsString()));
        return item == Items.AIR ? null : new RecipeInputItemStack(new ItemStack(item), count);
    }

    static ItemStack stack(JsonObject o) {
        var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(o.get("item").getAsString()));
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, o.has("count") ? o.get("count").getAsInt() : 1);
    }
}
