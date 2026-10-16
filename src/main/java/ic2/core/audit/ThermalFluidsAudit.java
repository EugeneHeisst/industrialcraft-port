package ic2.core.audit;
import ic2.api.recipe.Recipes;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.heatgenerator.TileEntityFluidHeatGenerator;
import ic2.core.block.machine.tileentity.*;
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
public final class ThermalFluidsAudit {
    public static void run(ServerLevel l,BlockPos p,CropAudit.Check check){
        for(var pos:BlockPos.betweenClosed(p.offset(-2,-1,-3),p.offset(12,2,3)))l.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
        l.setBlock(p,Ic2Blocks.FLUID_HEAT_GENERATOR.get().defaultBlockState(),3);var gen=(TileEntityFluidHeatGenerator)l.getBlockEntity(p);gen.onLoaded();
        check.test("fluid heat recipe API aliases",Recipes.FluidHeatGenerator.getBurnProperty(Ic2Fluids.BIOGAS.get()).heat==32&&Recipes.liquidCooldownManager.getHeatExchangeProperty(Ic2Fluids.HOT_COOLANT.get()).outputFluid==Ic2Fluids.COOLANT.get(),"biogas 32 hU/t, hot coolant 20 hU/mB");
        check.test("fluid heat generator rejects water",gen.fill(new FluidStack(Fluids.WATER,1000),FluidAction.EXECUTE)==0&&!gen.inputSlot.accepts(new ItemStack(Items.WATER_BUCKET)),"fuel validation");
        gen.fill(new FluidStack(Ic2Fluids.BIOGAS.get(),10),FluidAction.EXECUTE);gen.tick();
        check.test("fluid heat generator front output",gen.requestHeat(Direction.EAST,32)==0&&gen.getHeatBuffer()==32,gen.getHeatBuffer());
        int produced=0;for(int i=0;i<20;i++){produced+=gen.requestHeat(Direction.NORTH,32);gen.tick();}
        check.test("fluid heat exact fuel batch",produced==640&&gen.tank.isEmpty()&&gen.getHeatBuffer()==0,"heat="+produced+" fuel="+gen.tank.getFluidAmount());
        gen.fill(new FluidStack(Ic2Fluids.BIOGAS.get(),20),FluidAction.EXECUTE);gen.tick();for(int i=0;i<50;i++)gen.tick();
        check.test("fluid heat idle buffer does not burn",gen.tank.getFluidAmount()==10&&gen.burnTicks==19,"fuel="+gen.tank.getFluidAmount()+" ticks="+gen.burnTicks);
        var gcopy=(TileEntityFluidHeatGenerator)BlockEntity.loadStatic(p,gen.getBlockState(),gen.saveWithFullMetadata(l.registryAccess()),l.registryAccess());
        check.test("fluid heat pending burn persists",gcopy.burnTicks==19&&gcopy.burnHeat==32&&gcopy.getHeatBuffer()==32&&gcopy.tank.getFluidAmount()==10,gcopy.burnTicks);
        var ep=p.east(3);l.setBlock(ep,Ic2Blocks.LIQUID_HEAT_EXCHANGER.get().defaultBlockState(),3);var ex=(TileEntityLiquidHeatExchanger)l.getBlockEntity(ep);for(int i=0;i<10;i++)ex.conductors.put(i,new ItemStack(Ic2Items.get("heat_conductor")));ex.onLoaded();
        ex.fill(new FluidStack(Fluids.LAVA,1000),FluidAction.EXECUTE);ex.tick();
        check.test("liquid exchanger lava yields heat and same volume",ex.requestHeat(Direction.NORTH,100)==100&&ex.inputTank.getFluidAmount()==995&&ex.outputTank.getFluidAmount()==5&&ex.outputTank.getFluid().is(Ic2Fluids.PAHOEHOE_LAVA.get()),ex.outputTank.getFluid());
        ex.outputTank.setFluid(new FluidStack(Ic2Fluids.PAHOEHOE_LAVA.get(),2000));ex.tick();
        check.test("liquid exchanger full output does not consume lava",ex.inputTank.getFluidAmount()==995&&ex.getHeatBuffer()==0,ex.inputTank.getFluidAmount());
        ex.outputTank.setFluid(new FluidStack(Fluids.WATER,100));ex.tick();check.test("liquid exchanger mismatched output blocks",ex.inputTank.getFluidAmount()==995&&ex.getHeatBuffer()==0,ex.outputTank.getFluid());
        ex.outputTank.setFluid(FluidStack.EMPTY);ex.inputTank.setFluid(new FluidStack(Ic2Fluids.HOT_COOLANT.get(),100));ex.tick();
        check.test("liquid exchanger coolant recovery",ex.inputTank.getFluidAmount()==95&&ex.outputTank.getFluidAmount()==5&&ex.outputTank.getFluid().is(Ic2Fluids.COOLANT.get()),ex.outputTank.getFluid());
        check.test("heat exchange directional API",Recipes.liquidCooldownManager.getSingleDirectionLiquidManager().acceptsFluid(Fluids.LAVA)&&!Recipes.liquidCooldownManager.getSingleDirectionLiquidManager().acceptsFluid(Ic2Fluids.HOT_COOLANT.get()),"lava one-way, coolant reversible");
        var fp=p.east(7);l.setBlock(fp,Ic2Blocks.FERMENTER.get().defaultBlockState().setValue(BlockTileEntity.FACING,Direction.SOUTH),3);l.setBlock(fp.south(),Ic2Blocks.FLUID_HEAT_GENERATOR.get().defaultBlockState(),3);
        var f=(TileEntityFermenter)l.getBlockEntity(fp);var source=(TileEntityFluidHeatGenerator)l.getBlockEntity(fp.south());f.onLoaded();source.onLoaded();f.fill(new FluidStack(Ic2Fluids.BIOMASS.get(),500),FluidAction.EXECUTE);source.fill(new FluidStack(Ic2Fluids.BIOGAS.get(),10000),FluidAction.EXECUTE);
        int gas=0;for(int i=0;i<14000&&f.fertilizerSlot.isEmpty();i++){source.tick();f.tick();gas+=f.drain(2000,FluidAction.EXECUTE).getAmount();}
        check.test("fermenter heat chain and fertilizer",f.inputTank.isEmpty()&&gas==10000&&f.fertilizerSlot.get().is(Ic2Items.FERTILIZER.get())&&f.progress==0,"gas="+gas+" fertilizer="+f.fertilizerSlot.get()+" fuel left="+source.tank.getFluidAmount());
        f.inputTank.fill(new FluidStack(Ic2Fluids.BIOMASS.get(),100),FluidAction.EXECUTE);f.outputTank.fill(new FluidStack(Ic2Fluids.BIOGAS.get(),2000),FluidAction.EXECUTE);f.heatbuffer=7999;
        check.test("fermenter full output preserves biomass and heat",!f.work()&&f.heatbuffer==7999&&f.inputTank.getFluidAmount()==100,f.heatbuffer);
        f.outputTank.drain(2000,FluidAction.EXECUTE);f.heatbuffer=8000;f.work();
        check.test("fermenter stored heat resumes conversion",f.inputTank.getFluidAmount()==90&&f.outputTank.getFluidAmount()==200&&f.progress==10,f.outputTank.getFluid());
        var fcopy=(TileEntityFermenter)BlockEntity.loadStatic(fp,f.getBlockState(),f.saveWithFullMetadata(l.registryAccess()),l.registryAccess());check.test("fermenter persistence",fcopy.progress==10&&fcopy.inputTank.getFluidAmount()==90&&fcopy.outputTank.getFluidAmount()==200,fcopy.progress);
        boolean recipes=true;for(var item:new Item[]{Ic2Blocks.FLUID_HEAT_GENERATOR.get().asItem(),Ic2Blocks.LIQUID_HEAT_EXCHANGER.get().asItem(),Ic2Blocks.FERMENTER.get().asItem()})recipes&=l.getRecipeManager().getRecipes().stream().anyMatch(r->r.value().getResultItem(l.registryAccess()).is(item));check.test("thermal fluid machine crafting recipes",recipes,"three original recipes");
    }
}
