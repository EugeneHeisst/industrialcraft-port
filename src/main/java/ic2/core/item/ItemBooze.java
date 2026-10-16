package ic2.core.item;

import ic2.core.ref.Ic2Components;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** Mug of beer or rum from a booze barrel; what it does depends on the packed booze value. */
public class ItemBooze extends Item {
    public static final String[] solidRatio = {"Watery ", "Clear ", "Lite ", "", "Strong ", "Thick ", "Stodge ", "X"};
    public static final String[] hopsRatio = {"Soup ", "Alcfree ", "White ", "", "Dark ", "Full ", "Black ", "X"};
    public static final String[] timeRatioNames = {"Brew", "Youngster", "Beer", "Ale", "Dragonblood", "Black Stuff"};
    public static final int[] baseDuration = {300, 600, 900, 1200, 1600, 2000, 2400};
    public static final float[] baseIntensity = {0.4f, 0.75f, 1.0f, 1.5f, 2.0f};
    public static final float rumStackability = 2;
    public static final int rumDuration = 600;

    public ItemBooze(Properties p) { super(p.stacksTo(1)); }

    public static int value(ItemStack stack) { return stack.getOrDefault(Ic2Components.BOOZE.get(), 0); }
    public static ItemStack of(int value) { ItemStack s = new ItemStack(Ic2Items.BOOZE_MUG.get()); s.set(Ic2Components.BOOZE.get(), value); return s; }

    /** Model index: beer by age (0-5), rum 6. */
    public static float modelIndex(ItemStack stack) {
        int v = value(stack), type = getTypeOfValue(v);
        if (type == 2) return 6;
        return type == 1 ? Math.min(getTimeRatioOfBeerValue(v), timeRatioNames.length - 1) : 0;
    }

    @Override
    public Component getName(ItemStack stack) {
        int v = value(stack), type = getTypeOfValue(v);
        if (type == 1) {
            int timeRatio = Math.min(getTimeRatioOfBeerValue(v), timeRatioNames.length - 1);
            return Component.literal(timeRatio == timeRatioNames.length - 1 ? timeRatioNames[timeRatio] : solidRatio[getSolidRatioOfBeerValue(v)] + hopsRatio[getHopsRatioOfBeerValue(v)] + timeRatioNames[timeRatio]);
        }
        return Component.literal(type == 2 ? "Rum" : "Zero");
    }

    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 32; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) { return ItemUtils.startUsingInstantly(level, player, hand); }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level.isClientSide || !(entity instanceof Player player)) return new ItemStack(Ic2Items.EMPTY_MUG.get());
        int v = value(stack), type = getTypeOfValue(v);
        if (type == 1) {
            if (getTimeRatioOfBeerValue(v) == 5) return drinkBlackStuff(player);
            int solid = getSolidRatioOfBeerValue(v), alc = getHopsRatioOfBeerValue(v);
            int duration = baseDuration[solid];
            float intensity = baseIntensity[getTimeRatioOfBeerValue(v)];
            player.getFoodData().eat(6 - alc, solid * 0.15f);
            int max = (int)(intensity * alc * 0.5f);
            MobEffectInstance slow = player.getEffect(MobEffects.DIG_SLOWDOWN);
            int lvl = slow == null ? -1 : slow.getAmplifier();
            amplifyEffect(player, MobEffects.DIG_SLOWDOWN, max, intensity, duration);
            if (lvl > -1) {
                amplifyEffect(player, MobEffects.DAMAGE_BOOST, max, intensity, duration);
                if (lvl > 0) {
                    amplifyEffect(player, MobEffects.MOVEMENT_SLOWDOWN, max / 2, intensity, duration);
                    if (lvl > 1) {
                        amplifyEffect(player, MobEffects.DAMAGE_RESISTANCE, max - 1, intensity, duration);
                        if (lvl > 2) {
                            amplifyEffect(player, MobEffects.CONFUSION, 0, intensity, duration);
                            if (lvl > 3) player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, level.random.nextInt(3)));
                        }
                    }
                }
            }
        } else if (type == 2) {
            if (getProgressOfRumValue(v) < 100) drinkBlackStuff(player);
            else {
                amplifyEffect(player, MobEffects.FIRE_RESISTANCE, 0, rumStackability, rumDuration);
                MobEffectInstance def = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
                int lvl = def == null ? -1 : def.getAmplifier();
                amplifyEffect(player, MobEffects.DAMAGE_RESISTANCE, 2, rumStackability, rumDuration);
                if (lvl >= 0) amplifyEffect(player, MobEffects.BLINDNESS, 0, rumStackability, rumDuration);
                if (lvl >= 1) amplifyEffect(player, MobEffects.CONFUSION, 0, rumStackability, rumDuration);
            }
        }
        return new ItemStack(Ic2Items.EMPTY_MUG.get());
    }

    public static void amplifyEffect(Player player, Holder<MobEffect> effect, int max, float intensity, int duration) {
        MobEffectInstance eff = player.getEffect(effect);
        if (eff == null) { player.addEffect(new MobEffectInstance(effect, duration, 0)); return; }
        int newdur = eff.getDuration();
        int maxnewdur = Math.max(0, (int)(duration * (1 + intensity * 2) - newdur) / 2);
        if (maxnewdur < duration) duration = maxnewdur;
        newdur += duration;
        int newamp = eff.getAmplifier();
        if (newamp < max) newamp++;
        player.removeEffect(effect);
        player.addEffect(new MobEffectInstance(effect, newdur, newamp));
    }

    public static ItemStack drinkBlackStuff(Player player) {
        switch (player.level().random.nextInt(6)) {
            case 1 -> player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 1200, 0));
            case 2 -> player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 2400, 0));
            case 3 -> player.addEffect(new MobEffectInstance(MobEffects.POISON, 2400, 0));
            case 4 -> player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2));
            case 5 -> player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, player.level().random.nextInt(4)));
            default -> {}
        }
        return new ItemStack(Ic2Items.EMPTY_MUG.get());
    }

    public static int getTypeOfValue(int value) { return skipGetOfValue(value, 0, 2); }
    public static int getAmountOfValue(int value) { return getTypeOfValue(value) == 0 ? 0 : skipGetOfValue(value, 2, 5) + 1; }
    public static int getSolidRatioOfBeerValue(int value) { return skipGetOfValue(value, 7, 3); }
    public static int getHopsRatioOfBeerValue(int value) { return skipGetOfValue(value, 10, 3); }
    public static int getTimeRatioOfBeerValue(int value) { return skipGetOfValue(value, 13, 3); }
    public static int getProgressOfRumValue(int value) { return skipGetOfValue(value, 7, 7); }
    private static int skipGetOfValue(int value, int bitshift, int take) { return value >> bitshift & (1 << take) - 1; }
}
