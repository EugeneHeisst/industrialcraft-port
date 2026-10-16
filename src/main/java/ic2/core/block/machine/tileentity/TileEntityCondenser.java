package ic2.core.block.machine.tileentity;
import ic2.core.*;
import ic2.core.block.invslot.*;
import ic2.core.fluid.*;
import ic2.core.ref.*;
import ic2.core.upgrade.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import java.util.*;
public class TileEntityCondenser extends TileEntityElectricMachine implements IHasGui,FluidMachine,IUpgradableBlock {
    public final FluidTank inputTank=new FluidTank(100000,f->f.is(Ic2Fluids.STEAM.get())||f.is(Ic2Fluids.SUPERHEATED_STEAM.get())){protected void onContentsChanged(){setChanged();}};
    public final FluidTank outputTank=new FluidTank(1000,f->f.is(Ic2Fluids.DISTILLED_WATER.get())){protected void onContentsChanged(){setChanged();}};
    private final FluidTank[] tanks={inputTank,outputTank};
    public final InvSlotConsumableLiquid containerSlot=FluidMachine.slot(this,"container",true,Ic2Fluids.DISTILLED_WATER.get());
    public final InvSlotOutput outputSlot=new InvSlotOutput(this,"output",1);
    public final InvSlot vents=new InvSlot(this,"vents",InvSlot.Access.NONE,4){public boolean accepts(ItemStack s){return s.is(Ic2Items.HEAT_VENT.get());}};
    public final InvSlotUpgrade upgrades=new InvSlotUpgrade(this,"upgrade",1);
    public int progress;
    public TileEntityCondenser(BlockPos p,BlockState s){super(Ic2BlockEntities.CONDENSER.get(),p,s,100000,3);vents.setStackSizeLimit(1);}
    public FluidTank[] tanks(){return tanks;}
    public int getVents(){int n=0;for(int i=0;i<vents.size();i++)if(!vents.get(i).isEmpty())n++;return n;}
    @Override protected void updateEntityServer(){super.updateEntityServer();FluidMachine.container(containerSlot,outputSlot,outputTank,true);setActive(work());var u=upgrades.get();if(!u.isEmpty()&&u.getItem() instanceof IUpgradeItem upgrade&&upgrade.onTick(u,this))setChanged();}
    public boolean work(){
        if(outputTank.getCapacity()-outputTank.getFluidAmount()<100)return false;
        if(progress>=10000){outputTank.fill(new FluidStack(Ic2Fluids.DISTILLED_WATER.get(),100),FluidAction.EXECUTE);progress-=10000;setChanged();return true;}
        if(inputTank.isEmpty())return false;
        int n=getVents(),cost=n*2;if(energy<cost)return false;
        int amount=Math.min(10000-progress,n==0?100:n*100);var drained=inputTank.drain(amount,FluidAction.EXECUTE);
        if(drained.isEmpty())return false;energy-=cost;progress+=drained.getAmount();setChanged();return true;
    }
    @Override public ContainerBase<?> getGuiContainer(int id,Inventory i){return new ic2.core.block.machine.container.ContainerFluidMachines.Condenser(id,i,this);}
    @Override public void readFromNBT(CompoundTag n,HolderLookup.Provider r){super.readFromNBT(n,r);inputTank.readFromNBT(r,n.getCompound("inputTank"));outputTank.readFromNBT(r,n.getCompound("outputTank"));progress=Math.max(0,Math.min(10000,n.getInt("progress")));}
    @Override public void writeToNBT(CompoundTag n,HolderLookup.Provider r){super.writeToNBT(n,r);n.put("inputTank",inputTank.writeToNBT(r,new CompoundTag()));n.put("outputTank",outputTank.writeToNBT(r,new CompoundTag()));n.putInt("progress",progress);}
    public double getEnergy(){return energy;}public boolean useEnergy(double n){if(energy<n)return false;energy-=n;return true;}
    public Set<UpgradableProperty> getUpgradableProperties(){return EnumSet.of(UpgradableProperty.ItemConsuming,UpgradableProperty.ItemProducing,UpgradableProperty.FluidConsuming,UpgradableProperty.FluidProducing);}
}
