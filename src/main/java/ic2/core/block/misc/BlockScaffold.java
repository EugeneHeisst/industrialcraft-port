package ic2.core.block.misc;

import ic2.core.ref.Ic2Blocks;
import ic2.core.util.Keyboard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Wooden (strength 2, reinforced 5) and iron (5 / 12) scaffold. Strength drops by one per block away from a supported pillar;
 * pillars can be reinforced with two sticks / one iron fence. Players climb by walking into it; left click with scaffold stacks upward.
 */
public class BlockScaffold extends Block {
    public static final IntegerProperty STRENGTH = IntegerProperty.create("strength", 0, 12);
    private static final VoxelShape COLLISION = box(1, 0, 1, 15, 16, 15);
    private final boolean iron;

    public BlockScaffold(Properties properties, boolean iron) {
        super(properties);
        this.iron = iron;
        registerDefaultState(defaultBlockState().setValue(STRENGTH, 0));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(STRENGTH); }
    public int standardStrength() { return iron ? 5 : 2; }
    public int reinforcedStrength() { return iron ? 12 : 5; }
    public boolean isReinforced(BlockState s) { return s.getValue(STRENGTH) == reinforcedStrength(); }

    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return COLLISION; }
    @Override protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) { return true; }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof Player player)) return;
        player.fallDistance = 0;
        var m = player.getDeltaMovement();
        double y = Math.max(m.y, -0.15);
        if ((Keyboard.isForwardKeyDown(player) || player.zza > 0) && y < 0.2) y = 0.2;
        player.setDeltaMovement(m.x, y, m.z);
    }

    public boolean isPillar(BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos p = pos.mutable();
        while (level.getBlockState(p).is(this)) p.move(Direction.DOWN);
        return level.getBlockState(p).isFaceSturdy(level, p, Direction.UP);
    }

    private int compare(BlockGetter level, BlockPos pos, int strength) {
        BlockState s = level.getBlockState(pos);
        int v = s.is(this) ? Math.min(s.getValue(STRENGTH), reinforcedStrength()) : 0;
        return Math.max(v, strength);
    }

    public int getStrengthFrom(BlockGetter level, BlockPos pos) {
        int strength = isPillar(level, pos.below()) ? standardStrength() + 1 : 0;
        strength = compare(level, pos.below(), strength);
        for (Direction d : Direction.Plane.HORIZONTAL) strength = compare(level, pos.relative(d), strength);
        return strength - 1;
    }

    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) { return getStrengthFrom(level, pos) >= 0; }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        int s = getStrengthFrom(ctx.getLevel(), ctx.getClickedPos());
        return s < 0 ? null : defaultBlockState().setValue(STRENGTH, s);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        level.scheduleTick(pos, this, 1);
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int own = state.getValue(STRENGTH);
        if (own >= reinforcedStrength()) {
            if (!isPillar(level, pos)) {
                own = getStrengthFrom(level, pos);
                popResource(level, pos, iron ? new ItemStack(Ic2Blocks.IRON_FENCE.get()) : new ItemStack(Items.STICK, 2));
            }
        } else own = getStrengthFrom(level, pos);
        if (own < 0) level.destroyBlock(pos, true);
        else if (own != state.getValue(STRENGTH)) level.setBlock(pos, state.setValue(STRENGTH, own), 3);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        boolean ok = iron ? stack.is(Ic2Blocks.IRON_FENCE.get().asItem()) : stack.is(Items.STICK) && stack.getCount() >= 2;
        if (!ok || isReinforced(state) || !isPillar(level, pos)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide) {
            if (!player.getAbilities().instabuild) stack.shrink(iron ? 1 : 2);
            level.setBlock(pos, state.setValue(STRENGTH, reinforcedStrength()), 3);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        ItemStack held = player.getMainHandItem();
        if (level.isClientSide || !held.is(asItem())) return;
        BlockPos.MutableBlockPos p = pos.mutable();
        while (level.getBlockState(p).is(this)) p.move(Direction.UP);
        if (p.getY() >= level.getMaxBuildHeight() || !level.getBlockState(p).canBeReplaced()) return;
        int s = getStrengthFrom(level, p);
        if (s < 0) return;
        level.setBlock(p, defaultBlockState().setValue(STRENGTH, s), 3);
        if (!player.getAbilities().instabuild) held.shrink(1);
    }

    @Override public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) { return iron ? 0 : 20; }
    @Override public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) { return iron ? 0 : 8; }
}
