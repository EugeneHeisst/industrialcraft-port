package ic2.core.block.wiring;
import ic2.core.block.BlockTileEntity;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
public class BlockLuminator extends BlockTileEntity {
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty FULL_COVER=net.minecraft.world.level.block.state.properties.BooleanProperty.create("full_cover");
    @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(FULL_COVER);}
    public static boolean special(BlockState s){return s.getBlock() instanceof BlockCable||s.getBlock() instanceof FenceBlock||s.getBlock() instanceof net.minecraft.world.level.block.TransparentBlock;}
    public BlockLuminator(Properties p){super(p,()->Ic2BlockEntities.LUMINATOR.get());}
    public static boolean supports(BlockState s,BlockGetter l,BlockPos p,Direction face){return s.isFaceSturdy(l,p,face)||s.getBlock() instanceof BlockCable||s.getBlock() instanceof FenceBlock||s.getBlock() instanceof net.minecraft.world.level.block.TransparentBlock;}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){Direction d=c.getClickedFace();var s=defaultBlockState().setValue(FACING,d).setValue(FULL_COVER,special(c.getLevel().getBlockState(c.getClickedPos().relative(d.getOpposite()))));return s.canSurvive(c.getLevel(),c.getClickedPos())?s:null;}
    @Override protected boolean canSurvive(BlockState s,LevelReader l,BlockPos p){var d=s.getValue(FACING);var support=p.relative(d.getOpposite());return supports(l.getBlockState(support),l,support,d);}
    @Override protected void neighborChanged(BlockState s,Level l,BlockPos p,Block block,BlockPos from,boolean moving){super.neighborChanged(s,l,p,block,from,moving);if(!s.canSurvive(l,p))l.removeBlock(p,false);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){if(!s.getValue(FULL_COVER)&&s.getValue(FACING)!=Direction.UP)return switch(s.getValue(FACING)){case DOWN->box(4,13,4,12,16,12);case NORTH->box(6,3,14,10,13,16);case SOUTH->box(6,3,0,10,13,2);case WEST->box(14,3,6,16,13,10);case EAST->box(0,3,6,2,13,10);default->box(0,0,0,16,1,16);};return switch(s.getValue(FACING)){case UP->box(0,0,0,16,1,16);case DOWN->box(0,15,0,16,16,16);case NORTH->box(0,0,15,16,16,16);case SOUTH->box(0,0,0,16,16,1);case WEST->box(15,0,0,16,16,16);case EAST->box(0,0,0,1,16,16);};}
}
