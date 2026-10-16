package ic2.core.util;

import ic2.core.IC2;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

/** IC2DamageSource of the original as data-driven damage types (data/ic2/damage_type). */
public final class Ic2DamageSources {
    public static final ResourceKey<DamageType> ELECTRICITY = ResourceKey.create(Registries.DAMAGE_TYPE, IC2.id("electricity"));
    public static final ResourceKey<DamageType> RADIATION = ResourceKey.create(Registries.DAMAGE_TYPE, IC2.id("radiation"));
    public static final ResourceKey<DamageType> NUKE = ResourceKey.create(Registries.DAMAGE_TYPE, IC2.id("nuke"));
    private Ic2DamageSources() {}
    public static DamageSource get(Level level, ResourceKey<DamageType> key) { return level.damageSources().source(key); }
}
