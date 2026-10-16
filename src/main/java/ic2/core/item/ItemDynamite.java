package ic2.core.item;

import ic2.api.item.IBoxable;
import ic2.core.block.explosive.BlockDynamite;
import ic2.core.block.explosive.EntityDynamite;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Dynamite (placeable, thrown with right click in the air) and sticky dynamite (thrown only); dispensers launch both. */
public class ItemDynamite extends Item implements IBoxable, ProjectileItem {
    public final boolean sticky;
    public ItemDynamite(Properties p, boolean sticky) { super(p.stacksTo(16)); this.sticky = sticky; }

    @Override public boolean canBeStoredInToolbox(ItemStack stack) { return true; }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (sticky) return InteractionResult.PASS;
        Level level = ctx.getLevel();
        var placeCtx = new BlockPlaceContext(ctx);
        var pos = placeCtx.getClickedPos();
        if (!level.getBlockState(pos).isAir() || !BlockDynamite.canPlaceAt(level, pos)) return InteractionResult.sidedSuccess(level.isClientSide);
        var state = Ic2Blocks.DYNAMITE.get().getStateForPlacement(placeCtx);
        if (state == null) return InteractionResult.sidedSuccess(level.isClientSide);
        if (!level.isClientSide) { level.setBlock(pos, state, 3); ctx.getItemInHand().shrink(1); }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.5f, 0.4f / (level.random.nextFloat() * 0.4f + 0.8f));
        if (!level.isClientSide) level.addFreshEntity(new EntityDynamite(level, player, sticky));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) { return new EntityDynamite(level, pos.x(), pos.y(), pos.z(), sticky); }
}
