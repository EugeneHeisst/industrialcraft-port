package ic2.core.block.explosive;
import ic2.core.ref.*;
import net.minecraft.core.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
public class BlockIndustrialTnt extends TntBlock {
    public BlockIndustrialTnt(Properties p){super(p);}
    public static EntityIC2Explosive spawn(Level l,BlockPos p,LivingEntity owner,boolean chain){if(l.isClientSide)return null;var e=new EntityIC2Explosive(Ic2Entities.IC2_EXPLOSIVE.get(),l);e.setPos(p.getX()+.5,p.getY(),p.getZ()+.5);e.setBlockState(Ic2Blocks.INDUSTRIAL_TNT.get().defaultBlockState());e.setFuse(chain?l.random.nextInt(15)+7:60);e.setIgniter(owner);double angle=l.random.nextDouble()*Math.PI*2;e.setDeltaMovement(-Math.sin(angle)*.02,.2,-Math.cos(angle)*.02);l.addFreshEntity(e);l.playSound(null,p,SoundEvents.TNT_PRIMED,SoundSource.BLOCKS,1,1);return e;}
    @Override public void onCaughtFire(BlockState s,Level l,BlockPos p,Direction side,LivingEntity owner){spawn(l,p,owner,false);}
    @Override public void wasExploded(Level l,BlockPos p,Explosion explosion){spawn(l,p,explosion.getIndirectSourceEntity(),true);}
    @Override public BlockState playerWillDestroy(Level l,BlockPos p,BlockState s,Player player){if(!l.isClientSide&&!player.isCreative()&&!s.getValue(UNSTABLE))spawn(l,p,player,false);return super.playerWillDestroy(l,p,s,player);}
}
