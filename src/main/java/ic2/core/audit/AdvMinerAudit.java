package ic2.core.audit;
import ic2.api.item.ElectricItem;
import ic2.core.block.machine.tileentity.TileEntityAdvMiner;
import ic2.core.ref.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
public final class AdvMinerAudit {
    private static int count(ChestBlockEntity c,Item item){int n=0;for(int i=0;i<c.getContainerSize();i++)if(c.getItem(i).is(item))n+=c.getItem(i).getCount();return n;}
    public static void run(ServerLevel l,BlockPos p,CropAudit.Check check){
        for(var pos:BlockPos.betweenClosed(p.offset(-18,-2,-18),p.offset(18,2,18)))l.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
        l.setBlock(p,Ic2Blocks.ADV_MINER.get().defaultBlockState(),3);var m=(TileEntityAdvMiner)l.getBlockEntity(p);m.onLoaded();m.energy=100000;
        l.setBlock(p.east(),Blocks.CHEST.defaultBlockState(),3);var chest=(ChestBlockEntity)l.getBlockEntity(p.east());
        var scanner=Ic2Items.OD_SCANNER.get().charged();m.scannerSlot.put(scanner);m.xcounter=m.zcounter=0;m.ticker=19;var target=p.below();l.setBlock(target,Blocks.IRON_ORE.defaultBlockState(),3);double charge=ElectricItem.manager.getCharge(scanner);m.work();
        check.test("advanced miner scans mines and inserts actual loot",l.getBlockState(target).isAir()&&count(chest,Items.RAW_IRON)==1&&m.energy==99488&&ElectricItem.manager.getCharge(scanner)==charge-64,"EU="+m.energy+" loot="+count(chest,Items.RAW_IRON));
        m.ListSlot.put(new ItemStack(Items.COBBLESTONE));l.setBlock(target,Blocks.STONE.defaultBlockState(),3);double energy=m.energy;check.test("advanced miner blacklist checks drops",!m.mine(target)&&l.getBlockState(target).is(Blocks.STONE)&&m.energy==energy,m.energy);
        m.blacklist=false;check.test("advanced miner whitelist permits matching loot",m.mine(target)&&count(chest,Items.COBBLESTONE)==1,count(chest,Items.COBBLESTONE));
        l.setBlock(target,Blocks.IRON_ORE.defaultBlockState(),3);energy=m.energy;check.test("advanced miner whitelist rejects other drops",!m.mine(target)&&m.energy==energy,m.energy);
        m.blacklist=true;m.ListSlot.clear();m.silktouch=true;check.test("advanced miner Silk Touch follows modern loot tables",m.mine(target)&&count(chest,Items.IRON_ORE)==1&&count(chest,Items.RAW_IRON)==1,count(chest,Items.IRON_ORE));
        m.silktouch=false;l.setBlock(target,Blocks.BEDROCK.defaultBlockState(),3);energy=m.energy;check.test("advanced miner cannot mine bedrock",!m.mine(target)&&m.energy==energy,m.energy);
        l.setBlock(target,Blocks.WATER.defaultBlockState(),3);check.test("advanced miner leaves fluids",!m.mine(target)&&l.getBlockState(target).is(Blocks.WATER),l.getBlockState(target));
        l.setBlock(target,Blocks.CHEST.defaultBlockState(),3);((ChestBlockEntity)l.getBlockEntity(target)).setItem(0,new ItemStack(Items.DIAMOND,7));check.test("advanced miner preserves block entity inventories",!m.mine(target)&&((ChestBlockEntity)l.getBlockEntity(target)).getItem(0).getCount()==7,l.getBlockState(target));
        l.setBlock(target,Blocks.IRON_ORE.defaultBlockState(),3);boolean[] event={false};java.util.function.Consumer<BlockEvent.BreakEvent> listener=e->{if(e.getPos().equals(target)){event[0]=true;e.setCanceled(true);}};NeoForge.EVENT_BUS.addListener(listener);energy=m.energy;int loot=count(chest,Items.RAW_IRON);
        try{check.test("advanced miner respects cancelled protection events",!m.mine(target)&&event[0]&&l.getBlockState(target).is(Blocks.IRON_ORE)&&m.energy==energy&&count(chest,Items.RAW_IRON)==loot,"cancelled");}finally{NeoForge.EVENT_BUS.unregister(listener);}
        m.energy=511;check.test("advanced miner insufficient EU preserves block",!m.mine(target)&&l.getBlockState(target).is(Blocks.IRON_ORE),m.energy);m.energy=100000;
        // Redstone and scanner exhaustion must not advance the position or spend mining energy.
        l.setBlock(p.south(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);int z=m.zcounter;check.test("advanced miner redstone stops scan",!m.work()&&m.zcounter==z,m.zcounter);l.removeBlock(p.south(),false);
        m.scannerSlot.put(new ItemStack(Ic2Items.OD_SCANNER.get()));check.test("advanced miner empty scanner stops scan",!m.work()&&m.zcounter==z,m.zcounter);
        m.scannerSlot.put(Ic2Items.OD_SCANNER.get().charged());m.xcounter=m.zcounter=0;m.ticker=19;l.setBlock(target,Blocks.AIR.defaultBlockState(),3);m.work();check.test("advanced miner default scan budget",m.zcounter==5&&m.xcounter==0,m.zcounter);
        m.upgradeSlot.put(new ItemStack(Ic2Items.OVERCLOCKER_UPGRADE.get(),2));m.setUpgradestat();m.xcounter=m.zcounter=-8;m.ticker=19;m.work();check.test("advanced miner overclockers increase scan budget",m.zcounter==7&&m.xcounter==-8,m.zcounter);
        m.upgradeSlot.clear();m.upgradeSlot.put(new ItemStack(Ic2Items.TRANSFORMER_UPGRADE.get()));m.setUpgradestat();check.test("advanced miner transformer tier and range",m.getTier()==4&&m.range()==8,m.getTier());m.scannerSlot.put(Ic2Items.OV_SCANNER.get().charged());check.test("advanced miner OV covers 33 by 33",m.range()==16,m.range());
        m.setActive(false);m.event(1);m.event(2);check.test("advanced miner mode controls while stopped",!m.blacklist&&m.silktouch,"whitelist/silk");m.setActive(true);m.event(1);check.test("advanced miner active mode change rejected",!m.blacklist,"active");m.setActive(false);m.event(0);check.test("advanced miner reset restores scan origin",m.minelayer==p.getY()-1&&m.xcounter==99&&m.zcounter==99&&m.ticker==0,m.minelayer);
        m.minelayer=-32;m.xcounter=4;m.zcounter=-3;m.ticker=13;m.ListSlot.put(new ItemStack(Items.RAW_IRON));var saved=(TileEntityAdvMiner)BlockEntity.loadStatic(p,m.getBlockState(),m.saveWithFullMetadata(l.registryAccess()),l.registryAccess());
        check.test("advanced miner negative height and scan state persist",saved.minelayer==-32&&saved.xcounter==4&&saved.zcounter==-3&&saved.ticker==13&&!saved.blacklist&&saved.silktouch&&saved.ListSlot.get().is(Items.RAW_IRON)&&saved.energy==m.energy,saved.minelayer);
        var low=new BlockPos(p.getX(),l.getMinBuildHeight()+1,p.getZ());l.setBlock(low,Ic2Blocks.ADV_MINER.get().defaultBlockState(),3);var deep=(TileEntityAdvMiner)l.getBlockEntity(low);deep.onLoaded();deep.energy=100000;deep.scannerSlot.put(Ic2Items.OD_SCANNER.get().charged());deep.xcounter=deep.zcounter=0;deep.ticker=19;l.setBlock(low.below(),Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(),3);deep.work();
        check.test("advanced miner reaches modern minimum build height",l.getBlockState(low.below()).isAir()&&deep.minelayer==l.getMinBuildHeight()&&deep.energy==99488,deep.minelayer);
        deep.xcounter=9;deep.zcounter=-8;deep.ticker=19;energy=deep.energy;check.test("advanced miner terminates below world floor",!deep.work()&&deep.minelayer==l.getMinBuildHeight()-1&&deep.energy==energy,deep.minelayer);
        var unloaded=p.offset(200000,0,200000);check.test("advanced miner does not force load chunks",!l.hasChunkAt(unloaded)&&!m.mine(unloaded)&&!l.hasChunkAt(unloaded),unloaded);
        boolean recipe=l.getRecipeManager().getRecipes().stream().anyMatch(r->r.value().getResultItem(l.registryAccess()).is(Ic2Blocks.ADV_MINER.get().asItem()));check.test("advanced miner original crafting recipe",recipe,"recipe");
    }
}
