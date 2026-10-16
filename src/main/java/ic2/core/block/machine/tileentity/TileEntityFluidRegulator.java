package ic2.core.block.machine.tileentity;
import ic2.core.*;
import ic2.core.block.invslot.*;
import ic2.core.fluid.FluidMachine;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
public class TileEntityFluidRegulator extends TileEntityElectricMachine implements IHasGui,FluidMachine {
    public final FluidTank tank=new FluidTank(10000){protected void onContentsChanged(){setChanged();}};
    private final FluidTank[] tanks={tank};
    public final InvSlotConsumableLiquid inputSlot=FluidMachine.slot(this,"container",false);
    public final InvSlotOutput outputSlot=new InvSlotOutput(this,"output",1);
    public int outputmb,mode,ticker;
    public TileEntityFluidRegulator(BlockPos p,BlockState s){super(Ic2BlockEntities.FLUID_REGULATOR.get(),p,s,10000,4);}
    public FluidTank[] tanks(){return tanks;}public boolean canInput(Direction d){return d!=getFacing();}
    @Override public boolean wrenchCanSetFacing(Player p,Direction d){return d!=getFacing();}
    @Override protected void updateEntityServer(){super.updateEntityServer();FluidMachine.container(inputSlot,outputSlot,tank,false);if(ticker++%20==0||mode==1)setActive(work());}
    public boolean work(){
        if(outputmb<=0||energy<10||tank.isEmpty()||!level.hasChunkAt(worldPosition.relative(getFacing())))return false;
        var target=level.getCapability(Capabilities.FluidHandler.BLOCK,worldPosition.relative(getFacing()),getFacing().getOpposite());
        if(target!=null&&FluidMachine.transfer(tank,target,outputmb)>0){energy-=10;setChanged();return true;}return false;
    }
    public void event(int event){if(event==1001)mode=1;else if(event==1002)mode=0;else outputmb=(int)Math.max(0,Math.min(1000,(long)outputmb+event));setChanged();}
    @Override public ContainerBase<?> getGuiContainer(int id,Inventory i){return new ic2.core.block.machine.container.ContainerFluidMachines.Regulator(id,i,this);}
    @Override public void readFromNBT(CompoundTag n,HolderLookup.Provider r){super.readFromNBT(n,r);tank.readFromNBT(r,n.getCompound("tank"));outputmb=Math.max(0,Math.min(1000,n.getInt("outputmb")));mode=n.getInt("mode")==1?1:0;}
    @Override public void writeToNBT(CompoundTag n,HolderLookup.Provider r){super.writeToNBT(n,r);n.put("tank",tank.writeToNBT(r,new CompoundTag()));n.putInt("outputmb",outputmb);n.putInt("mode",mode);}
}
