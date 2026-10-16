package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Armor ticking and the ISpecialArmor damage handling of the original for IC2 armor. */
@EventBusSubscriber(modid = IC2.MODID)
public final class ArmorEvents {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
    private ArmorEvents() {}

    @SubscribeEvent
    static void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        for (EquipmentSlot slot : ARMOR) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof ItemArmorIC2 armor) armor.onArmorTick(player.level(), player, stack);
        }
    }

    private static boolean hazmatAbsorbs(DamageSource source) {
        return source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.LAVA) || source.is(DamageTypes.ON_FIRE)
            || source.getMsgId().equals("electricity") || source.getMsgId().equals("radiation");
    }

    /** Electric armor absorbs base x ratio of each hit per piece, limited by its charge, paying energyPerDamage per point. */
    @SubscribeEvent
    static void incomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        float damage = event.getAmount();
        if (entity.level().isClientSide) return;
        if (entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ItemArmorUtility.Hazmat && hazmatAbsorbs(source) && ItemArmorUtility.Hazmat.hasCompleteHazmat(entity)) {
            if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.LAVA)) entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 1));
            event.setCanceled(true);
            return;
        }
        if (source.is(DamageTypeTags.BYPASSES_ARMOR)) return;
        double absorbed = 0;
        for (EquipmentSlot slot : ARMOR) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!(stack.getItem() instanceof ItemArmorElectric armor)) continue;
            double ratio = armor.getBaseAbsorptionRatio() * armor.getDamageAbsorptionRatio();
            if (ratio <= 0) continue;
            int perDamage = armor.getEnergyPerDamage();
            double limit = perDamage > 0 ? 25 * ElectricItem.manager.getCharge(stack) / perDamage / 25 : Double.MAX_VALUE;
            double part = Math.min(damage * ratio, limit);
            if (part <= 0) continue;
            absorbed += part;
            ElectricItem.manager.discharge(stack, Math.max(1, Math.round(part)) * (double)perDamage, Integer.MAX_VALUE, true, false, false);
        }
        if (absorbed > 0) event.setAmount((float)Math.max(0, damage - absorbed));
    }

    /** Nano boots cancel falls under 11 blocks, quantum boots any fall, for 5000/20000 EU per damage point; hazmat boots soak short falls. */
    @SubscribeEvent
    static void fall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET);
        if (boots.getItem() instanceof ItemArmorNanoSuit nano) {
            int fallDamage = (int)event.getDistance() - 3;
            if (fallDamage >= 8) return;
            double cost = nano.getEnergyPerDamage() * Math.max(0, fallDamage);
            if (cost <= ElectricItem.manager.getCharge(boots)) { ElectricItem.manager.discharge(boots, cost, Integer.MAX_VALUE, true, false, false); event.setCanceled(true); }
        } else if (boots.getItem() instanceof ItemArmorQuantumSuit quantum) {
            int fallDamage = Math.max((int)event.getDistance() - 10, 0);
            double cost = quantum.getEnergyPerDamage() * fallDamage;
            if (cost <= ElectricItem.manager.getCharge(boots)) { ElectricItem.manager.discharge(boots, cost, Integer.MAX_VALUE, true, false, false); event.setCanceled(true); }
        } else if (boots.getItem() instanceof ItemArmorUtility.Hazmat && entity instanceof Player player) {
            int fallDamage = (int)event.getDistance() - 3;
            if (fallDamage >= 8) return;
            int armorDamage = (fallDamage + 1) / 2;
            if (armorDamage >= 0 && armorDamage <= boots.getMaxDamage() - boots.getDamageValue()) {
                boots.hurtAndBreak(armorDamage, player, EquipmentSlot.FEET);
                event.setCanceled(true);
            }
        }
    }
}
