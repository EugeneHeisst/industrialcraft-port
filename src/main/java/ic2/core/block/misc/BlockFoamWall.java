package ic2.core.block.misc;

import ic2.api.event.PaintEvent;
import ic2.core.IC2;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/** Hardened construction foam wall in the sixteen dye colours (dye index, 7 = light gray); painters recolour it, it drops nothing. */
@EventBusSubscriber(modid = IC2.MODID)
public class BlockFoamWall extends Block {
    public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 15);
    public BlockFoamWall(Properties p) { super(p); registerDefaultState(defaultBlockState().setValue(COLOR, 7)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(COLOR); }
    @Override public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) { return new ItemStack(Ic2Blocks.CONSTRUCTION_FOAM.get()); }

    @SubscribeEvent
    static void paint(PaintEvent event) {
        BlockState s = event.level.getBlockState(event.pos);
        if (!s.is(Ic2Blocks.CONSTRUCTION_FOAM_WALL.get()) || s.getValue(COLOR) == event.color) return;
        if (!event.level.isClientSide) event.level.setBlock(event.pos, s.setValue(COLOR, event.color), 3);
        event.painted = true;
    }
}
