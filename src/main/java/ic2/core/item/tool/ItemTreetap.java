package ic2.core.item.tool;

import ic2.core.block.rubber.BlockRubWood;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2Sounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ItemTreetap extends Item {
    public ItemTreetap(Properties properties) { super(properties); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockState state = level.getBlockState(ctx.getClickedPos());
        if (!state.is(Ic2Blocks.RUBBER_WOOD.get())) return InteractionResult.PASS;
        attemptExtract(ctx.getPlayer(), level, ctx.getClickedPos(), ctx.getClickedFace(), null);
        if (!level.isClientSide && ctx.getPlayer() != null) ctx.getItemInHand().hurtAndBreak(1, ctx.getPlayer(), LivingEntity.getSlotForHand(ctx.getHand()));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static void ejectHarz(Level level, BlockPos pos, Direction side, int quantity) {
        double x = pos.getX() + .5 + side.getStepX() * .3, y = pos.getY() + .5, z = pos.getZ() + .5 + side.getStepZ() * .3;
        for (int i = 0; i < quantity; i++) {
            ItemEntity e = new ItemEntity(level, x, y, z, new ItemStack(Ic2Items.get("resin")));
            e.setPickUpDelay(10);
            level.addFreshEntity(e);
        }
    }

    /** Wet hole: 1-3 resin and the hole dries; dry hole: 1/5 chance to close it and 1/5 chance for one resin. */
    public static boolean attemptExtract(Player player, Level level, BlockPos pos, Direction side, List<ItemStack> stacks) {
        BlockState state = level.getBlockState(pos);
        BlockRubWood.Hole hole = state.getValue(BlockRubWood.HOLE);
        if (hole == BlockRubWood.Hole.none || state.getValue(BlockRubWood.FACING) != side) return false;
        if (hole == BlockRubWood.Hole.wet) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(BlockRubWood.HOLE, BlockRubWood.Hole.dry), 3);
                int n = level.random.nextInt(3) + 1;
                if (stacks != null) stacks.add(new ItemStack(Ic2Items.get("resin"), n)); else ejectHarz(level, pos, side, n);
                level.scheduleTick(pos, state.getBlock(), 100);
            }
            playSound(level, player);
            return true;
        }
        if (!level.isClientSide && level.random.nextInt(5) == 0) level.setBlock(pos, state.setValue(BlockRubWood.HOLE, BlockRubWood.Hole.none), 3);
        if (level.random.nextInt(5) != 0) return false;
        if (!level.isClientSide) { if (stacks != null) stacks.add(new ItemStack(Ic2Items.get("resin"))); else ejectHarz(level, pos, side, 1); }
        playSound(level, player);
        return true;
    }

    private static void playSound(Level level, Player player) {
        if (level.isClientSide && player != null) level.playLocalSound(player.getX(), player.getY(), player.getZ(), Ic2Sounds.TREETAP.get(), SoundSource.PLAYERS, 1, 1, false);
    }
}
