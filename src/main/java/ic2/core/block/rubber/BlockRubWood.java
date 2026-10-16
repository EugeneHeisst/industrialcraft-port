package ic2.core.block.rubber;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/** Rubber wood: plain or with a wet/dry resin hole facing one side (metadata 2-5 / 8-11 of the original). Dry holes refill slowly. */
public class BlockRubWood extends Block {
    public enum Hole implements StringRepresentable {
        none, wet, dry;
        @Override public String getSerializedName() { return name(); }
    }
    public static final EnumProperty<Hole> HOLE = EnumProperty.create("hole", Hole.class);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BlockRubWood(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HOLE, Hole.none).setValue(FACING, Direction.NORTH));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(HOLE, FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) { return defaultBlockState(); }

    /** Scheduled every 100 ticks while dry; 1 in 200 per check turns the hole wet again. */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HOLE) != Hole.dry) return;
        if (random.nextInt(200) == 0) level.setBlock(pos, state.setValue(HOLE, Hole.wet), 3);
        else level.scheduleTick(pos, this, 100);
    }

    @Override public boolean isFlammable(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, Direction face) { return true; }
    @Override public int getFlammability(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, Direction face) { return 20; }
    @Override public int getFireSpreadSpeed(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, Direction face) { return 4; }
}
