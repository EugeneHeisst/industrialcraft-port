package ic2.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;

/** Original IC2 chest entries join the existing weighted pool, retaining its rolls and vanilla loot. */
@EventBusSubscriber(modid = IC2.MODID)
public final class IC2Loot {
    private static final JsonObject ENTRIES = load();
    private static JsonObject load() {
        try (var stream = IC2Loot.class.getResourceAsStream("/ic2/loot_injections.json")) {
            if (stream == null) throw new IllegalStateException("Missing bundled IC2 chest loot");
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) { throw new IllegalStateException("Reading IC2 chest loot", e); }
    }
    @SubscribeEvent
    static void loadTable(LootTableLoadEvent event) {
        var additions = ENTRIES.getAsJsonArray(event.getName().toString());
        if (additions == null) return;
        var ops = event.getRegistries().createSerializationContext(JsonOps.INSTANCE);
        var pools = ((ic2.mixin.LootTableAccessor)event.getTable()).ic2$getPools();
        if (pools.isEmpty()) return;
        var pool = (ic2.mixin.LootPoolAccessor)pools.getFirst();
        var entries = new java.util.ArrayList<>(pool.ic2$getEntries());
        for (var entry : additions)
            entries.add(net.minecraft.world.level.storage.loot.entries.LootPoolEntries.CODEC.parse(ops, entry).getOrThrow());
        pool.ic2$setEntries(java.util.List.copyOf(entries));
    }
    private IC2Loot() {}
}
