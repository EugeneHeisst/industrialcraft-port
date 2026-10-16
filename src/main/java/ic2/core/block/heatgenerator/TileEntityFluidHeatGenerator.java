package ic2.core.block.heatgenerator;
import ic2.api.recipe.Recipes;
import ic2.core.*;
import ic2.core.block.TileEntityHeatSourceInventory;
import ic2.core.block.invslot.*;
import ic2.core.fluid.FluidMachine;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
public class TileEntityFluidHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui,FluidMachine {
    public final FluidTank tank=new FluidTank(10000,f->Recipes.FluidHeatGenerator.acceptsFluid(f.getFluid())){protected void onContentsChanged(){setChanged();}};
    private final FluidTank[] tanks={tank};
    public final InvSlotConsumableLiquid inputSlot=new InvSlotConsumableLiquid(this,"input",InvSlot.Access.I,1,InvSlot.InvSide.TOP){public boolean accepts(net.minecraft.world.item.ItemStack s){var f=contained(s);return !f.isEmpty()&&Recipes.FluidHeatGenerator.acceptsFluid(f.getFluid());}};
    public final InvSlotOutput outputSlot=new InvSlotOutput(this,"output",1);
    public int burnTicks,burnHeat;
    public TileEntityFluidHeatGenerator(BlockPos p,BlockState s){super(Ic2BlockEntities.FLUID_HEAT_GENERATOR.get(),p,s);}
    public FluidTank[] tanks(){return tanks;}
    public int getMaxHeatEmittedPerTick(){if(burnTicks>0)return burnHeat;var p=Recipes.FluidHeatGenerator.getBurnProperty(tank.getFluid().getFluid());return p!=null&&tank.getFluidAmount()>=p.amount?p.heat:0;}
    protected int fillHeatBuffer(int space){
        if(burnTicks<=0){var p=Recipes.FluidHeatGenerator.getBurnProperty(tank.getFluid().getFluid());if(p==null||tank.getFluidAmount()<p.amount)return 0;tank.drain(p.amount,FluidAction.EXECUTE);burnTicks=20;burnHeat=p.heat;}
        burnTicks--;setChanged();return Math.min(space,burnHeat);
    }
    protected void updateEntityServer(){FluidMachine.container(inputSlot,outputSlot,tank,false);super.updateEntityServer();setActive(getHeatBuffer()>0);}
    @Override public ContainerBase<?> getGuiContainer(int id,Inventory i){return new ic2.core.block.machine.container.ContainerThermalFluids.Generator(id,i,this);}
    @Override public void readFromNBT(CompoundTag n,HolderLookup.Provider r){super.readFromNBT(n,r);tank.readFromNBT(r,n.getCompound("tank"));burnTicks=Math.max(0,Math.min(20,n.getInt("burnTicks")));burnHeat=Math.max(0,n.getInt("burnHeat"));}
    @Override public void writeToNBT(CompoundTag n,HolderLookup.Provider r){super.writeToNBT(n,r);n.put("tank",tank.writeToNBT(r,new CompoundTag()));n.putInt("burnTicks",burnTicks);n.putInt("burnHeat",burnHeat);}
}
