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
    /** Dye index like the original TileEntityCable.color: 0 = plain insulation (black), 1 red .. 15 white; 16 unused. */
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

    private static int foam(BlockGetter level, BlockPos pos) { return level.getBlockEntity(pos) instanceof TileEntityCable c ? c.foamed : 0; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        if (foam(level, pos) > 0) return Shapes.block();
        return cableShape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        int f = foam(level, pos);
        return f == 2 ? Shapes.block() : f == 1 ? Shapes.empty() : cableShape(state);
    }

    @Override protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) { return foam(level, pos) == 2 ? 15 : 0; }
    @Override protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) { return foam(level, pos) != 2; }

    @Override
    protected float getDestroyProgress(BlockState state, net.minecraft.world.entity.player.Player player, BlockGetter level, BlockPos pos) {
        int f = foam(level, pos);
        float hardness = f == 1 ? 0.01f : f == 2 ? 3 : 0.2f;
        return player.getDestroySpeed(state) / hardness / 30f;
    }

    @Override public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.level.Explosion explosion) { return foam(level, pos) == 2 ? 90 : 6; }

    /** Breaking a foamed cable only removes the foam, like the original removedByPlayer. */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, boolean willHarvest, net.minecraft.world.level.material.FluidState fluid) {
        if (level.getBlockEntity(pos) instanceof TileEntityCable c && c.foamed > 0) {
            if (!level.isClientSide) c.changeFoam(0);
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof TileEntityCable c) {
            boolean sand = stack.is(net.minecraft.world.item.Items.SAND), foam = stack.is(ic2.core.ref.Ic2Blocks.CONSTRUCTION_FOAM.get().asItem());
            if (sand && c.foamed == 1 || foam && c.foamed == 0) {
                c.changeFoam(sand ? 2 : 1);
                if (!level.isClientSide && !player.getAbilities().instabuild) stack.shrink(1);
                return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (level.getBlockEntity(pos) instanceof TileEntityCable c) c.foamTick();
    }

    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return false; }

    private VoxelShape cableShape(BlockState state) {
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
