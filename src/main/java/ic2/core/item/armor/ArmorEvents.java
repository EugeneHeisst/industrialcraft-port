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

    /** Hazmat immunity is an incoming-damage cancellation, before the damage pipeline. */
    @SubscribeEvent
    static void incomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        if (entity.level().isClientSide) return;
        if (entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ItemArmorUtility.Hazmat && hazmatAbsorbs(source) && ItemArmorUtility.Hazmat.hasCompleteHazmat(entity)) {
            if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.LAVA)) entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 1));
            event.setCanceled(true);
            return;
        }
    }

    /** Called at the armor stage, after shields and hurt cooldown accept the hit. */
    public static float absorbDamage(LivingEntity entity, DamageSource source, float damage) {
        // Original boots override the normal armor-bypassing fall source with priority10 special absorption.
        // LivingFallEvent above the damage calculation already handles their fully funded cancellation cases.
        if (source.is(DamageTypes.FALL)) {
            ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET);
            if (boots.getItem() instanceof ItemArmorNanoSuit nano && nano.armorType() == 3) {
                return absorbBootFall(damage, boots, nano, damage < 8 ? 1 : 0.875);
            }
            if (boots.getItem() instanceof ItemArmorQuantumSuit quantum && quantum.armorType() == 3) {
                return absorbBootFall(damage, boots, quantum, 1);
            }
        }
        if (source.is(DamageTypeTags.BYPASSES_ARMOR)) return damage;
        // Original IC2 ItemArmorElectric.getProperties -> ArmorProperties(0, base x ratio, int(25 x charge / energyPerDamage)),
        // applied together by Forge 1.7.10 ApplyArmor; damageArmor pays the integer damage points x energyPerDamage.
        java.util.List<Props> list = new java.util.ArrayList<>();
        for (int x = 0; x < ARMOR.length; x++) {
            ItemStack stack = entity.getItemBySlot(ARMOR[x]);
            if (!(stack.getItem() instanceof ItemArmorElectric armor)) continue;
            int perDamage = armor.getEnergyPerDamage();
            int max = perDamage > 0 ? (int)Math.min(Integer.MAX_VALUE, 25 * ElectricItem.manager.getCharge(stack) / perDamage) : Integer.MAX_VALUE;
            list.add(new Props(0, armor.getBaseAbsorptionRatio() * armor.getDamageAbsorptionRatio(), max, x));
        }
        if (list.isEmpty()) return damage;
        double scaled = damage * 25d;
        Props[] props = list.toArray(new Props[0]);
        standardize(props, scaled);
        int level = props[0].priority;
        double ratio = 0;
        for (Props prop : props) {
            if (level != prop.priority) { scaled -= scaled * ratio; ratio = 0; level = prop.priority; }
            ratio += prop.ratio;
            double absorb = scaled * prop.ratio;
            if (absorb > 0) {
                ItemStack stack = entity.getItemBySlot(ARMOR[prop.slot]);
                int itemDamage = (int)(absorb / 25d < 1 ? 1 : absorb / 25d);
                ElectricItem.manager.discharge(stack, itemDamage * (double)((ItemArmorElectric)stack.getItem()).getEnergyPerDamage(), Integer.MAX_VALUE, true, false, false);
            }
        }
        scaled -= scaled * ratio;
        return (float)Math.max(0, scaled / 25d);
    }

    /** Forge 1.7.10 ISpecialArmor.ArmorProperties (Priority, AbsorbRatio, AbsorbMax on the 25-scale, Slot) and its ordering. */
    private static final class Props implements Comparable<Props> {
        final int priority, slot;
        double ratio;
        int max;
        Props(int priority, double ratio, int max, int slot) { this.priority = priority; this.ratio = ratio; this.max = max; this.slot = slot; }
        @Override public int compareTo(Props o) {
            if (o.priority != priority) return o.priority - priority;
            double left = ratio == 0 ? 0 : max * 100.0 / ratio, right = o.ratio == 0 ? 0 : o.max * 100.0 / o.ratio;
            return (int)(left - right);
        }
    }

    /** Forge 1.7.10 ArmorProperties.StandardizeList, verbatim: normalises a priority whose ratios exceed 1 and caps each by AbsorbMax. */
    private static void standardize(Props[] armor, double damage) {
        java.util.Arrays.sort(armor);
        int start = 0, priority = armor[0].priority, pStart = 0;
        double total = 0;
        boolean pChange = false, pFinished = false;
        for (int x = 0; x < armor.length; x++) {
            total += armor[x].ratio;
            if (x == armor.length - 1 || armor[x].priority != priority) {
                if (armor[x].priority != priority) { total -= armor[x].ratio; x--; pChange = true; }
                if (total > 1) {
                    for (int y = start; y <= x; y++) {
                        double newRatio = armor[y].ratio / total;
                        if (newRatio * damage > armor[y].max) {
                            armor[y].ratio = (double)armor[y].max / damage;
                            total = 0;
                            for (int z = pStart; z <= y; z++) total += armor[z].ratio;
                            start = y + 1;
                            x = y;
                            break;
                        } else {
                            armor[y].ratio = newRatio;
                            pFinished = true;
                        }
                    }
                    if (pChange && pFinished) {
                        damage -= damage * total;
                        total = 0;
                        start = x + 1;
                        priority = armor[start].priority;
                        pStart = start;
                        pChange = false;
                        pFinished = false;
                        if (damage <= 0) { for (int y = x + 1; y < armor.length; y++) armor[y].ratio = 0; break; }
                    }
                } else {
                    for (int y = start; y <= x; y++) {
                        total -= armor[y].ratio;
                        if (damage * armor[y].ratio > armor[y].max) armor[y].ratio = (double)armor[y].max / damage;
                        total += armor[y].ratio;
                    }
                    damage -= damage * total;
                    total = 0;
                    if (x != armor.length - 1) {
                        start = x + 1;
                        priority = armor[start].priority;
                        pStart = start;
                        pChange = false;
                        if (damage <= 0) { for (int y = x + 1; y < armor.length; y++) armor[y].ratio = 0; break; }
                    }
                }
            }
        }
    }

    /** Forge1.7 special armor used an integer25-scale absorption budget, then floor/minimum-one damage payment. */
    private static float absorbBootFall(float damage, ItemStack boots, ItemArmorElectric armor, double ratio) {
        int perDamage = armor.getEnergyPerDamage();
        int budget = perDamage > 0 ? (int)Math.min(Integer.MAX_VALUE, 25 * ElectricItem.manager.getCharge(boots) / perDamage) : Integer.MAX_VALUE;
        double absorbed = Math.min(damage * ratio, budget / 25d);
        if (absorbed <= 0) return damage;
        if (perDamage > 0) ElectricItem.manager.discharge(boots, Math.max(1, (int)absorbed) * (double)perDamage, Integer.MAX_VALUE, true, false, false);
        return (float)Math.max(0, damage - absorbed);
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
