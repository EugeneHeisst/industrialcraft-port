package ic2.core.ref;

import ic2.core.IC2;
import ic2.core.util.Ic2DamageSources;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2Effects {
    public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(Registries.MOB_EFFECT, IC2.MODID);

    /** Radiation: amplifier / 100 + 0.5 radiation damage every (25 >> amplifier) ticks, like IC2Potion of the original. */
    public static final DeferredHolder<MobEffect, MobEffect> RADIATION = REGISTRY.register("radiation", () -> new MobEffect(MobEffectCategory.HARMFUL, 5149489) {
        @Override public boolean applyEffectTick(LivingEntity entity, int amplifier) { entity.hurt(Ic2DamageSources.get(entity.level(), Ic2DamageSources.RADIATION), amplifier / 100 + 0.5f); return true; }
        @Override public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { int rate = 25 >> amplifier; return rate <= 0 || duration % rate == 0; }
    });

    private Ic2Effects() {}
}
