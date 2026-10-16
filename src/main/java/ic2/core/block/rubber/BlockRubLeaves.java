package ic2.core.block.rubber;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BlockRubLeaves extends LeavesBlock {
    public BlockRubLeaves(Properties properties) { super(properties); }
    @Override public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) { return 20; }
    @Override public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) { return 30; }
}
