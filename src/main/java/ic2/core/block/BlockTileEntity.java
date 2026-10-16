package ic2.core.block;

import com.mojang.serialization.MapCodec;
import ic2.core.IHasGui;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Block of one IC2 block entity (the original's BlockMultiID metadata entry). */
public class BlockTileEntity extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private final Supplier<? extends BlockEntityType<? extends TileEntityBlock>> type;

    public BlockTileEntity(Properties properties, Supplier<? extends BlockEntityType<? extends TileEntityBlock>> type) {
        super(properties);
        this.type = type;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { throw new UnsupportedOperationException(); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, ACTIVE); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return type.get().create(pos, state); }

    /** Front towards the placer, like BlockMultiID.onBlockPlacedBy. */
    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getPlayer() == null ? Direction.NORTH : ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> beType) {
        return beType == type.get() ? (l, p, s, be) -> ((TileEntityBlock)be).tick() : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (level.getBlockEntity(pos) instanceof TileEntityBlock teb && teb.onActivated(player)) return InteractionResult.sidedSuccess(level.isClientSide);
        if (!(level.getBlockEntity(pos) instanceof IHasGui gui)) return InteractionResult.PASS;
        if (!level.isClientSide && player instanceof ServerPlayer sp)
            sp.openMenu(new SimpleMenuProvider((id, inv, p) -> gui.getGuiContainer(id, inv), getName()), buf -> buf.writeBlockPos(pos));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileEntityBlock teb) teb.onBlockBreak();
            if (be instanceof TileEntityInventory inv) { Containers.dropContents(level, pos, inv); }
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moving) {
        super.neighborChanged(state, level, pos, block, from, moving);
        if (level.getBlockEntity(pos) instanceof TileEntityBlock teb) teb.onNeighborUpdate(from);
    }

    public static ItemStack stackOf(BlockState state) { return new ItemStack(state.getBlock()); }
}
