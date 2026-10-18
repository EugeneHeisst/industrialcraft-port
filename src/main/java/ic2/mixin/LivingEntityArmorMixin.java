package ic2.mixin;

import ic2.core.item.armor.ArmorEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Special armor pays only for accepted damage, before vanilla armor and potion reductions. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityArmorMixin {
    @ModifyVariable(method = "getDamageAfterArmorAbsorb", at = @At("HEAD"), argsOnly = true)
    private float ic2$absorb(float damage, DamageSource source, float originalDamage) {
        return ArmorEvents.absorbDamage((LivingEntity)(Object)this, source, damage);
    }
}
