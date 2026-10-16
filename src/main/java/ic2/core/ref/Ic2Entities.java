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
    public static final Supplier<EntityType<ic2.core.block.explosive.EntityDynamite>> DYNAMITE = REGISTRY.register("dynamite", () -> EntityType.Builder.<ic2.core.block.explosive.EntityDynamite>of(ic2.core.block.explosive.EntityDynamite::new, MobCategory.MISC).sized(.5f, .5f).clientTrackingRange(10).updateInterval(5).build("ic2:dynamite"));
    public static final Supplier<EntityType<ic2.core.block.explosive.EntityIC2Explosive>> IC2_EXPLOSIVE = REGISTRY.register("ic2_explosive", () -> EntityType.Builder.<ic2.core.block.explosive.EntityIC2Explosive>of(ic2.core.block.explosive.EntityIC2Explosive::new, MobCategory.MISC).sized(.98f,.98f).clientTrackingRange(10).updateInterval(5).build("ic2:ic2_explosive"));
    public static final Supplier<EntityType<ic2.core.item.boat.EntityIC2Boat>> CARBON_BOAT = boat("carbon_boat", ic2.core.item.boat.EntityIC2Boat.Kind.CARBON);
    public static final Supplier<EntityType<ic2.core.item.boat.EntityIC2Boat>> RUBBER_BOAT = boat("rubber_boat", ic2.core.item.boat.EntityIC2Boat.Kind.RUBBER);
    public static final Supplier<EntityType<ic2.core.item.boat.EntityIC2Boat>> ELECTRIC_BOAT = boat("electric_boat", ic2.core.item.boat.EntityIC2Boat.Kind.ELECTRIC);

    private static Supplier<EntityType<ic2.core.item.boat.EntityIC2Boat>> boat(String name, ic2.core.item.boat.EntityIC2Boat.Kind kind) {
        return REGISTRY.register(name, () -> {
            var b = EntityType.Builder.<ic2.core.item.boat.EntityIC2Boat>of((type, level) -> new ic2.core.item.boat.EntityIC2Boat(type, level, kind), MobCategory.MISC).sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10);
            if (kind == ic2.core.item.boat.EntityIC2Boat.Kind.ELECTRIC) b = b.fireImmune();
            return b.build("ic2:" + name);
        });
    }

    private Ic2Entities() {}
}
