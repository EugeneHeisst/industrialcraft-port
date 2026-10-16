package ic2.core.item;

import ic2.core.ref.Ic2Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Filled tin can: right click eats as many cans as the hunger bar needs (1 food + 1 saturation each), returning empty cans. */
public class ItemTinCan extends Item {
    public ItemTinCan(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer) || !player.getFoodData().needsFood()) return InteractionResultHolder.pass(stack);
        int need = 20 - player.getFoodData().getFoodLevel();
        int eat = Math.min(need, stack.getCount());
        if (eat <= 0) return InteractionResultHolder.pass(stack);
        player.getFoodData().eat(eat, eat);
        stack.shrink(eat);
        ItemStack empty = new ItemStack(Ic2Items.get("tin_can"), eat);
        if (!player.getInventory().add(empty)) player.drop(empty, false);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1, 1);
        return InteractionResultHolder.success(stack);
    }
}
