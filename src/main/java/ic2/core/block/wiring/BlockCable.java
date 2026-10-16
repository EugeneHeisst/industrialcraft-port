package ic2.core.block.wiring;

import com.mojang.serialization.MapCodec;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.TileEntityBlock;
import ic2.core.ref.Ic2BlockEntities;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockCable extends BaseEntityBlock {
    public static final Map<Direction, BooleanProperty> PROPS = PipeBlock.PROPERTY_BY_DIRECTION;
    /** 0 = uncoloured, 1-16 = dye colour + 1 like the original TileEntityCable.color. */
    public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 16);
    public static final BooleanProperty ACTIVE = BlockTileEntity.ACTIVE;
    private static final Map<CableType, BlockCable> BY_TYPE = new EnumMap<>(CableType.class);
    public final CableType type;
    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

    public BlockCable(Properties properties, CableType type) {
        super(properties);
        this.type = type;
        BY_TYPE.put(type, this);
        BlockState def = stateDefinition.any().setValue(COLOR, 0).setValue(ACTIVE, false);
        for (var p : PROPS.values()) def = def.setValue(p, false);
        registerDefaultState(def);
    }

    public static BlockCable byType(CableType type) { return BY_TYPE.get(type); }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { throw new UnsupportedOperationException(); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(COLOR, ACTIVE); PROPS.values().forEach(builder::add); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (type) { case detector -> new TileEntityCableDetector(pos, state); case splitter -> new TileEntityCableSplitter(pos, state); default -> new TileEntityCable(pos, state); };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> beType) {
        return level.isClientSide || type != CableType.detector && type != CableType.splitter ? null : (l, p, s, be) -> ((TileEntityBlock)be).tick();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return shapes.computeIfAbsent(state, s -> {
            double sp = (1 - type.thickness) / 2 * 16, th = type.thickness * 16;
            VoxelShape shape = Block.box(sp, sp, sp, sp + th, sp + th, sp + th);
            for (Direction dir : Direction.values()) {
                if (!s.getValue(PROPS.get(dir))) continue;
                double[] min = {sp, sp, sp}, max = {sp + th, sp + th, sp + th};
                int axis = dir.getAxis().ordinal();
                if (dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE) min[axis] = 0; else max[axis] = 16;
                shape = Shapes.or(shape, Block.box(min[0], min[1], min[2], max[0], max[1], max[2]));
            }
            return shape;
        });
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moving) {
        super.neighborChanged(state, level, pos, block, from, moving);
        if (level.getBlockEntity(pos) instanceof TileEntityCable cable) cable.onNeighborUpdate(from);
    }

    @Override protected boolean isSignalSource(BlockState state) { return type == CableType.detector; }
    @Override protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) { return type == CableType.detector && state.getValue(ACTIVE) ? 15 : 0; }
}
