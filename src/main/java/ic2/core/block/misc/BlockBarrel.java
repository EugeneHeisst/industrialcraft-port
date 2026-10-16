package ic2.core.block.misc;

import com.mojang.serialization.MapCodec;
import ic2.core.item.ItemBarrel;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Booze barrel set on a wooden scaffold. Left click takes the tap off, or the (filled) barrel back off the scaffold. */
public class BlockBarrel extends BaseEntityBlock {
    public enum Tap implements StringRepresentable {
        NONE, NORTH, SOUTH, WEST, EAST;
        @Override public String getSerializedName() { return name().toLowerCase(java.util.Locale.ROOT); }
        public static Tap of(int side) { return side >= 2 && side <= 5 ? values()[side - 1] : NONE; }
    }
    public static final EnumProperty<Tap> TAP = EnumProperty.create("tap", Tap.class);

    public BlockBarrel(Properties p) { super(p); registerDefaultState(defaultBlockState().setValue(TAP, Tap.NONE)); }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { throw new UnsupportedOperationException(); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(TAP); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new TileEntityBarrel(pos, state); }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide || type != Ic2BlockEntities.BARREL.get() ? null : (l, p, s, be) -> ((TileEntityBarrel)be).serverTick();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof TileEntityBarrel te)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        return te.rightclick(player, hand) ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof TileEntityBarrel te)) return;
        if (te.treetapSide > 1) {
            popResource(level, pos, new ItemStack(Ic2Items.TREETAP.get()));
            te.setTreetapSide(0);
            te.drainLiquid(1);
        } else {
            popResource(level, pos, ItemBarrel.of(te.calculateMetaValue()));
            level.setBlock(pos, Ic2Blocks.SCAFFOLD.get().defaultBlockState(), 3);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return level.getBlockEntity(pos) instanceof TileEntityBarrel te ? ItemBarrel.of(te.calculateMetaValue()) : new ItemStack(Ic2Items.BARREL.get());
    }

    /** A filled barrel on a scaffold may stand only where the scaffold could. */
    public static boolean isWoodScaffold(BlockGetter level, BlockPos pos) { return level.getBlockState(pos).is(Ic2Blocks.SCAFFOLD.get()); }
}
