package ic2.core.block.machine.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Mining pipe shaft (4/16 wide like the original bounds) and the full-cube drill tip; only the miner places them. */
public class BlockMiningPipe extends Block {
    private static final VoxelShape SHAFT = box(6, 0, 6, 10, 16, 10);
    private final boolean tip;
    public BlockMiningPipe(Properties properties, boolean tip) { super(properties); this.tip = tip; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return tip ? Shapes.block() : SHAFT; }
}
