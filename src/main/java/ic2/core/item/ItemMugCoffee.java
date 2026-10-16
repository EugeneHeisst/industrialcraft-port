package ic2.core.item;

import ic2.core.ref.Ic2Items;
import net.minecraft.core.Holder;
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

/** Cold (0), dark (1) and finished (2) coffee: speed and haste that stack up, too much makes you sick. */
public class ItemMugCoffee extends Item {
    private final int meta;

    public ItemMugCoffee(Properties p, int meta) { super(p.stacksTo(1)); this.meta = meta; }

    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 32; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) { return ItemUtils.startUsingInstantly(level, player, hand); }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            int highest = Math.max(amplifyEffect(player, MobEffects.MOVEMENT_SPEED), amplifyEffect(player, MobEffects.DIG_SPEED));
            if (meta == 2) highest -= 2;
            if (highest >= 3) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, (highest - 2) * 200, 0));
                if (highest >= 4) player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, highest - 3));
            }
        }
        return new ItemStack(Ic2Items.EMPTY_MUG.get());
    }

    public int amplifyEffect(Player player, Holder<MobEffect> effect) {
        MobEffectInstance eff = player.getEffect(effect);
        if (eff == null) { player.addEffect(new MobEffectInstance(effect, 300, 0)); return 1; }
        int max = meta == 1 ? 5 : meta == 2 ? 6 : 1;
        int newAmp = eff.getAmplifier(), newDur = eff.getDuration();
        if (newAmp < max) newAmp++;
        newDur += meta == 0 ? 600 : 1200;
        player.addEffect(new MobEffectInstance(effect, newDur, newAmp));
        return newAmp;
    }
}
