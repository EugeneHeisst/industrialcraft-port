package ic2.core.block.misc;

import ic2.core.ref.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Construction foam (soft, walk-through): hardens into a light gray foam wall, or reinforced foam into reinforced stone, after
 * scheduled checks gated by light (light * 6 >= rand(1000)) or at once when clicked with sand.
 */
public class BlockFoam extends Block {
    private final boolean reinforced;
    public BlockFoam(Properties p, boolean reinforced) { super(p); this.reinforced = reinforced; }

    private int tickRate() { return reinforced ? 1000 : 500; }
    private BlockState hardened() { return reinforced ? Ic2Blocks.REINFORCED_STONE.get().defaultBlockState() : Ic2Blocks.CONSTRUCTION_FOAM_WALL.get().defaultBlockState().setValue(BlockFoamWall.COLOR, 7); }

    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return Shapes.empty(); }
    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) { if (!level.isClientSide) level.scheduleTick(pos, this, tickRate()); }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos) * 6 >= random.nextInt(1000)) level.setBlock(pos, hardened(), 3);
        else level.scheduleTick(pos, this, tickRate());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Items.SAND)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide) { level.setBlock(pos, hardened(), 3); if (!player.getAbilities().instabuild) stack.shrink(1); }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Foam can only be sprayed into air, fire or liquids. */
    public static boolean canPlaceFoam(Level level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        return s.isAir() || s.is(net.minecraft.world.level.block.Blocks.FIRE) || s.liquid();
    }
}
