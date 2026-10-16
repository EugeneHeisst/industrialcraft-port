package ic2.core.block.explosive;
import ic2.core.ExplosionIC2;
import ic2.core.item.tool.ItemToolWrench;
import ic2.api.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.UUID;
/** Modern TNT motion/render synchronization, with IC2 blast physics and persistent nuke payload. */
public class EntityIC2Explosive extends PrimedTnt {
    public float explosivePower=5.5f,dropRate=.9f;
    public int radiationRange;
    private UUID igniter;
    public EntityIC2Explosive(EntityType<? extends EntityIC2Explosive> type,Level level){super(type,level);}
    public void setIgniter(LivingEntity owner){igniter=owner==null?null:owner.getUUID();}
    @Override public LivingEntity getOwner(){if(igniter==null)return null;if(level() instanceof net.minecraft.server.level.ServerLevel l&&l.getEntity(igniter) instanceof LivingEntity owner)return owner;return null;}
    @Override protected void explode(){new ExplosionIC2(level(),this,getX(),getY()+.5,getZ(),explosivePower,dropRate,radiationRange>0?ExplosionIC2.Type.Nuclear:ExplosionIC2.Type.Normal,getOwner(),radiationRange).doExplosion();}
    @Override protected void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putFloat("ic2Power",explosivePower);n.putFloat("ic2DropRate",dropRate);n.putInt("ic2Radiation",radiationRange);if(igniter!=null)n.putUUID("ic2Igniter",igniter);}
    @Override protected void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);explosivePower=n.contains("ic2Power")?Math.max(0,n.getFloat("ic2Power")):5.5f;dropRate=n.contains("ic2DropRate")?Math.max(0,Math.min(1,n.getFloat("ic2DropRate"))):.9f;radiationRange=Math.max(0,n.getInt("ic2Radiation"));igniter=n.hasUUID("ic2Igniter")?n.getUUID("ic2Igniter"):null;}
    @Override public InteractionResult interact(Player p,InteractionHand hand){ItemStack s=p.getItemInHand(hand);if(getBlockState().is(ic2.core.ref.Ic2Blocks.NUKE.get())&&s.getItem() instanceof ItemToolWrench wrench&&wrench.canTakeDamage(s,1)){if(!level().isClientSide){if(s.getItem() instanceof IElectricItem)ElectricItem.manager.use(s,50,p);else s.hurtAndBreak(1,p,LivingEntity.getSlotForHand(hand));discard();}return InteractionResult.sidedSuccess(level().isClientSide);}return super.interact(p,hand);}
}
