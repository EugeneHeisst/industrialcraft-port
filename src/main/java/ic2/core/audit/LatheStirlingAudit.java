package ic2.core.audit;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.machine.tileentity.*;
import ic2.core.block.kineticgenerator.*;
import ic2.core.block.generator.tileentity.TileEntityGenerators2;
import ic2.core.item.resources.ItemLatheDefault;
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
public final class LatheStirlingAudit {
    public static void run(ServerLevel l,BlockPos p,CropAudit.Check check){
        for(var pos:BlockPos.betweenClosed(p.offset(-2,-1,-2),p.offset(14,2,2)))l.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
        l.setBlock(p,Ic2Blocks.LATHE.get().defaultBlockState(),3);var lathe=(TileEntityLathe)l.getBlockEntity(p);lathe.onLoaded();
        l.setBlock(p.south(),Ic2Blocks.ELECTRIC_KINETIC_GENERATOR.get().defaultBlockState(),3);var motor=(TileEntityKineticGenerators.Electric)l.getBlockEntity(p.south());motor.onLoaded();motor.energy=1000;
        for(int n=0;n<10;n++)motor.slotMotor.put(n,new ItemStack(Ic2Items.get("electric_motor")));motor.tick();lathe.tick();
        lathe.toolSlot.put(new ItemStack(Ic2Items.LATHING_TOOL.get()));lathe.latheSlot.put(new ItemStack(Ic2Items.IRON_TURNING_BLANK.get()));
        check.test("lathe electric motor supplies real kU",lathe.kUBuffer==1000&&motor.ku==0&&motor.energy==750,lathe.kUBuffer);
        check.test("lathe cutting spends energy and tool durability",lathe.process(2)&&lathe.kUBuffer==0&&lathe.toolSlot.get().getDamageValue()==1&&Ic2Items.IRON_TURNING_BLANK.get().getCurrentState(lathe.latheSlot.get())[2]==4,lathe.latheSlot.get());
        check.test("lathe insufficient energy and invalid positions rejected",!lathe.process(2)&&!lathe.process(-1)&&!lathe.process(5),lathe.kUBuffer);
        lathe.kUBuffer=10000;lathe.outputSlot.put(new ItemStack(Ic2Items.get("small_iron_dust"),64));
        check.test("lathe full byproduct slot preserves blank and energy",!lathe.process(2)&&lathe.kUBuffer==10000&&lathe.toolSlot.get().getDamageValue()==1,lathe.kUBuffer);
        lathe.outputSlot.put(ItemStack.EMPTY);lathe.latheSlot.put(ItemLatheDefault.getStackForState(new ItemStack(Ic2Items.IRON_TURNING_BLANK.get()),0x32323));
        var stirlingRecipe=l.getRecipeManager().getRecipes().stream().filter(r->r.value().getResultItem(l.registryAccess()).is(Ic2Blocks.STIRLING_KINETIC_GENERATOR.get().asItem())).findFirst().orElseThrow().value();
        check.test("lathe shaped blank recipe rejects uncut and wrong shapes",stirlingRecipe.getIngredients().stream().anyMatch(i->i.test(lathe.latheSlot.get()))&&stirlingRecipe.getIngredients().stream().noneMatch(i->i.test(new ItemStack(Ic2Items.IRON_TURNING_BLANK.get())))&&stirlingRecipe.getIngredients().stream().noneMatch(i->i.test(ItemLatheDefault.getStackForState(new ItemStack(Ic2Items.IRON_TURNING_BLANK.get()),0x43222))),"component ingredient");
        var copy=(TileEntityLathe)BlockEntity.loadStatic(p,lathe.getBlockState(),lathe.saveWithFullMetadata(l.registryAccess()),l.registryAccess());
        check.test("lathe blank shape tool and energy persistence",copy.kUBuffer==10000&&copy.toolSlot.get().getDamageValue()==1&&ItemLatheDefault.getNumericState(Ic2Items.IRON_TURNING_BLANK.get().getCurrentState(copy.latheSlot.get()))==0x32323,copy.latheSlot.get());
        lathe.latheSlot.put(new ItemStack(Ic2Items.WOOD_TURNING_BLANK.get()));lathe.toolSlot.get().setDamageValue(49);check.test("lathe wooden blank and tool breaks at fifty",lathe.process(0)&&lathe.toolSlot.isEmpty()&&Ic2Items.WOOD_TURNING_BLANK.get().getCurrentState(lathe.latheSlot.get())[0]==2,lathe.latheSlot.get());
        lathe.toolSlot.put(new ItemStack(Ic2Items.LATHING_TOOL.get()));lathe.kUBuffer=10000;lathe.process(0);check.test("lathe minimum section width one",!lathe.process(0)&&lathe.kUBuffer==9000,lathe.kUBuffer);
        var q=p.east(5);l.setBlock(q,Ic2Blocks.STIRLING_KINETIC_GENERATOR.get().defaultBlockState(),3);var s=(TileEntityStirlingKineticGenerator)l.getBlockEntity(q);s.onLoaded();
        check.test("Stirling accepts water but rejects reversible coolant",s.fill(new FluidStack(Fluids.WATER,1000),FluidAction.EXECUTE)==1000&&s.fill(new FluidStack(Ic2Fluids.COOLANT.get(),10),FluidAction.EXECUTE)==0,s.inputTank.getFluid());
        s.heatbuffer=100;s.work();check.test("Stirling original 3 kU per hU and quarter heat to water",s.kUBuffer==300&&s.inputTank.getFluidAmount()==975&&s.outputTank.getFluidAmount()==25&&s.outputTank.getFluid().is(Ic2Fluids.HOT_WATER.get())&&s.heatbuffer==0,s.kUBuffer);
        check.test("Stirling side and simulation restrictions",s.requestkineticenergy(Direction.SOUTH,500)==0&&s.requestkineticenergy(Direction.NORTH,-100)==0&&s.drain(10,FluidAction.SIMULATE).getAmount()==10&&s.outputTank.getFluidAmount()==25,s.kUBuffer);
        s.heatbuffer=1000;s.work();check.test("Stirling buffer hard cap preserves unspent heat",s.kUBuffer==1992&&s.heatbuffer==436&&s.inputTank.getFluidAmount()+s.outputTank.getFluidAmount()==1000,s.kUBuffer);
        check.test("Stirling repeated kinetic pulls consume buffer once",s.requestkineticenergy(Direction.NORTH,1000)==1000&&s.requestkineticenergy(Direction.NORTH,2000)==992&&s.requestkineticenergy(Direction.NORTH,1)==0,s.kUBuffer);
        s.outputTank.setFluid(new FluidStack(Ic2Fluids.HOT_WATER.get(),2000));int heat=s.heatbuffer,water=s.inputTank.getFluidAmount();check.test("Stirling full output preserves fluids and heat",!s.work()&&s.heatbuffer==heat&&s.inputTank.getFluidAmount()==water&&s.kUBuffer==0,s.heatbuffer);
        s.outputTank.setFluid(new FluidStack(Ic2Fluids.BIOGAS.get(),10));check.test("Stirling incompatible output blocks work",!s.work()&&s.heatbuffer==heat,s.heatbuffer);
        s.outputTank.setFluid(FluidStack.EMPTY);s.heatbuffer=3;check.test("Stirling partial heat accumulates without free energy",!s.work()&&s.heatbuffer==3&&s.kUBuffer==0,s.heatbuffer);
        s.heatbuffer=64;s.work();var saved=(TileEntityStirlingKineticGenerator)BlockEntity.loadStatic(q,s.getBlockState(),s.saveWithFullMetadata(l.registryAccess()),l.registryAccess());
        check.test("Stirling tanks energy and heat persistence",saved.kUBuffer==s.kUBuffer&&saved.heatbuffer==s.heatbuffer&&saved.liquidHeatStored==s.liquidHeatStored&&saved.inputTank.getFluidAmount()==s.inputTank.getFluidAmount()&&saved.outputTank.getFluidAmount()==s.outputTank.getFluidAmount(),saved.kUBuffer);
        // Full heat -> Stirling -> EU chain with real loaded machines.
        s.kUBuffer=s.heatbuffer=0;s.inputTank.setFluid(new FluidStack(Fluids.WATER,1000));s.outputTank.setFluid(FluidStack.EMPTY);
        l.setBlock(q.south(),Ic2Blocks.LIQUID_HEAT_EXCHANGER.get().defaultBlockState(),3);var source=(TileEntityLiquidHeatExchanger)l.getBlockEntity(q.south());source.onLoaded();for(int i=0;i<10;i++)source.conductors.put(i,new ItemStack(Ic2Items.get("heat_conductor")));source.inputTank.fill(new FluidStack(Fluids.LAVA,1000),FluidAction.EXECUTE);
        l.setBlock(q.north(),Ic2Blocks.KINETIC_GENERATOR.get().defaultBlockState().setValue(BlockTileEntity.FACING,Direction.SOUTH),3);var eu=(TileEntityGenerators2.Kinetic)l.getBlockEntity(q.north());eu.onLoaded();
        for(int i=0;i<10;i++){source.tick();s.tick();eu.tick();}
        check.test("heat exchanger Stirling to EU conserves working fluid",eu.EUstorage>0&&s.outputTank.getFluidAmount()>0&&s.inputTank.getFluidAmount()+s.outputTank.getFluidAmount()==1000&&source.inputTank.getFluidAmount()<1000,"EU="+eu.EUstorage+" hot="+s.outputTank.getFluidAmount());
        l.removeBlock(q.north(),false);l.setBlock(q.north(),Ic2Blocks.LATHE.get().defaultBlockState(),3);var powered=(TileEntityLathe)l.getBlockEntity(q.north());powered.onLoaded();powered.latheSlot.put(new ItemStack(Ic2Items.IRON_TURNING_BLANK.get()));powered.toolSlot.put(new ItemStack(Ic2Items.LATHING_TOOL.get()));
        for(int i=0;i<10;i++){source.tick();s.tick();powered.tick();}
        check.test("Stirling powers lathe cutting",powered.kUBuffer>=1000&&powered.process(0),powered.kUBuffer);
        boolean recipes=true;for(var item:new Item[]{Ic2Blocks.LATHE.get().asItem(),Ic2Blocks.STIRLING_KINETIC_GENERATOR.get().asItem(),Ic2Items.LATHING_TOOL.get(),Ic2Items.IRON_TURNING_BLANK.get(),Ic2Items.WOOD_TURNING_BLANK.get()})recipes&=l.getRecipeManager().getRecipes().stream().anyMatch(r->r.value().getResultItem(l.registryAccess()).is(item));check.test("lathe and Stirling crafting recipes",recipes,"five recipes");
    }
}
