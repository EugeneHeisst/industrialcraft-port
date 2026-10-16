package ic2.core.ref;

import ic2.core.IC2;
import ic2.core.world.RubberTreeFeature;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2Features {
    public static final DeferredRegister<Feature<?>> REGISTRY = DeferredRegister.create(Registries.FEATURE, IC2.MODID);
    public static final Supplier<Feature<NoneFeatureConfiguration>> RUBBER_TREE = REGISTRY.register("rubber_tree", RubberTreeFeature::new);
    private Ic2Features() {}
}
