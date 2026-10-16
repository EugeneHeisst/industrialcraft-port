package ic2.core.block.rubber;

import com.mojang.serialization.MapCodec;
import ic2.core.world.WorldGenRubTree;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Grows with light >= 9 and a 1/30 chance per random tick; bone meal grows it at once. */
public class BlockRubSapling extends BushBlock implements BonemealableBlock {
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 12, 14);
    public BlockRubSapling(Properties properties) { super(properties); }

    @Override protected MapCodec<? extends BushBlock> codec() { throw new UnsupportedOperationException(); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(30) == 0) WorldGenRubTree.grow(level, pos, random);
    }

    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return true; }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return true; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) { WorldGenRubTree.grow(level, pos, random); }
}
