package ic2.core.audit;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.machine.tileentity.*;
import ic2.core.block.kineticgenerator.TileEntitySteamKineticGenerator;
import ic2.core.block.generator.tileentity.TileEntityGenerators2;
import ic2.core.fluid.Ic2Fluids;
import ic2.core.ref.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
public final class SteamAudit {
    private static TileEntityLiquidHeatExchanger heat(ServerLevel l,BlockPos p,Direction facing){l.setBlock(p,Ic2Blocks.LIQUID_HEAT_EXCHANGER.get().defaultBlockState().setValue(BlockTileEntity.FACING,facing),3);var h=(TileEntityLiquidHeatExchanger)l.getBlockEntity(p);for(int i=0;i<10;i++)h.conductors.put(i,new ItemStack(Ic2Items.get("heat_conductor")));h.inputTank.fill(new FluidStack(Fluids.LAVA,2000),FluidAction.EXECUTE);h.onLoaded();return h;}
    public static void run(ServerLevel l,BlockPos p,CropAudit.Check check){
        for(var pos:BlockPos.betweenClosed(p.offset(-2,-1,-2),p.offset(15,3,3)))l.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
        l.setBlock(p,Ic2Blocks.STEAM_GENERATOR.get().defaultBlockState(),3);var b=(TileEntitySteamGenerator)l.getBlockEntity(p);var h=heat(l,p.south(),Direction.NORTH);
        b.inputmb=1;b.systemheat=99.99f;b.fill(new FluidStack(Fluids.WATER,100),FluidAction.EXECUTE);h.tick();b.produce();
        check.test("steam boiler heats without losing warmup water",b.systemheat==100&&b.waterTank.getFluidAmount()==100&&b.steamTank.isEmpty(),b.systemheat);
        h.tick();b.produce();check.test("steam boiler ratio and calcification",b.waterTank.getFluidAmount()==99&&b.steamTank.getFluidAmount()==100&&b.steamTank.getFluid().is(Ic2Fluids.STEAM.get())&&b.calcification==1,"water="+b.waterTank.getFluidAmount()+" steam="+b.steamTank.getFluidAmount());
        int fuel=h.inputTank.getFluidAmount();h.tick();b.produce();check.test("steam boiler pending output preserves water",b.waterTank.getFluidAmount()==99&&b.steamTank.getFluidAmount()==100&&h.inputTank.getFluidAmount()>=fuel-5,"pending="+b.steamTank.getFluidAmount());
        b.steamTank.setFluid(FluidStack.EMPTY);b.waterTank.setFluid(new FluidStack(Ic2Fluids.DISTILLED_WATER.get(),1));b.inputmb=1000;h.tick();b.produce();
        check.test("steam boiler partial final input and distilled water",b.waterTank.isEmpty()&&b.steamTank.getFluidAmount()==100&&b.calcification==1,b.steamTank.getFluid());
        b.event(20000);b.event(-2000);check.test("steam boiler controls clamp",b.pressurevalve==300&&b.inputmb==0,"pressure="+b.pressurevalve+" input="+b.inputmb);
        b.pressurevalve=220;b.inputmb=1;b.systemheat=374;b.steamTank.setFluid(FluidStack.EMPTY);b.waterTank.fill(new FluidStack(Ic2Fluids.DISTILLED_WATER.get(),10),FluidAction.EXECUTE);
        var h2=heat(l,p.above(),Direction.DOWN);h.tick();h2.tick();b.produce();check.test("steam boiler high pressure superheated steam",b.steamTank.getFluid().is(Ic2Fluids.SUPERHEATED_STEAM.get())&&b.steamTank.getFluidAmount()==100&&b.heatinput==200&&b.targetTemperature()==374,"hU="+b.heatinput);
        var bc=(TileEntitySteamGenerator)BlockEntity.loadStatic(p,b.getBlockState(),b.saveWithFullMetadata(l.registryAccess()),l.registryAccess());check.test("steam boiler persistence",bc.pressurevalve==220&&bc.systemheat==374&&bc.steamTank.getFluidAmount()==100&&bc.waterTank.getFluidAmount()==9,bc.systemheat);
        var tp=p.east(4);l.setBlock(tp,Ic2Blocks.STEAM_KINETIC_GENERATOR.get().defaultBlockState(),3);var t=(TileEntitySteamKineticGenerator)l.getBlockEntity(tp);t.turbineSlot.put(new ItemStack(Ic2Items.STEAM_TURBINE.get()));t.fill(new FluidStack(Ic2Fluids.STEAM.get(),20000),FluidAction.EXECUTE);t.work();
        check.test("steam turbine normal output and recovery",t.kUoutput==1998&&t.steamTank.getFluidAmount()==19000&&t.exhaustTank.getFluidAmount()==900&&t.waterTank.getFluidAmount()==1,"kU="+t.kUoutput);
        int first=t.requestkineticenergy(Direction.NORTH,1000),second=t.requestkineticenergy(Direction.NORTH,1000),third=t.requestkineticenergy(Direction.NORTH,1000);
        check.test("steam turbine output budget cannot be duplicated",first==1000&&second==998&&third==0&&t.requestkineticenergy(Direction.EAST,1)==0,first+second+third);
        t.exhaustTank.setFluid(FluidStack.EMPTY);t.waterTank.setFluid(FluidStack.EMPTY);t.steamTank.setFluid(new FluidStack(Ic2Fluids.SUPERHEATED_STEAM.get(),20000));t.work();
        check.test("steam turbine superheated exhaust and doubled power",t.kUoutput==4000&&t.exhaustTank.getFluidAmount()==1000&&t.hotExhaust&&t.waterTank.isEmpty(),t.kUoutput);
        check.test("steam turbine side restrictions and simulation",t.port(Direction.NORTH).fill(new FluidStack(Ic2Fluids.STEAM.get(),10),FluidAction.EXECUTE)==0&&t.port(Direction.WEST).fill(new FluidStack(Ic2Fluids.STEAM.get(),10),FluidAction.SIMULATE)==0&&t.steamTank.getFluidAmount()==19000,t.steamTank.getFluid());
        t.exhaustTank.setFluid(FluidStack.EMPTY);t.steamTank.setFluid(new FluidStack(Ic2Fluids.STEAM.get(),20000));t.waterTank.fill(new FluidStack(Ic2Fluids.DISTILLED_WATER.get(),1000),FluidAction.EXECUTE);check.test("steam turbine flooded rotor stops",!t.work()&&t.steamTank.getFluidAmount()==20000,"flooded");
        t.waterTank.setFluid(FluidStack.EMPTY);for(int i=0;i<18;i++){t.exhaustTank.setFluid(FluidStack.EMPTY);t.steamTank.setFluid(new FluidStack(Ic2Fluids.STEAM.get(),20000));t.work();}
        check.test("steam turbine rotor wear",t.turbineSlot.get().getDamageValue()==2,t.turbineSlot.get().getDamageValue());
        var tc=(TileEntitySteamKineticGenerator)BlockEntity.loadStatic(tp,t.getBlockState(),t.saveWithFullMetadata(l.registryAccess()),l.registryAccess());check.test("steam turbine tank rotor and condensation persistence",tc.steamTank.getFluidAmount()==t.steamTank.getFluidAmount()&&tc.exhaustTank.getFluidAmount()==t.exhaustTank.getFluidAmount()&&tc.condensation==t.condensation&&tc.turbineSlot.get().getDamageValue()==2,tc.exhaustTank.getFluid());
        // Complete heat -> boiler -> turbine -> EU plus exhaust recovery, all actual machines.
        var q=p.east(9);l.setBlock(q,Ic2Blocks.STEAM_GENERATOR.get().defaultBlockState(),3);var boiler=(TileEntitySteamGenerator)l.getBlockEntity(q);var source=heat(l,q.south(),Direction.NORTH);
        l.setBlock(q.east(),Ic2Blocks.STEAM_KINETIC_GENERATOR.get().defaultBlockState(),3);var turbine=(TileEntitySteamKineticGenerator)l.getBlockEntity(q.east());turbine.turbineSlot.put(new ItemStack(Ic2Items.STEAM_TURBINE.get()));
        l.setBlock(q.east().south(),Ic2Blocks.CONDENSER.get().defaultBlockState(),3);var condenser=(TileEntityCondenser)l.getBlockEntity(q.east().south());
        l.setBlock(q.east().north(),Ic2Blocks.KINETIC_GENERATOR.get().defaultBlockState().setValue(BlockTileEntity.FACING,Direction.SOUTH),3);var electric=(TileEntityGenerators2.Kinetic)l.getBlockEntity(q.east().north());electric.onLoaded();
        boiler.systemheat=100;boiler.inputmb=1;boiler.fill(new FluidStack(Ic2Fluids.DISTILLED_WATER.get(),100),FluidAction.EXECUTE);
        for(int i=0;i<100;i++){source.tick();boiler.produce();boiler.distributeSteam();turbine.exhaust();turbine.work();turbine.exhaust();electric.tick();condenser.work();}
        int mass=boiler.waterTank.getFluidAmount()*100+boiler.steamTank.getFluidAmount()+turbine.steamTank.getFluidAmount()+turbine.exhaustTank.getFluidAmount()+turbine.waterTank.getFluidAmount()*100+turbine.condensation/10+condenser.inputTank.getFluidAmount()+condenser.progress+condenser.outputTank.getFluidAmount()*100;
        check.test("steam to EU closed water balance",electric.EUstorage>0&&mass==10000&&boiler.calcification==0,"EU="+electric.EUstorage+" steam-equivalent="+mass);
        b.calcification=100000;int water=b.waterTank.getFluidAmount();check.test("steam boiler calcification stops work",!b.produce()&&b.waterTank.getFluidAmount()==water,b.calcification);
        var hazard=p.east(14);boolean[] overheated={false};int[] pressureEvents={0};
        java.util.function.Consumer<ic2.api.event.ExplosionEvent> listener=e->{if(e.level==l&&Math.abs(e.x-hazard.getX()-0.5)<0.1&&Math.abs(e.z-hazard.getZ()-0.5)<0.1){if(e.power>=10)overheated[0]=true;else pressureEvents[0]++;e.setCanceled(true);}};
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);
        try{
            l.setBlock(hazard,Ic2Blocks.STEAM_GENERATOR.get().defaultBlockState(),3);var hot=(TileEntitySteamGenerator)l.getBlockEntity(hazard);hot.systemheat=501;hot.onLoaded();hot.tick();
            check.test("steam boiler overheat explosion event",overheated[0]&&l.getBlockState(hazard).isAir(),"explosion cancelled in audit");
            l.setBlock(hazard,Ic2Blocks.STEAM_GENERATOR.get().defaultBlockState(),3);var blocked=(TileEntitySteamGenerator)l.getBlockEntity(hazard);blocked.systemheat=100;blocked.steamTank.fill(new FluidStack(Ic2Fluids.STEAM.get(),100),FluidAction.EXECUTE);blocked.onLoaded();
            for(int attempt=0;attempt<200&&pressureEvents[0]==0;attempt++)blocked.tick();
            check.test("steam boiler blocked exhaust pressure event",pressureEvents[0]>0&&blocked.steamTank.getFluidAmount()==100,"pressure events="+pressureEvents[0]);
            l.removeBlock(hazard,false);
        }finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);}
        boolean recipes=true;for(var item:new Item[]{Ic2Blocks.STEAM_GENERATOR.get().asItem(),Ic2Blocks.STEAM_KINETIC_GENERATOR.get().asItem(),Ic2Items.STEAM_TURBINE.get()})recipes&=l.getRecipeManager().getRecipes().stream().anyMatch(r->r.value().getResultItem(l.registryAccess()).is(item));check.test("steam chain crafting recipes",recipes,"boiler + generator + turbine");
    }
}
