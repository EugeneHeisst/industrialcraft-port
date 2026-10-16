package ic2.core.block.reactor;

import ic2.core.block.BlockTileEntity;
import ic2.core.block.TileEntityBlock;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Reactor chamber: must touch exactly one reactor; smokes and burns with the reactor heat. */
public class BlockReactorChamber extends BlockTileEntity {
    public BlockReactorChamber(Properties properties, Supplier<? extends BlockEntityType<? extends TileEntityBlock>> type) { super(properties, type); }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        int count = 0;
        for (Direction d : Direction.values()) if (level.hasChunkAt(pos.relative(d)) && level.getBlockEntity(pos.relative(d)) instanceof TileEntityNuclearReactor) count++;
        return count == 1;
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) { return canSurvive(defaultBlockState(), ctx.getLevel(), ctx.getClickedPos()) ? super.getStateForPlacement(ctx) : null; }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return canSurvive(state, level, pos) ? super.updateShape(state, dir, neighbor, level, pos, neighborPos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof TileEntityReactorChamber c) || c.getReactor() == null) return;
        int puffs = c.getReactor().heat / 1000;
        if (puffs <= 0) return;
        puffs = random.nextInt(puffs);
        for (int n = 0; n < puffs; n++) level.addParticle(ParticleTypes.SMOKE, pos.getX() + random.nextFloat(), pos.getY() + 0.95, pos.getZ() + random.nextFloat(), 0, 0, 0);
        puffs -= random.nextInt(4) + 3;
        for (int n = 0; n < puffs; n++) level.addParticle(ParticleTypes.FLAME, pos.getX() + random.nextFloat(), pos.getY() + 1, pos.getZ() + random.nextFloat(), 0, 0, 0);
    }
}
