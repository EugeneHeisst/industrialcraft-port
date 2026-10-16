package ic2.core.block.explosive;
import ic2.core.*;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.machine.tileentity.TileEntityNuke;
import ic2.core.ref.*;
import net.minecraft.core.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public class BlockNuke extends BlockTileEntity {
    public BlockNuke(Properties p){super(p,()->Ic2BlockEntities.NUKE.get());}
    public static EntityIC2Explosive prime(Level l,BlockPos p,LivingEntity owner,boolean chain){
        if(l.isClientSide||!NukeConfig.ENABLE_NUKE.get()||!(l.getBlockEntity(p) instanceof TileEntityNuke n))return null;
        float power=n.getNukeExplosivePower();if(power<0)return null;
        var e=new EntityIC2Explosive(Ic2Entities.IC2_EXPLOSIVE.get(),l);e.setPos(p.getX()+.5,p.getY(),p.getZ()+.5);e.setBlockState(Ic2Blocks.NUKE.get().defaultBlockState());e.setFuse(chain?l.random.nextInt(75)+37:300);e.explosivePower=power;e.dropRate=.05f;e.radiationRange=n.getRadiationRange();e.setIgniter(owner);e.setDeltaMovement(0,.2,0);
        if(!l.addFreshEntity(e))return null;n.onIgnite();l.removeBlock(p,false);l.playSound(null,p,SoundEvents.TNT_PRIMED,SoundSource.BLOCKS,1,1);IC2.LOG.info("Nuke at {} ignited by {} (power {}, radiation {}).",p,owner==null?"redstone/explosion":owner.getUUID(),power,e.radiationRange);return e;
    }
    @Override public void onCaughtFire(BlockState s,Level l,BlockPos p,Direction side,LivingEntity owner){prime(l,p,owner,false);}
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){if(stack.is(Items.FLINT_AND_STEEL)||stack.is(Items.FIRE_CHARGE)){if(l.isClientSide)return ItemInteractionResult.SUCCESS;if(prime(l,p,player,false)==null)return ItemInteractionResult.FAIL;if(stack.is(Items.FLINT_AND_STEEL))stack.hurtAndBreak(1,player,LivingEntity.getSlotForHand(hand));else stack.consume(1,player);return ItemInteractionResult.SUCCESS;}return super.useItemOn(stack,s,l,p,player,hand,hit);}
    @Override public void onBlockExploded(BlockState s,Level l,BlockPos p,Explosion explosion){if(prime(l,p,explosion.getIndirectSourceEntity(),true)==null)super.onBlockExploded(s,l,p,explosion);}
    @Override public boolean dropFromExplosion(Explosion e){return false;}
}
