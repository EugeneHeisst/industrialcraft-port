package ic2.core.block.wiring;

import ic2.core.block.BlockTileEntity;
import ic2.core.block.TileEntityBlock;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

/** Charge pad block: 0.95 high; players standing on it are charged; looking up while placing puts the output at the bottom. */
public class BlockChargepad extends BlockElectric {
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 15.2, 16);
    private static final DustParticleOptions BLUE = new DustParticleOptions(new Vector3f(0.2f, 0.2f, 1f), 1f);

    public BlockChargepad(Properties properties, Supplier<? extends BlockEntityType<? extends TileEntityBlock>> type) { super(properties, type); }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getPlayer() != null && ctx.getPlayer().getXRot() <= -65 ? Direction.DOWN : ctx.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }
    @Override protected boolean useShapeForLightOcclusion(BlockState state) { return true; }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof Player p && level.getBlockEntity(pos) instanceof TileEntityChargepad te) te.playerStandsAt(p);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (!state.getValue(ACTIVE)) return;
        for (int i = 0; i < 20; i++) level.addParticle(BLUE, pos.getX() + rand.nextFloat(), pos.getY() + 0.9 + rand.nextFloat(), pos.getZ() + rand.nextFloat(), 0, 0.1, 0);
    }
}
