package ic2.core.item;

import ic2.core.block.misc.TileEntityBarrel;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/** Stone mug: click the tapped side of a booze barrel to draw a mug of its booze. */
public class ItemMug extends Item {
    public ItemMug(Properties p) { super(p.stacksTo(1)); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        var level = ctx.getLevel();
        if (!level.getBlockState(ctx.getClickedPos()).is(Ic2Blocks.BARREL.get()) || !(level.getBlockEntity(ctx.getClickedPos()) instanceof TileEntityBarrel barrel)) return InteractionResult.PASS;
        if (barrel.treetapSide < 2 || barrel.treetapSide != ctx.getClickedFace().get3DDataValue()) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        int value = barrel.calculateMetaValue();
        if (!barrel.drainLiquid(1)) return InteractionResult.FAIL;
        ItemStack booze = ItemBooze.of(value);
        ItemStack hand = ctx.getItemInHand();
        var player = ctx.getPlayer();
        if (hand.getCount() > 1 && player != null) {
            hand.shrink(1);
            if (!player.getInventory().add(booze)) player.drop(booze, false);
        } else if (player != null) player.setItemInHand(ctx.getHand(), booze);
        return InteractionResult.SUCCESS;
    }
}
