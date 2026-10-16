package ic2.core.block.misc;

import ic2.core.util.Keyboard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Rubber sheet (trampoline): hung between solid blocks it bounces entities; a span without support tears under living entities. */
public class BlockRubberSheet extends Block {
    public static final BooleanProperty SUPPORTED = BooleanProperty.create("supported");
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 2, 16);

    public BlockRubberSheet(Properties properties) { super(properties); registerDefaultState(defaultBlockState().setValue(SUPPORTED, false)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(SUPPORTED); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }

    private boolean supporter(BlockGetter level, BlockPos p) { BlockState s = level.getBlockState(p); return s.isRedstoneConductor(level, p) || s.is(this); }
    private static boolean solid(BlockGetter level, BlockPos p) { return level.getBlockState(p).isRedstoneConductor(level, p); }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        for (Direction d : Direction.Plane.HORIZONTAL) if (supporter(level, pos.relative(d))) return true;
        return false;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!canSurvive(state, level, pos)) return Blocks.AIR.defaultBlockState();
        return state.setValue(SUPPORTED, false);
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) { return canSurvive(defaultBlockState(), ctx.getLevel(), ctx.getClickedPos()) ? defaultBlockState() : null; }

    private boolean walk(Level level, BlockPos pos, Direction d) {
        BlockPos.MutableBlockPos p = pos.mutable();
        while (true) {
            if (solid(level, p)) return true;
            if (!level.getBlockState(p).is(this)) return false;
            if (solid(level, p.below())) return true;
            p.move(d);
        }
    }

    public boolean canSupportWeight(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(SUPPORTED)) return true;
        boolean ok = walk(level, pos, Direction.WEST) && walk(level, pos, Direction.EAST) || walk(level, pos, Direction.NORTH) && walk(level, pos, Direction.SOUTH);
        if (ok) level.setBlock(pos, state.setValue(SUPPORTED, true), 3);
        return ok;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && !solid(level, pos.below()) && entity instanceof LivingEntity && !canSupportWeight(level, pos, state)) level.destroyBlock(pos, false);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (solid(level, pos.below())) super.fallOn(level, state, pos, entity, fallDistance);
        else entity.resetFallDistance();
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        Vec3 m = entity.getDeltaMovement();
        BlockPos pos = entity.getOnPos();
        if (m.y > -0.4 || solid(level, pos.below())) { super.updateEntityAfterFallOn(level, entity); return; }
        double f = entity instanceof Player p && Keyboard.isJumpKeyDown(p) ? -1.3 : entity instanceof Player p2 && p2.isShiftKeyDown() ? -0.1 : -0.8;
        entity.setDeltaMovement(m.x * 1.1, m.y * f, m.z * 1.1);
    }
}
