package ic2.core.block.machine.tileentity;
import ic2.api.energy.tile.IHeatSource;
import ic2.core.*;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.*;
import ic2.core.fluid.*;
import ic2.core.ref.*;
import ic2.core.upgrade.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import java.util.*;
public class TileEntityFermenter extends TileEntityInventory implements IHasGui,FluidMachine,IUpgradableBlock {
    public final FluidTank inputTank=new FluidTank(10000,f->f.is(Ic2Fluids.BIOMASS.get())){protected void onContentsChanged(){setChanged();}};
    public final FluidTank outputTank=new FluidTank(2000,f->f.is(Ic2Fluids.BIOGAS.get())){protected void onContentsChanged(){setChanged();}};
    private final FluidTank[] tanks={inputTank,outputTank};
    public final InvSlotConsumableLiquid inputSlot=FluidMachine.slot(this,"input",false,Ic2Fluids.BIOMASS.get()),containerSlot=FluidMachine.slot(this,"container",true,Ic2Fluids.BIOGAS.get());
    public final InvSlotOutput emptySlot=new InvSlotOutput(this,"empty",1),gasSlot=new InvSlotOutput(this,"gas",1),fertilizerSlot=new InvSlotOutput(this,"fertilizer",1);
    public final InvSlotUpgrade upgrades=new InvSlotUpgrade(this,"upgrade",2);
    public int heatbuffer,progress;
    public TileEntityFermenter(BlockPos p,BlockState s){super(Ic2BlockEntities.FERMENTER.get(),p,s);}
    public FluidTank[] tanks(){return tanks;}
    protected void updateEntityServer(){FluidMachine.container(inputSlot,emptySlot,inputTank,false);FluidMachine.container(containerSlot,gasSlot,outputTank,true);setActive(work());for(int i=0;i<upgrades.size();i++){var u=upgrades.get(i);if(!u.isEmpty()&&u.getItem() instanceof IUpgradeItem item&&item.onTick(u,this))setChanged();}}
    public boolean work(){
        if(progress>=500){var fertilizer=new ItemStack(Ic2Items.FERTILIZER.get());if(!fertilizerSlot.canAdd(fertilizer))return false;fertilizerSlot.add(fertilizer);progress-=500;setChanged();}
        if(inputTank.getFluidAmount()<10||outputTank.getCapacity()-outputTank.getFluidAmount()<200)return false;
        var neighbor=ic2.core.util.Util.loadedBlockEntity(level,worldPosition.relative(getFacing()));
        if(neighbor instanceof IHeatSource source){int received=source.requestHeat(getFacing().getOpposite(),Math.min(100,8000-heatbuffer));if(received>0){heatbuffer+=received;setChanged();}}
        if(heatbuffer>=8000){heatbuffer-=8000;inputTank.drain(10,FluidAction.EXECUTE);outputTank.fill(new FluidStack(Ic2Fluids.BIOGAS.get(),200),FluidAction.EXECUTE);progress+=10;setChanged();return true;}
        return heatbuffer>0;
    }
    @Override public boolean wrenchCanSetFacing(net.minecraft.world.entity.player.Player p,Direction d){return d!=getFacing();}
    @Override public ContainerBase<?> getGuiContainer(int id,Inventory i){return new ic2.core.block.machine.container.ContainerThermalFluids.Fermenter(id,i,this);}
    @Override public void readFromNBT(CompoundTag n,HolderLookup.Provider r){super.readFromNBT(n,r);inputTank.readFromNBT(r,n.getCompound("inputTank"));outputTank.readFromNBT(r,n.getCompound("outputTank"));heatbuffer=Math.max(0,Math.min(8000,n.getInt("heatbuffer")));progress=Math.max(0,n.getInt("progress"));}
    @Override public void writeToNBT(CompoundTag n,HolderLookup.Provider r){super.writeToNBT(n,r);n.put("inputTank",inputTank.writeToNBT(r,new CompoundTag()));n.put("outputTank",outputTank.writeToNBT(r,new CompoundTag()));n.putInt("heatbuffer",heatbuffer);n.putInt("progress",progress);}
    public double getEnergy(){return 40;}public boolean useEnergy(double n){return true;}
    public Set<UpgradableProperty> getUpgradableProperties(){return EnumSet.of(UpgradableProperty.ItemConsuming,UpgradableProperty.ItemProducing,UpgradableProperty.FluidConsuming,UpgradableProperty.FluidProducing);}
}
