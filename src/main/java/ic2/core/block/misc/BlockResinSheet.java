package ic2.core.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Sticky resin sheet placed with resin on top of a solid block: no collision, slows anything walking through it. */
public class BlockResinSheet extends Block {
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 2, 16);
    public BlockResinSheet(Properties properties) { super(properties); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }
    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return Shapes.empty(); }
    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) { BlockPos b = pos.below(); return level.getBlockState(b).isFaceSturdy(level, b, Direction.UP); }
    @Override protected BlockState updateShape(BlockState state, Direction dir, BlockState n, LevelAccessor level, BlockPos pos, BlockPos np) { return canSurvive(state, level, pos) ? state : Blocks.AIR.defaultBlockState(); }
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.fallDistance *= 0.75f;
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.6, 0.85, 0.6));
    }
}
