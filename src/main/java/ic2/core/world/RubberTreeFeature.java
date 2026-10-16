package ic2.core.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.common.Tags;

/** Per chunk: swamps 5-14 and forests/jungles 1-5 tree "points"; a cluster spawns with points*2 percent chance (IC2.generate). */
public class RubberTreeFeature extends Feature<NoneFeatureConfiguration> {
    public RubberTreeFeature() { super(NoneFeatureConfiguration.CODEC); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        var random = ctx.random();
        BlockPos origin = ctx.origin();
        Holder<Biome> biome = ctx.level().getBiome(origin.offset(16, 0, 16));
        int rubbertrees = 0;
        if (biome.is(Tags.Biomes.IS_SWAMP)) rubbertrees += random.nextInt(10) + 5;
        if (biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_JUNGLE)) rubbertrees += random.nextInt(5) + 1;
        if (random.nextInt(100) + 1 > rubbertrees * 2) return false;
        WorldGenRubTree.generate(ctx.level(), random, origin.getX() + random.nextInt(16), rubbertrees, origin.getZ() + random.nextInt(16),
            origin.getX() - 12, origin.getX() + 27, origin.getZ() - 12, origin.getZ() + 27);
        return true;
    }
}
