package ic2.core.block.misc;

import ic2.api.item.ItemWrapper;
import ic2.core.block.machine.tileentity.TileEntityMagnetizer;
import ic2.core.util.Keyboard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;

/** Iron fence; a free-standing pole charged by a magnetizer (MAGNET 1..15) lifts players, fast with metal boots. */
public class BlockPoleFence extends FenceBlock {
    public static final IntegerProperty MAGNET = IntegerProperty.create("magnet", 0, 15);

    public BlockPoleFence(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(MAGNET, 0));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { super.createBlockStateDefinition(builder); builder.add(MAGNET); }

    @Override public boolean connectsTo(BlockState state, boolean isSideSolid, Direction direction) { return state.getBlock() instanceof FenceBlock || super.connectsTo(state, isSideSolid, direction); }

    private static boolean connects(BlockGetter level, BlockPos pos, boolean pole) {
        BlockState s = level.getBlockState(pos);
        if (s.getBlock() instanceof FenceBlock) return true;
        return !pole && level.getBlockEntity(pos) instanceof TileEntityMagnetizer;
    }

    public static boolean isPole(BlockGetter level, BlockPos pos) {
        for (Direction d : Direction.Plane.HORIZONTAL) if (connects(level, pos.relative(d), true)) return false;
        return true;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof Player player) || !isPole(level, pos)) return;
        int magnet = state.getValue(MAGNET);
        Vec3 m = player.getDeltaMovement();
        double my = m.y;
        boolean descending = player.isShiftKeyDown();
        boolean slow = my >= -0.25 || my < 1.6;
        if (slow) player.fallDistance = 0;
        if (magnet == 0) {
            if (descending && !slow) my *= 0.9;
        } else {
            if (!level.isClientSide) level.setBlock(pos, state.setValue(MAGNET, magnet - 1), 2);
            if (descending) { if (!slow) my *= 0.8; }
            else {
                my += 0.075;
                if (my > 0) my *= 1.03;
                double max = Keyboard.isAltKeyDown(player) ? 0.1 : hasMetalShoes(player) ? 1.5 : 0.5;
                my = Math.min(my, max);
            }
        }
        player.setDeltaMovement(m.x, my, m.z);
    }

    public static boolean hasMetalShoes(Player player) {
        ItemStack shoes = player.getItemBySlot(EquipmentSlot.FEET);
        if (shoes.isEmpty()) return false;
        return shoes.is(Items.IRON_BOOTS) || shoes.is(Items.GOLDEN_BOOTS) || shoes.is(Items.CHAINMAIL_BOOTS) || shoes.is(Items.NETHERITE_BOOTS) || ItemWrapper.isMetalArmor(shoes, player);
    }
}
