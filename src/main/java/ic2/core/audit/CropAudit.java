package ic2.core.audit;

import ic2.api.crops.*;
import ic2.api.item.ElectricItem;
import ic2.core.crop.*;
import ic2.core.item.*;
import ic2.core.ref.*;
import ic2.core.block.machine.tileentity.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import java.util.*;

/** Gameplay checks on a real server world. Called from the main server audit before its summary. */
public final class CropAudit {
    @FunctionalInterface public interface Check {void test(String name,boolean ok,Object detail);}
    private static TileEntityCrop place(ServerLevel l,BlockPos p){
        l.setBlock(p.below(),Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE,7),3);
        l.setBlock(p,Ic2Blocks.CROP.get().defaultBlockState(),3);
        return (TileEntityCrop)l.getBlockEntity(p);
    }
    public static void run(ServerLevel l,BlockPos p,Check check){
        for(int x=-2;x<=14;x++)for(int z=-2;z<=5;z++)for(int y=-6;y<=4;y++)l.setBlock(p.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);
        l.setDayTime(6000);
        check.test("crop registry",Crops.instance.getCrops().size()==29,"cards="+Crops.instance.getCrops().size());
        var wheat=place(l,p);
        l.setBlock(p.below(),Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE,0),3);
        l.getBlockState(p.below()).randomTick(l,p.below(),l.random);
        check.test("crop maintains dry farmland",l.getBlockState(p.below()).is(Blocks.FARMLAND),l.getBlockState(p.below()));
        var base=Crops.instance.getBaseSeed(new ItemStack(Items.WHEAT_SEEDS));
        boolean planted=wheat.tryPlantIn(base.crop,base.size,base.statGrowth,base.statGain,base.statResistance,0);
        wheat.nutrientStorage=100;wheat.waterStorage=200;
        for(int i=0;i<500 && wheat.size<7;i++)wheat.cropTick();
        check.test("crop planted and grew to maturity",planted&&wheat.size==7,"size="+wheat.size);
        var drop=wheat.harvest_automated(true);
        check.test("crop harvest and regrowth",drop!=null&&Arrays.stream(drop).allMatch(s->s.isEmpty()||s.is(Items.WHEAT))&&wheat.size==2,"drops="+Arrays.toString(drop)+" size="+wheat.size);
        var seed=ItemCropSeed.create(wheat.getCrop(),12,17,9,4);
        var encoded=seed.save(l.registryAccess());var decoded=ItemStack.parseOptional(l.registryAccess(),(CompoundTag)encoded);
        check.test("crop seed item serialization",ItemStack.isSameItemSameComponents(seed,decoded)&&Crops.instance.getCropCard(decoded)==wheat.getCrop(),decoded);
        wheat.statGrowth=12;wheat.statGain=17;wheat.statResistance=9;wheat.scanLevel=4;wheat.getCustomData().putInt("addon",42);
        var tag=wheat.saveWithFullMetadata(l.registryAccess());var reloaded=(TileEntityCrop)net.minecraft.world.level.block.entity.BlockEntity.loadStatic(p,wheat.getBlockState(),tag,l.registryAccess());
        check.test("crop save reload",reloaded!=null&&reloaded.getCrop()==wheat.getCrop()&&reloaded.statGain==17&&reloaded.scanLevel==4&&reloaded.getCustomData().getInt("addon")==42,"growth="+(reloaded==null?-1:reloaded.statGrowth));
        var metal=place(l,p.east(3));metal.tryPlantIn(Crops.instance.getCropCard("IC2","ferru"),3,1,1,1,0);
        boolean blocked=!metal.getCrop().canGrow(metal);l.setBlock(metal.getBlockPos().below(2),Blocks.DIRT.defaultBlockState(),3);l.setBlock(metal.getBlockPos().below(3),Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(),3);
        check.test("metal crop deepslate root tag",blocked&&metal.getCrop().canGrow(metal),metal.getCrop().name());
        for(int i=0;i<700&&metal.size<4;i++){metal.nutrientStorage=100;metal.waterStorage=200;metal.cropTick();}
        check.test("metal crop matures with ore",metal.size==4,"size="+metal.size);
        var mushroom=place(l,p.east(5));mushroom.tryPlantIn(Crops.instance.getCropCard("IC2","redMushroom"),1,1,1,1,0);
        boolean dry=mushroom.getCrop()==null; // dry mushrooms cannot be planted until hydrated
        mushroom.waterStorage=200;boolean wet=mushroom.tryPlantIn(Crops.instance.getCropCard("IC2","redMushroom"),1,1,1,1,0);
        check.test("mushroom hydration condition",dry&&wet,mushroom.getHydrationStorage());
        var cross=place(l,p.south(3));cross.upgraded=true;
        for(var dir:new Direction[]{Direction.EAST,Direction.WEST,Direction.NORTH,Direction.SOUTH}){
            var parent=place(l,cross.getBlockPos().relative(dir));parent.tryPlantIn(IC2Crops.cropWheat,7,10,12,8,4);
        }
        boolean crossed=false;for(int i=0;i<2000&&!crossed;i++)crossed=cross.attemptCrossing();cross.updateState();
        check.test("crop crossing inherits bounded stats",crossed&&cross.getCrop()!=null&&!cross.upgraded&&cross.statGrowth>=6&&cross.statGrowth<=14&&cross.scanLevel==0,"crop="+(cross.getCrop()==null?"none":cross.getCrop().name())+" stats="+cross.statGrowth+"/"+cross.statGain+"/"+cross.statResistance);
        cross.exStorage=3;check.test("weed ex consumes without negative storage",cross.hasEx()&&cross.exStorage==0,cross.exStorage);
        var mp=p.east(8);l.setBlock(mp,Ic2Blocks.CROPMATRON.get().defaultBlockState(),3);
        var matron=(TileEntityCropmatron)l.getBlockEntity(mp);var cared=place(l,mp.east());
        matron.energy=1000;matron.fertilizerSlot.put(0,new ItemStack(Ic2Items.get("fertilizer")));matron.weedExSlot.put(0,new ItemStack(Ic2Items.WEED_EX.get()));
        matron.fill(new FluidStack(Fluids.WATER,1000),FluidAction.EXECUTE);matron.scanX=0;matron.scanY=0;matron.scanZ=0;matron.scan();
        check.test("cropmatron fertilizer water weed ex",cared.nutrientStorage==90&&cared.waterStorage==200&&cared.exStorage==50&&matron.energy==969,"stores="+cared.nutrientStorage+"/"+cared.waterStorage+"/"+cared.exStorage+" EU="+matron.energy);
        var hp=p.east(12);l.setBlock(hp,Ic2Blocks.CROP_HARVESTER.get().defaultBlockState(),3);var h=(TileEntityCropHarvester)l.getBlockEntity(hp);var grown=place(l,hp.east());
        grown.tryPlantIn(IC2Crops.cropWheat,7,1,31,1,0);
        // A mature plant can legitimately yield zero items; retry complete harvests instead of asserting one random roll.
        for(int attempt=0;attempt<20&&h.contentSlot.isEmpty();attempt++){grown.setSize((byte)7);h.energy=10000;h.scanX=0;h.scanY=0;h.scanZ=0;h.scan();}
        check.test("crop harvester collects and regrows",grown.size==2&&!h.contentSlot.isEmpty(),"size="+grown.size+" output="+h.contentSlot);
        var profile=new com.mojang.authlib.GameProfile(UUID.randomUUID(),"CropAudit");
        var player=new ServerPlayer(l.getServer(),l,profile,net.minecraft.server.level.ClientInformation.createDefault());
        player.setItemInHand(InteractionHand.MAIN_HAND,Ic2Items.CROP_ANALYZER.get().charged());
        var menu=(ItemCropAnalyzer.Container)Ic2Items.CROP_ANALYZER.get().createContainer(99,player.getInventory(),player.getMainHandItem(),InteractionHand.MAIN_HAND);
        var sample=ItemCropSeed.create(IC2Crops.cropWheat,12,17,9,0);double before=ElectricItem.manager.getCharge(player.getMainHandItem());
        for(int stage=1;stage<=4;stage++){menu.base.setItem(0,sample);menu.broadcastChanges();sample=menu.base.removeItem(1,1);}
        check.test("crop analyzer four stages and EU cost",sample.getOrDefault(Ic2Components.CROP_DATA.get(),new CompoundTag()).getByte("scan")==4&&ElectricItem.manager.getCharge(player.getMainHandItem())==before-10000,"EU="+ElectricItem.manager.getCharge(player.getMainHandItem()));
        menu.base.setItem(0,sample);menu.broadcastChanges();
        var stored=player.getMainHandItem().copy();
        player.setItemInHand(InteractionHand.MAIN_HAND,stored);
        var reopened=(ItemCropAnalyzer.Container)Ic2Items.CROP_ANALYZER.get().createContainer(100,player.getInventory(),stored,InteractionHand.MAIN_HAND);
        check.test("crop analyzer inventory persists on reopen",reopened.base.getItem(1).getOrDefault(Ic2Components.CROP_DATA.get(),new CompoundTag()).getByte("scan")==4,reopened.base.getItem(1));
        var addon=new CropWheat(){@Override public String owner(){return "audit_addon";}};
        boolean registered=Crops.instance.registerCrop(addon)>=0;
        var external=place(l,p.offset(6,0,4));external.tryPlantIn(addon,7,5,6,7,4);
        var externalSeed=external.generateSeeds(addon,(byte)5,(byte)6,(byte)7,(byte)4);
        check.test("crop addon API and independent namespace",registered&&Crops.instance.getCropCard("audit_addon","wheat")==addon&&Crops.instance.getCropCard(externalSeed)==addon&&external.getBlockState().getValue(BlockCrop.PLANT)==BlockCrop.Plant.EMPTY,externalSeed);
        var patient=net.minecraft.world.entity.EntityType.COW.create(l);
        patient.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON,1200));
        patient.addEffect(new net.minecraft.world.effect.MobEffectInstance(Ic2Effects.RADIATION,1200));
        Ic2Items.TERRA_WART.get().finishUsingItem(new ItemStack(Ic2Items.TERRA_WART.get()),l,patient);
        check.test("terra wart cures poison and reduces radiation",!patient.hasEffect(net.minecraft.world.effect.MobEffects.POISON)&&patient.getEffect(Ic2Effects.RADIATION)!=null&&patient.getEffect(Ic2Effects.RADIATION).getDuration()==600,patient.getActiveEffects());
        check.test("crop recipes present",l.getRecipeManager().getRecipes().stream().anyMatch(r->r.value().getResultItem(l.registryAccess()).is(Ic2Blocks.CROP.get().asItem()))&&l.getRecipeManager().getRecipes().stream().anyMatch(r->r.value().getResultItem(l.registryAccess()).is(Ic2Items.CROP_ANALYZER.get())),"sticks + analyzer");
    }
}
