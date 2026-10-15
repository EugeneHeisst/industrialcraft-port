package ic2.core.item.tool;

import ic2.api.tile.IWrenchable;
import ic2.core.IC2;
import ic2.core.ref.Ic2Sounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Rotates IC2 blocks to the clicked side (sneak: opposite side) or removes them, dropping the block itself with the wrench drop rate. */
public class ItemToolWrench extends Item {
    public ItemToolWrench(Properties properties) { super(properties); }

    public boolean canTakeDamage(ItemStack stack, int amount) { return true; }
    public void damage(ItemStack stack, int damage, Player player, UseOnContext ctx) { stack.hurtAndBreak(damage, player, LivingEntity.getSlotForHand(ctx.getHand())); }
    public boolean overrideWrenchSuccessRate(ItemStack stack) { return false; }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        Player player = ctx.getPlayer();
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (player == null || !canTakeDamage(stack, 1)) return InteractionResult.PASS;
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return InteractionResult.PASS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IWrenchable wrenchable) {
            Direction side = ctx.getClickedFace();
            if (player.isShiftKeyDown()) side = side.getOpposite();
            if (wrenchable.wrenchCanSetFacing(player, side)) {
                if (!level.isClientSide) { wrenchable.setFacing(side); damage(stack, 1, player, ctx); }
                playSound(level, player);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (canTakeDamage(stack, 10) && wrenchable.wrenchCanRemove(player)) {
                if (level instanceof ServerLevel server) {
                    IC2.LOG.info("Player {}/{} used the wrench to remove the {} at {}.", player.getGameProfile().getName(), player.getUUID(), be.getClass().getSimpleName(), pos);
                    boolean dropOriginalBlock;
                    if (wrenchable.getWrenchDropRate() < 1 && overrideWrenchSuccessRate(stack)) {
                        if (!canTakeDamage(stack, 200)) { player.displayClientMessage(Component.literal("Not enough energy for lossless wrench operation"), false); return InteractionResult.SUCCESS; }
                        dropOriginalBlock = true;
                        damage(stack, 200, player, ctx);
                    } else {
                        dropOriginalBlock = level.random.nextFloat() <= wrenchable.getWrenchDropRate();
                        damage(stack, 10, player, ctx);
                    }
                    List<ItemStack> drops = new java.util.ArrayList<>(Block.getDrops(state, server, pos, be, player, stack));
                    if (dropOriginalBlock) {
                        ItemStack wrenchDrop = wrenchable.getWrenchDrop(player);
                        if (!wrenchDrop.isEmpty()) { if (drops.isEmpty()) drops.add(wrenchDrop); else drops.set(0, wrenchDrop); }
                    }
                    level.removeBlock(pos, false);
                    for (ItemStack drop : drops) Block.popResource(level, pos, drop);
                }
                playSound(level, player);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    private static void playSound(Level level, Player player) {
        if (level.isClientSide) level.playLocalSound(player.getX(), player.getY(), player.getZ(), Ic2Sounds.WRENCH.get(), SoundSource.PLAYERS, 1, 1, false);
    }

    @Override public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.level.LevelReader level, BlockPos pos, Player player) { return true; }
}
