package ic2.core.block.machine.tileentity;
import ic2.core.*;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.*;
import ic2.core.fluid.FluidMachine;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import java.util.ArrayList;
public class TileEntityFluidDistributor extends TileEntityInventory implements IHasGui,FluidMachine {
    public final FluidTank tank=new FluidTank(1000){protected void onContentsChanged(){setChanged();}};
    private final FluidTank[] tanks={tank};
    public final InvSlotConsumableLiquid inputSlot=FluidMachine.slot(this,"container",true);
    public final InvSlotOutput outputSlot=new InvSlotOutput(this,"output",1);
    public TileEntityFluidDistributor(BlockPos p,BlockState s){super(Ic2BlockEntities.FLUID_DISTRIBUTOR.get(),p,s);}
    public FluidTank[] tanks(){return tanks;}
    public boolean canInput(Direction side){return side==null || (getActive()?side!=getFacing():side==getFacing());}
    @Override public boolean wrenchCanSetFacing(Player p,Direction d){return d!=getFacing();}
    @Override protected void updateEntityServer(){FluidMachine.container(inputSlot,outputSlot,tank,true);distribute();}
    public int distribute(){
        var targets=new ArrayList<net.neoforged.neoforge.fluids.capability.IFluidHandler>();
        for(var d:Direction.values())if((getActive()?d==getFacing():d!=getFacing())&&level.hasChunkAt(worldPosition.relative(d))){
            var target=level.getCapability(Capabilities.FluidHandler.BLOCK,worldPosition.relative(d),d.getOpposite());
            if(target!=null&&!tank.isEmpty()&&target.fill(tank.getFluid().copy(),FluidAction.SIMULATE)>0)targets.add(target);
        }
        int total=0;
        while(!targets.isEmpty()&&!tank.isEmpty()){
            int share=Math.max(1,tank.getFluidAmount()/targets.size()),moved=0;
            for(var it=targets.iterator();it.hasNext();){var target=it.next();int n=FluidMachine.transfer(tank,target,share);moved+=n;if(n<share)it.remove();}
            total+=moved;if(moved==0)break;
        }
        return total;
    }
    @Override public ContainerBase<?> getGuiContainer(int id,Inventory i){return new ic2.core.block.machine.container.ContainerFluidMachines.Distributor(id,i,this);}
    @Override public void readFromNBT(CompoundTag n,HolderLookup.Provider r){super.readFromNBT(n,r);tank.readFromNBT(r,n.getCompound("tank"));}
    @Override public void writeToNBT(CompoundTag n,HolderLookup.Provider r){super.writeToNBT(n,r);n.put("tank",tank.writeToNBT(r,new CompoundTag()));}
}
