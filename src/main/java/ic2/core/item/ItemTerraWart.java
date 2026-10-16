package ic2.core.item;
import ic2.core.ref.Ic2Effects;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.*;
import net.minecraft.world.food.FoodProperties;
public class ItemTerraWart extends Item {
    public ItemTerraWart(Properties p){super(p.rarity(Rarity.RARE).food(new FoodProperties.Builder().nutrition(0).saturationModifier(1).alwaysEdible().build()));}
    @Override public ItemStack finishUsingItem(ItemStack s,Level l,LivingEntity e){
        var result=super.finishUsingItem(s,l,e);
        if(!l.isClientSide){
            for(var effect:java.util.List.of(MobEffects.CONFUSION,MobEffects.DIG_SLOWDOWN,MobEffects.HUNGER,MobEffects.MOVEMENT_SLOWDOWN,MobEffects.WEAKNESS,MobEffects.BLINDNESS,MobEffects.POISON,MobEffects.WITHER))e.removeEffect(effect);
            var radiation=e.getEffect(Ic2Effects.RADIATION);
            if(radiation!=null){int duration=radiation.getDuration();int amp=radiation.getAmplifier();e.removeEffect(Ic2Effects.RADIATION);if(duration>600)e.addEffect(new MobEffectInstance(Ic2Effects.RADIATION,duration-600,amp));}
        }
        return result;
    }
}
