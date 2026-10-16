package ic2.core.util;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Scanner values of ores (IC2.registerOre of the original): coal 1, copper/tin/lead/quartz 2, iron/gold/redstone/lapis/silver 3, uranium/gems 4, diamond/emerald/tungsten 5, other ores 1. */
public final class OreValues {
    private static final Map<TagKey<Block>, Integer> VALUES = new LinkedHashMap<>();
    private static final TagKey<Block> ORES = tag("c:ores");
    static {
        put(5, "diamond", "emerald", "tungsten");
        put(4, "uranium", "ruby", "sapphire", "green_sapphire");
        put(3, "iron", "gold", "redstone", "lapis", "silver");
        put(2, "copper", "tin", "lead", "quartz");
        put(1, "coal");
    }
    private OreValues() {}
    private static TagKey<Block> tag(String id) { return TagKey.create(Registries.BLOCK, ResourceLocation.parse(id)); }
    private static void put(int value, String... names) { for (String n : names) VALUES.put(tag("c:ores/" + n), value); }

    public static int valueOf(BlockState state) {
        if (!state.is(ORES)) return 0;
        for (var e : VALUES.entrySet()) if (state.is(e.getKey())) return e.getValue();
        return 1;
    }
    public static boolean isValuable(BlockState state) { return valueOf(state) > 0; }
}
