package ic2.core.audit;
import ic2.api.energy.EnergyNet;
import ic2.api.item.ElectricItem;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.wiring.*;
import ic2.core.block.machine.tileentity.*;
import ic2.core.block.kineticgenerator.TileEntityStirlingKineticGenerator;
import ic2.core.fluid.Ic2Fluids;
import ic2.core.ref.*;
import ic2.core.util.Keyboard;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
public final class UtilityAudit {
    public static void run(ServerLevel l,BlockPos p,CropAudit.Check check){
        for(var pos:BlockPos.betweenClosed(p.offset(-2,-1,-2),p.offset(8,3,4)))l.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
        l.setBlock(p.south(),Blocks.STONE.defaultBlockState(),3);l.setBlock(p,Ic2Blocks.LUMINATOR.get().defaultBlockState(),3);var lamp=(TileEntityLuminator)l.getBlockEntity(p);lamp.onLoaded();
        check.test("luminator bounded EU tank returns overflow",lamp.injectEnergy(Direction.SOUTH,32,32)==30&&lamp.energy==2&&lamp.getDemandedEnergy()==0,lamp.energy);lamp.tick();check.test("luminator lights with 15 light and costs quarter EU per tick",lamp.energy==1&&lamp.getActive()&&lamp.getBlockState().getLightEmission(l,p)==15,lamp.energy);for(int i=0;i<4;i++)lamp.tick();check.test("luminator goes dark without charge",lamp.energy==0&&!lamp.getActive(),lamp.energy);
        lamp.energy=2;l.setBlock(p.west(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);lamp.ticker=0;lamp.tick();check.test("luminator redstone disables default mode",lamp.energy==2&&!lamp.getActive(),lamp.energy);lamp.setFacing(Direction.UP);lamp.ticker=0;lamp.tick();check.test("luminator wrench switches redstone polarity",!lamp.lswitch&&lamp.energy==1&&lamp.getActive()&&lamp.getFacing()==Direction.NORTH,"inverted, orientation preserved");l.removeBlock(p.west(),false);
        var saved=(TileEntityLuminator)BlockEntity.loadStatic(p,lamp.getBlockState(),lamp.saveWithFullMetadata(l.registryAccess()),l.registryAccess());check.test("luminator switch and EU persist",saved.energy==1&&!saved.lswitch,saved.energy);
        l.removeBlock(p.south(),false);check.test("luminator disappears when support removed",l.getBlockState(p).isAir(),l.getBlockState(p));
        var cablePos=p.south();l.setBlock(cablePos,Ic2Blocks.CABLES.get(CableType.glass_fibre).get().defaultBlockState(),3);var cable=(TileEntityCable)l.getBlockEntity(cablePos);cable.onLoaded();l.setBlock(p,Ic2Blocks.LUMINATOR.get().defaultBlockState().setValue(BlockLuminator.FULL_COVER,true),3);lamp=(TileEntityLuminator)l.getBlockEntity(p);lamp.onLoaded();
        check.test("luminator accepts only its supporting cable",lamp.acceptsEnergyFrom(cable,Direction.SOUTH)&&!lamp.acceptsEnergyFrom(cable,Direction.NORTH)&&!lamp.acceptsEnergyFrom(lamp,Direction.SOUTH),"front/back");
        l.setBlock(p.south(2),Ic2Blocks.BATBOX.get().defaultBlockState().setValue(BlockTileEntity.FACING,Direction.NORTH),3);var battery=(TileEntityElectricBlock)l.getBlockEntity(p.south(2));battery.energy=1000;battery.onLoaded();var energyNet=(ic2.core.energy.EnergyNetGlobal)EnergyNet.instance;boolean lit=false;for(int i=0;i<16;i++){battery.tick();energyNet.onLevelTick(new LevelTickEvent.Post(()->true,l));lamp.tick();lit|=lamp.getActive();}
        check.test("real BatBox cable luminator energy network",lit&&battery.energy<1000&&lamp.energy<=2,"battery="+battery.energy+" lamp="+lamp.energy);
        // Modern fluid item capability drives actual bottler refuelling.
        var jet=Ic2Items.FUEL_JETPACK.get();var stack=new ItemStack(jet);var handler=stack.getCapability(Capabilities.FluidHandler.ITEM);check.test("fuel jetpack fluid capability rejects water",handler!=null&&handler.getTankCapacity(0)==30000&&handler.fill(new FluidStack(Fluids.WATER,1000),FluidAction.EXECUTE)==0,handler==null?"null":handler.getTankCapacity(0));
        check.test("fuel jetpack fill simulation preserves contents",handler.fill(new FluidStack(Ic2Fluids.BIOGAS.get(),40000),FluidAction.SIMULATE)==30000&&jet.isEmpty(stack),"simulation");handler.fill(new FluidStack(Ic2Fluids.BIOGAS.get(),40000),FluidAction.EXECUTE);check.test("fuel jetpack capacity and simulated drain",jet.getFluid(stack).getAmount()==30000&&jet.drain(stack,2,false).getAmount()==2&&jet.getFluid(stack).getAmount()==30000,jet.getFluid(stack));
        var reloaded=ItemStack.parse(l.registryAccess(),stack.save(l.registryAccess())).orElseThrow();check.test("fuel jetpack component persistence",jet.getFluid(reloaded).getAmount()==30000,jet.getFluid(reloaded));
        var actor=FakePlayerFactory.getMinecraft(l);actor.getInventory().clearContent();actor.setItemSlot(EquipmentSlot.CHEST,stack);actor.setOnGround(false);actor.setPos(p.getX(),100,p.getZ());actor.setDeltaMovement(Vec3.ZERO);actor.fallDistance=10;Keyboard.processKeyUpdate(actor,0);jet.useJetpack(actor,false);
        check.test("fuel jetpack thrust costs two mB and resets fall distance",Math.abs(actor.getDeltaMovement().y-.2)<.001&&jet.getFluid(stack).getAmount()==29998&&actor.fallDistance==0,actor.getDeltaMovement());actor.setDeltaMovement(0,-.1,0);jet.useJetpack(actor,true);check.test("fuel jetpack hover costs one mB and stops descent",Math.abs(actor.getDeltaMovement().y)<.001&&jet.getFluid(stack).getAmount()==29997,actor.getDeltaMovement());
        actor.setOnGround(true);jet.useJetpack(actor,false);check.test("fuel jetpack does not burn fuel on ground",jet.getFluid(stack).getAmount()==29997,jet.getFluid(stack));actor.setOnGround(false);jet.drain(stack,30000,true);handler.fill(new FluidStack(Ic2Fluids.BIOGAS.get(),3000),FluidAction.EXECUTE);actor.setDeltaMovement(Vec3.ZERO);jet.useJetpack(actor,false);check.test("fuel jetpack low fuel reduces thrust below twenty percent",Math.abs(actor.getDeltaMovement().y-.1)<.001,actor.getDeltaMovement());
        jet.filltank(stack);actor.setPos(p.getX(),l.getMaxBuildHeight(),p.getZ());actor.setDeltaMovement(Vec3.ZERO);jet.useJetpack(actor,false);check.test("fuel jetpack modern height ceiling",actor.getDeltaMovement().y==0,actor.getY());jet.drain(stack,30000,true);actor.setDeltaMovement(Vec3.ZERO);check.test("empty fuel jetpack cannot fly",!jet.useJetpack(actor,false)&&actor.getDeltaMovement().equals(Vec3.ZERO),"empty");
        jet.filltank(stack);actor.setPos(p.getX(),100,p.getZ());Keyboard.processKeyUpdate(actor,(1<<Keyboard.Key.jump.ordinal())|(1<<Keyboard.Key.modeSwitch.ordinal()));jet.onArmorTick(l,actor,stack);check.test("fuel jetpack mode key persists hover with debounce",stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag().getBoolean("hoverMode"),"mode key");Keyboard.processKeyUpdate(actor,0);
        var electric=Ic2Items.ELECTRIC_JETPACK.get();var electricStack=electric.charged();actor.setItemSlot(EquipmentSlot.CHEST,electricStack);electric.useJetpack(actor,false);check.test("electric jetpack original eight EU flight cost",ElectricItem.manager.getCharge(electricStack)==29992,ElectricItem.manager.getCharge(electricStack));electric.useJetpack(actor,true);check.test("electric jetpack original seven EU hover cost",ElectricItem.manager.getCharge(electricStack)==29985,ElectricItem.manager.getCharge(electricStack));actor.getInventory().clearContent();
        var bpos=p.east(4);l.setBlock(bpos,Ic2Blocks.FLUID_BOTTLER.get().defaultBlockState(),3);var bottler=(TileEntityFluidBottler)l.getBlockEntity(bpos);bottler.fill(new FluidStack(Ic2Fluids.BIOGAS.get(),8000),FluidAction.EXECUTE);bottler.fillInputSlot.put(new ItemStack(jet));var out=bottler.getOutput();if(out!=null)bottler.operateOnce(out,out.items);check.test("actual fluid bottler refuels jetpack without loss",out!=null&&jet.getFluid(bottler.outputSlot.get()).getAmount()==8000&&bottler.getFluidInTank(0).isEmpty()&&bottler.fillInputSlot.isEmpty(),bottler.outputSlot.get());
        var sp=p.east(7);l.setBlock(sp,Ic2Blocks.STIRLING_KINETIC_GENERATOR.get().defaultBlockState(),3);var stirling=(TileEntityStirlingKineticGenerator)l.getBlockEntity(sp);var port=l.getCapability(Capabilities.FluidHandler.BLOCK,sp,Direction.EAST);check.test("Stirling external fluid capability is registered",port!=null&&port.fill(new FluidStack(Fluids.WATER,100),FluidAction.EXECUTE)==100,port==null?"null":stirling.inputTank.getFluidAmount());stirling.heatbuffer=40;stirling.work();check.test("Stirling external port drains heated output only",port!=null&&port.drain(100,FluidAction.EXECUTE).is(Ic2Fluids.HOT_WATER.get())&&stirling.inputTank.getFluidAmount()==90,stirling.inputTank.getFluidAmount());
        boolean recipes=true;for(var item:new Item[]{Ic2Blocks.LUMINATOR.get().asItem(),jet,Ic2Items.NIGHTVISION_GOGGLES.get(),Ic2Blocks.SCANNER.get().asItem()})recipes&=l.getRecipeManager().getRecipes().stream().anyMatch(r->r.value().getResultItem(l.registryAccess()).is(item));check.test("luminator fuel jetpack and dependent scanner recipes",recipes,"recipes");
    }
}
