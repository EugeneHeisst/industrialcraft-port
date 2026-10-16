package ic2.core.crop;

import ic2.api.crops.ICropTile;
import ic2.api.item.ItemWrapper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.sounds.*;

public class CropEating extends Ic2CropCard {
    public String discoveredBy(){return "Hasudako";}
    public String name(){return "eatingplant";}
    public int tier(){return 6;}
    public int stat(int n){return n>=0&&n<5?new int[]{1,1,3,1,4}[n]:0;}
    public String[] attributes(){return new String[]{"Bad","Food"};}
    public int maxSize(){return 6;}
    public boolean canGrow(ICropTile c){return c.getLightLevel()>10 && (c.getSize()<3 || c.getSize()<6&&c.isBlockBelow(Blocks.LAVA));}
    public int getOptimalHavestSize(ICropTile c){return 4;}
    public boolean canBeHarvested(ICropTile c){return c.getSize()>=4&&c.getSize()<6;}
    public ItemStack getGain(ICropTile c){return canBeHarvested(c)?new ItemStack(Blocks.CACTUS):ItemStack.EMPTY;}
    public int getrootslength(ICropTile c){return 5;}
    public int growthDuration(ICropTile c){
        var b=c.getWorld().getBiome(c.getLocation());
        float f=b.is(net.minecraft.tags.BiomeTags.IS_MOUNTAIN)||b.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME,net.minecraft.resources.ResourceLocation.parse("c:is_swamp")))?1/1.5f:1;
        return (int)(super.growthDuration(c)*f/(1+c.getAirQuality()/10f));
    }
    public void tick(ICropTile c){
        if(c.getSize()==1)return;
        var l=c.getWorld();var p=c.getLocation();
        if(c.getCustomData().getBoolean("eaten")){((TileEntityCrop)c).drop(new ItemStack(Items.ROTTEN_FLESH));c.getCustomData().putBoolean("eaten",false);}
        var targets=l.getEntitiesOfClass(LivingEntity.class,new AABB(p.getX()-0.5,p.getY(),p.getZ()-0.5,p.getX()+1.5,p.getY()+2,p.getZ()+1.5),e->e.isAlive()&&!(e instanceof Player pl&&(pl.isCreative()||pl.isSpectator())));
        if(targets.isEmpty())return;
        var e=targets.get(l.random.nextInt(targets.size()));var v=e.getDeltaMovement();e.setDeltaMovement((p.getX()+0.5-e.getX())*0.5,Math.min(-0.05,v.y),(p.getZ()+0.5-e.getZ())*0.5);
        e.hurt(l.damageSources().cactus(),c.getSize()*2);
        boolean metal=false;if(e instanceof Player pl)for(var a:e.getArmorSlots())if(ItemWrapper.isMetalArmor(a,pl))metal=true;
        if(!metal){e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,64,50));e.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,64));e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,64));}
        if(canGrow(c))((TileEntityCrop)c).growthPoints+=(int)(((TileEntityCrop)c).calcGrowthRate()*0.5);
        l.playSound(null,p,SoundEvents.PLAYER_BURP,SoundSource.BLOCKS,1,l.random.nextFloat()*0.1f+0.9f);c.getCustomData().putBoolean("eaten",true);
    }
}
