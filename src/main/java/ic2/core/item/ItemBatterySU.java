package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.core.util.Util;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Single-use battery: right click dumps its charge into the hotbar; also burns in discharge slots (1200 EU). */
public class ItemBatterySU extends Item {
    public final int capacity, tier;
    public ItemBatterySU(Properties properties, int capacity, int tier) { super(properties); this.capacity = capacity; this.tier = tier; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        double energy = capacity;
        for (int i = 0; i < 9 && energy > 0; i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!stack.isEmpty() && stack != itemstack) energy -= ElectricItem.manager.charge(stack, energy, tier, true, false);
        }
        if (!Util.isSimilar(energy, capacity)) itemstack.shrink(1);
        return InteractionResultHolder.pass(itemstack);
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) { list.add(Component.literal(capacity + " EU").withStyle(ChatFormatting.GRAY)); }
}
