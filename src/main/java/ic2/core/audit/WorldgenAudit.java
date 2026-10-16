package ic2.core.audit;

import ic2.core.IC2;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** -Dic2.audit=worldgen: counts IC2 ores near spawn and rubber trees in the nearest forest and swamp of a normal world. */
@EventBusSubscriber(modid = IC2.MODID)
public final class WorldgenAudit {
    private WorldgenAudit() {}

    private static int count(ServerLevel level, ChunkPos center, int radius, Block block, int minY, int maxY) {
        int n = 0;
        for (int cx = -radius; cx <= radius; cx++) for (int cz = -radius; cz <= radius; cz++) {
            var chunk = level.getChunk(center.x + cx, center.z + cz);
            for (BlockPos p : BlockPos.betweenClosed(chunk.getPos().getMinBlockX(), minY, chunk.getPos().getMinBlockZ(), chunk.getPos().getMaxBlockX(), maxY, chunk.getPos().getMaxBlockZ()))
                if (chunk.getBlockState(p).is(block)) n++;
        }
        return n;
    }

    @SubscribeEvent
    static void started(ServerStartedEvent event) {
        if (!"worldgen".equals(System.getProperty("ic2.audit"))) return;
        ServerLevel level = event.getServer().overworld();
        ChunkPos spawn = new ChunkPos(level.getSharedSpawnPos());
        int chunks = 81;
        // per ore: stone / deepslate variant above and below y = 0, plus the highest stone-ore and the highest deepslate-ore block found
        Block[][] ores = {{Ic2Blocks.TIN_ORE.get(), Ic2Blocks.DEEPSLATE_TIN_ORE.get()}, {Ic2Blocks.URANIUM_ORE.get(), Ic2Blocks.DEEPSLATE_URANIUM_ORE.get()}, {Ic2Blocks.LEAD_ORE.get(), Ic2Blocks.DEEPSLATE_LEAD_ORE.get()}};
        for (Block[] pair : ores) {
            int stoneLow = count(level, spawn, 4, pair[0], -64, -1), stoneHigh = count(level, spawn, 4, pair[0], 0, 319);
            int deepLow = count(level, spawn, 4, pair[1], -64, -1), deepHigh = count(level, spawn, 4, pair[1], 0, 319);
            int[] bands = new int[6];
            for (int b = 0; b < 6; b++) bands[b] = count(level, spawn, 4, pair[0], -64 + b * 32, -64 + b * 32 + 31) + count(level, spawn, 4, pair[1], -64 + b * 32, -64 + b * 32 + 31);
            IC2.LOG.info("[audit] {}: stone y<0={} y>=0={} | deepslate y<0={} y>=0={} | per chunk {} | bands(-64,-32,0,32,64,96..127)={}", pair[0].getDescriptionId(), stoneLow, stoneHigh, deepLow, deepHigh,
                (stoneLow + stoneHigh + deepLow + deepHigh) / (float)chunks, java.util.Arrays.toString(bands));
            boolean ok = deepLow > 0 && deepHigh <= deepLow / 10 + 5 && stoneLow <= (stoneLow + deepLow) / 10 + 5 && stoneHigh + deepHigh > 0 && bands[5] == 0;
            IC2.LOG.info("[audit] {} {} deepslate variant below y=0", ok ? "PASS" : "FAIL", pair[0].getDescriptionId());
        }
        for (var tag : java.util.List.of(BiomeTags.IS_FOREST, net.neoforged.neoforge.common.Tags.Biomes.IS_SWAMP)) {
            var found = level.findClosestBiome3d(b -> b.is(tag), level.getSharedSpawnPos(), 6400, 32, 64);
            if (found == null) { IC2.LOG.info("[audit] no biome {}", tag.location()); continue; }
            ChunkPos at = new ChunkPos(found.getFirst());
            IC2.LOG.info("[audit] {} at {}: rubber wood in 81 chunks = {}", tag.location(), found.getFirst(), count(level, at, 4, Ic2Blocks.RUBBER_WOOD.get(), 40, 200));
        }
        IC2.LOG.info("[audit] DONE worldgen");
        event.getServer().halt(false);
    }
}
