package ic2.core.block.explosive;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Placed dynamite stick (plain or linked to a remote), torch-like on a floor or wall. Breaking it, redstone or a remote lights it
 * (40-tick fuse); explosions light it with a 5-tick fuse; losing every support makes it go off, losing just its own support removes it.
 */
public class BlockDynamite extends Block {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", d -> d != Direction.DOWN);
    private static final Map<Direction, VoxelShape> SHAPES = Map.of(Direction.UP, box(6.4, 0, 6.4, 9.6, 9.6, 9.6), Direction.EAST, box(0, 3.2, 5.6, 4.8, 12.8, 10.4),
        Direction.WEST, box(11.2, 3.2, 5.6, 16, 12.8, 10.4), Direction.SOUTH, box(5.6, 3.2, 0, 10.4, 12.8, 4.8), Direction.NORTH, box(5.6, 3.2, 11.2, 10.4, 12.8, 16));

    public BlockDynamite(Properties p) { super(p); registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return SHAPES.get(s.getValue(FACING)); }
    @Override protected VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return Shapes.empty(); }

    private static boolean solid(LevelReader l, BlockPos p) { return l.getBlockState(p).isRedstoneConductor(l, p); }
    /** support block of a facing: below for UP, behind for walls */
    private static BlockPos support(BlockPos pos, Direction facing) { return pos.relative(facing.getOpposite()); }

    public static boolean canPlaceAt(LevelReader l, BlockPos p) {
        return solid(l, p.west()) || solid(l, p.east()) || solid(l, p.north()) || solid(l, p.south()) || solid(l, p.below());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        LevelReader l = ctx.getLevel();
        BlockPos p = ctx.getClickedPos();
        Direction face = ctx.getClickedFace();
        if (face != Direction.DOWN && solid(l, support(p, face))) return defaultBlockState().setValue(FACING, face);
        for (Direction d : new Direction[]{Direction.UP, Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH})
            if (solid(l, support(p, d))) return defaultBlockState().setValue(FACING, d);
        return null;
    }

    @Override protected boolean canSurvive(BlockState s, LevelReader l, BlockPos p) { return canPlaceAt(l, p); }

    public static void ignite(Level level, BlockPos pos, Player player, int fuse) {
        if (level.isClientSide) return;
        level.removeBlock(pos, false);
        EntityDynamite e = new EntityDynamite(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, false);
        e.setOwner(player);
        e.fuse = fuse;
        level.addFreshEntity(e);
        if (fuse > 5) level.playSound(null, e, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1, 1);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        ignite(level, pos, player, 40);
        return true;
    }

    @Override protected void onPlace(BlockState s, Level level, BlockPos pos, BlockState old, boolean moving) { if (!level.isClientSide && level.hasNeighborSignal(pos)) ignite(level, pos, null, 40); }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moving) {
        if (level.isClientSide) return;
        if (level.hasNeighborSignal(pos)) { ignite(level, pos, null, 40); return; }
        if (!canPlaceAt(level, pos)) ignite(level, pos, null, 5);
        else if (!solid(level, support(pos, state.getValue(FACING)))) level.removeBlock(pos, false);
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (level.isClientSide) return;
        EntityDynamite e = new EntityDynamite(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, false);
        e.setOwner(explosion.getIndirectSourceEntity());
        e.fuse = 5;
        level.addFreshEntity(e);
    }
}
