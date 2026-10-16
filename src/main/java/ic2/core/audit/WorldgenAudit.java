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
        IC2.LOG.info("[audit] ores in {} chunks: tin={} uranium={} lead={} (per chunk {} / {} / {})", chunks,
            count(level, spawn, 4, Ic2Blocks.TIN_ORE.get(), 0, 70), count(level, spawn, 4, Ic2Blocks.URANIUM_ORE.get(), 0, 70), count(level, spawn, 4, Ic2Blocks.LEAD_ORE.get(), 0, 70),
            count(level, spawn, 4, Ic2Blocks.TIN_ORE.get(), 0, 70) / (float)chunks, count(level, spawn, 4, Ic2Blocks.URANIUM_ORE.get(), 0, 70) / (float)chunks, count(level, spawn, 4, Ic2Blocks.LEAD_ORE.get(), 0, 70) / (float)chunks);
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
