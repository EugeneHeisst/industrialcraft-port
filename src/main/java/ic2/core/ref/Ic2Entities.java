package ic2.core.ref;

import ic2.core.IC2;
import ic2.core.item.tool.EntityMiningLaser;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2Entities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(Registries.ENTITY_TYPE, IC2.MODID);
    public static final Supplier<EntityType<EntityMiningLaser>> MINING_LASER = REGISTRY.register("mining_laser", () -> EntityType.Builder.<EntityMiningLaser>of(EntityMiningLaser::new, MobCategory.MISC).sized(0.8f, 0.8f).clientTrackingRange(10).updateInterval(5).noSave().build("ic2:mining_laser"));
    private Ic2Entities() {}
}
