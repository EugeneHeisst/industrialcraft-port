package ic2.core.item;

import ic2.core.block.misc.BlockBarrel;
import ic2.core.block.misc.TileEntityBarrel;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Components;
import ic2.core.ref.Ic2Items;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/** Booze barrel item: placed on a wooden scaffold, keeps the brew it was taken off with. */
public class ItemBarrel extends Item {
    public ItemBarrel(Properties p) { super(p.stacksTo(1)); }

    public static ItemStack of(int value) { ItemStack s = new ItemStack(Ic2Items.BARREL.get()); if (value != 0) s.set(Ic2Components.BOOZE.get(), value); return s; }

    @Override
    public Component getName(ItemStack stack) {
        int v = ItemBooze.getAmountOfValue(stack.getOrDefault(Ic2Components.BOOZE.get(), 0));
        if (v <= 0) return Component.translatable("ic2.item.EmptyBoozeBarrel");
        String filled = Component.translatable("ic2.item.LBoozeBarrel").getString();
        return Component.literal(filled.startsWith("L ") ? v + filled : filled + " (" + v + ")");
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        var level = ctx.getLevel();
        var pos = ctx.getClickedPos();
        if (!BlockBarrel.isWoodScaffold(level, pos)) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        level.setBlock(pos, Ic2Blocks.BARREL.get().defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof TileEntityBarrel te) te.set(ctx.getItemInHand().getOrDefault(Ic2Components.BOOZE.get(), 0));
        if (ctx.getPlayer() == null || !ctx.getPlayer().getAbilities().instabuild) ctx.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
