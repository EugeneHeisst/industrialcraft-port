package ic2.core.audit;
import ic2.api.event.ExplosionEvent;
import ic2.core.*;
import ic2.core.block.explosive.*;
import ic2.core.block.machine.tileentity.TileEntityNuke;
import ic2.core.block.machine.container.ContainerNuke;
import ic2.core.ref.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
public final class ExplosivesAudit {
    private static TileEntityNuke bomb(ServerLevel l,BlockPos p,int tnt,String fuel,int amount){l.setBlock(p,Ic2Blocks.NUKE.get().defaultBlockState(),3);var n=(TileEntityNuke)l.getBlockEntity(p);if(tnt>0)n.outsideSlot.put(new ItemStack(Ic2Blocks.INDUSTRIAL_TNT.get(),tnt));if(fuel!=null)n.insideSlot.put(new ItemStack(Ic2Items.get(fuel),amount));return n;}
    public static void run(ServerLevel l,BlockPos p,CropAudit.Check check){
        for(var pos:BlockPos.betweenClosed(p.offset(-2,-1,-2),p.offset(8,3,4)))l.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
        var n=bomb(l,p,0,"uranium_235",4);check.test("unloaded nuke cannot ignite without industrial TNT",n.getNukeExplosivePower()<0&&BlockNuke.prime(l,p,null,false)==null&&l.getBlockEntity(p)==n,"empty outer charge");
        n.outsideSlot.put(new ItemStack(Ic2Blocks.INDUSTRIAL_TNT.get(),8));n.insideSlot.clear();check.test("nuke conventional charge original cube root formula",n.getNukeExplosivePower()==10&&n.getRadiationRange()==0,n.getNukeExplosivePower());
        n.insideSlot.put(new ItemStack(Ic2Items.get("uranium_238"),8));check.test("nuke U238 contributes radiation only",n.getNukeExplosivePower()==10&&n.getRadiationRange()==8,n.getRadiationRange());n.insideSlot.put(new ItemStack(Ic2Blocks.URANIUM_BLOCK.get(),4));check.test("nuke uranium blocks radiation multiplier",n.getNukeExplosivePower()==10&&n.getRadiationRange()==48,n.getRadiationRange());
        n.outsideSlot.put(new ItemStack(Ic2Blocks.INDUSTRIAL_TNT.get(),31));n.insideSlot.put(new ItemStack(Ic2Items.get("uranium_235"),4));check.test("nuke U235 enrichment ignition threshold",Math.abs(n.getNukeExplosivePower()-5*Math.cbrt(31))<.001&&n.getRadiationRange()==62,n.getNukeExplosivePower());
        n.outsideSlot.get().setCount(32);check.test("nuke U235 enrichment adds original yield",Math.abs(n.getNukeExplosivePower()-(5*Math.cbrt(32)+.5*Math.pow(4,1.4)))<.001,n.getNukeExplosivePower());
        n.outsideSlot.get().setCount(64);n.insideSlot.put(new ItemStack(Ic2Items.get("small_uranium_235"),9));check.test("nuke small U235 original yield",Math.abs(n.getNukeExplosivePower()-(20+Math.pow(9,1.6)/18))<.001&&n.getRadiationRange()==128,n.getNukeExplosivePower());
        n.outsideSlot.get().setCount(32);n.insideSlot.put(new ItemStack(Ic2Items.get("small_plutonium"),9));check.test("nuke small plutonium original yield",Math.abs(n.getNukeExplosivePower()-(5*Math.cbrt(32)+4.5))<.001&&n.getRadiationRange()==96,n.getNukeExplosivePower());
        n.outsideSlot.get().setCount(64);n.insideSlot.put(new ItemStack(Ic2Items.get("plutonium"),64));check.test("nuke original power limit and plutonium radiation",n.getNukeExplosivePower()==60&&n.getRadiationRange()==256,n.getNukeExplosivePower());
        double oldLimit=NukeConfig.POWER_LIMIT.get();try{NukeConfig.POWER_LIMIT.set(10d);check.test("nuke configurable power cap",n.getNukeExplosivePower()==10,n.getNukeExplosivePower());}finally{NukeConfig.POWER_LIMIT.set(oldLimit);}
        var saved=(TileEntityNuke)BlockEntity.loadStatic(p,n.getBlockState(),n.saveWithFullMetadata(l.registryAccess()),l.registryAccess());check.test("nuke casing payload persistence",saved.outsideSlot.get().getCount()==64&&saved.insideSlot.get().getCount()==64&&saved.getNukeExplosivePower()==60,saved.getNukeExplosivePower());
        boolean enabled=NukeConfig.ENABLE_NUKE.get();try{NukeConfig.ENABLE_NUKE.set(false);check.test("nuke disabled config preserves casing and fuel",BlockNuke.prime(l,p,null,false)==null&&l.getBlockEntity(p)==n&&n.insideSlot.get().getCount()==64,"disabled");}finally{NukeConfig.ENABLE_NUKE.set(enabled);}
        var actor=FakePlayerFactory.getMinecraft(l);actor.getInventory().clearContent();var menu=new ContainerNuke(200,actor.getInventory(),n);menu.quickMoveStack(actor,37);for(int i=38;i<45;i++)menu.quickMoveStack(actor,i);int total=0;for(var s:actor.getInventory().items)if(s.is(Ic2Blocks.INDUSTRIAL_TNT.get().asItem()))total+=s.getCount();check.test("nuke eight mirrored charge slots cannot duplicate items",total==64&&n.outsideSlot.isEmpty(),total);actor.getInventory().clearContent();
        n.outsideSlot.put(new ItemStack(Ic2Blocks.INDUSTRIAL_TNT.get(),32));n.insideSlot.put(new ItemStack(Ic2Items.get("uranium_235"),4));float power=n.getNukeExplosivePower();int rad=n.getRadiationRange();var primed=BlockNuke.prime(l,p,actor,false);
        check.test("nuke ignition captures charge and consumes casing once",primed!=null&&primed.getFuse()==300&&primed.explosivePower==power&&primed.radiationRange==rad&&l.getBlockState(p).isAir()&&n.outsideSlot.isEmpty()&&n.insideSlot.isEmpty()&&l.getEntitiesOfClass(ItemEntity.class,new AABB(p).inflate(2)).isEmpty(),primed==null?"null":primed.getFuse());
        var tag=new CompoundTag();primed.saveWithoutId(tag);var restored=new EntityIC2Explosive(Ic2Entities.IC2_EXPLOSIVE.get(),l);restored.load(tag);check.test("primed nuke fuse power radiation and texture persist",restored.getFuse()==300&&restored.explosivePower==power&&restored.radiationRange==rad&&restored.dropRate==.05f&&restored.getBlockState().is(Ic2Blocks.NUKE.get()),restored.getFuse());restored.discard();
        int[] events={0};double[] seen={0,0};java.util.function.Consumer<ExplosionEvent> listener=e->{if(e.entity instanceof EntityIC2Explosive){events[0]++;seen[0]=e.power;seen[1]=e.radiationRange;e.setCanceled(true);}};NeoForge.EVENT_BUS.addListener(listener);
        try{primed.tick();check.test("nuke fuse ticks without early explosion",primed.getFuse()==299&&!primed.isRemoved()&&events[0]==0,primed.getFuse());primed.setFuse(1);primed.tick();check.test("nuke expiration posts cancellable nuclear explosion",primed.isRemoved()&&events[0]==1&&seen[0]==power&&seen[1]==rad,seen[0]);
            n=bomb(l,p,8,null,0);var defuse=BlockNuke.prime(l,p,null,false);actor.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Ic2Items.WRENCH.get()));defuse.interact(actor,InteractionHand.MAIN_HAND);check.test("primed nuke wrench defuses without recovering charge",defuse.isRemoved()&&actor.getMainHandItem().getDamageValue()==1&&events[0]==1,"defused");actor.getInventory().clearContent();
            n=bomb(l,p,8,null,0);l.setBlock(p.south(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);var live=l.getEntitiesOfClass(EntityIC2Explosive.class,new AABB(p).inflate(2));check.test("nuke redstone ignition uses real primed entity",l.getBlockState(p).isAir()&&live.size()==1&&live.getFirst().getFuse()==300,live.size());for(var e:live)e.discard();l.removeBlock(p.south(),false);
            l.setBlock(p,Ic2Blocks.INDUSTRIAL_TNT.get().defaultBlockState(),3);l.setBlock(p.south(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);live=l.getEntitiesOfClass(EntityIC2Explosive.class,new AABB(p).inflate(2));check.test("industrial TNT redstone original fuse and drop rate",l.getBlockState(p).isAir()&&live.size()==1&&live.getFirst().getFuse()==60&&live.getFirst().dropRate==.9f&&live.getFirst().explosivePower==5.5f,live.size());for(var e:live)e.discard();l.removeBlock(p.south(),false);
            var chain=BlockIndustrialTnt.spawn(l,p,null,true);check.test("industrial TNT explosion chain shortened fuse",chain.getFuse()>=7&&chain.getFuse()<=21,chain.getFuse());chain.discard();
            n=bomb(l,p,16,"plutonium",4);var chained=BlockNuke.prime(l,p,null,true);check.test("nuke chain preserves payload with shortened fuse",chained!=null&&chained.getFuse()>=37&&chained.getFuse()<=111&&chained.radiationRange==64&&n.insideSlot.isEmpty(),chained==null?"null":chained.getFuse());if(chained!=null)chained.discard();
        }finally{NeoForge.EVENT_BUS.unregister(listener);}
        // Small actual ITNT blast, isolated from the main audit stand.
        var blast=p.east(20);for(var pos:BlockPos.betweenClosed(blast.offset(-2,-2,-2),blast.offset(2,2,2)))l.setBlock(pos,Blocks.STONE.defaultBlockState(),3);var itnt=BlockIndustrialTnt.spawn(l,blast,null,false);itnt.setFuse(1);itnt.tick();int removed=0;for(var pos:BlockPos.betweenClosed(blast.offset(-2,-2,-2),blast.offset(2,2,2)))if(l.getBlockState(pos).isAir())removed++;check.test("industrial TNT detonates through actual IC2 blast physics",itnt.isRemoved()&&removed>0,removed);
        var r=p.east(40);var pig=EntityType.PIG.create(l);pig.setPos(r.getX()+2,r.getY(),r.getZ());l.addFreshEntity(pig);var protectedPig=EntityType.PIG.create(l);protectedPig.setPos(r.getX()+4,r.getY(),r.getZ());protectedPig.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Ic2Items.HAZMAT_HELMET.get()));protectedPig.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Ic2Items.HAZMAT_CHESTPLATE.get()));protectedPig.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Ic2Items.HAZMAT_LEGGINGS.get()));protectedPig.setItemSlot(EquipmentSlot.FEET,new ItemStack(Ic2Items.RUBBER_BOOTS.get()));l.addFreshEntity(protectedPig);
        new ExplosionIC2(l,null,r.getX(),r.getY(),r.getZ(),.01f,0,ExplosionIC2.Type.Nuclear,null,30).doExplosion();check.test("nuclear radiation reaches mobs and respects full hazmat",pig.hasEffect(Ic2Effects.RADIATION)&&!protectedPig.hasEffect(Ic2Effects.RADIATION),"radiation/hazmat");pig.discard();protectedPig.discard();
        check.test("industrial TNT breaking cannot return an intact charge",Block.getDrops(Ic2Blocks.INDUSTRIAL_TNT.get().defaultBlockState(),l,p,null).isEmpty(),"no duplicate explosive");
        boolean recipes=true;for(var item:new Item[]{Ic2Blocks.INDUSTRIAL_TNT.get().asItem(),Ic2Blocks.NUKE.get().asItem()})recipes&=l.getRecipeManager().getRecipes().stream().anyMatch(recipe->recipe.value().getResultItem(l.registryAccess()).is(item));check.test("industrial TNT and nuke original crafting recipes",recipes,"recipes");
    }
}
