package ic2.core.block.kineticgenerator;
import ic2.api.energy.tile.*;
import ic2.api.recipe.Recipes;
import ic2.core.*;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.*;
import ic2.core.fluid.FluidMachine;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.upgrade.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import java.util.*;
public class TileEntityStirlingKineticGenerator extends TileEntityInventory implements IKineticSource,FluidMachine,IHasGui,IUpgradableBlock {
    public final FluidTank inputTank=new FluidTank(2000,f->Recipes.liquidHeatupManager.getSingleDirectionLiquidManager().acceptsFluid(f.getFluid())){protected void onContentsChanged(){setChanged();}};
    public final FluidTank outputTank=new FluidTank(2000){protected void onContentsChanged(){setChanged();}};
    private final FluidTank[] tanks={inputTank,outputTank};
    public final InvSlotConsumableLiquid inputSlot=FluidMachine.slot(this,"input",false),containerSlot=FluidMachine.slot(this,"container",true);
    public final InvSlotOutput emptySlot=new InvSlotOutput(this,"empty",1),outputSlot=new InvSlotOutput(this,"output",1);
    public final InvSlotUpgrade upgrades=new InvSlotUpgrade(this,"upgrade",3);
    public int heatbuffer,kUBuffer,liquidHeatStored;
    public TileEntityStirlingKineticGenerator(BlockPos p,BlockState s){super(Ic2BlockEntities.STIRLING_KINETIC_GENERATOR.get(),p,s);}
    public FluidTank[] tanks(){return tanks;}
    protected void updateEntityServer(){
        FluidMachine.container(inputSlot,emptySlot,inputTank,false);FluidMachine.container(containerSlot,outputSlot,outputTank,true);
        for(var d:Direction.values())if(d!=getFacing()&&heatbuffer<1000){var be=ic2.core.util.Util.loadedBlockEntity(level,worldPosition.relative(d));if(be instanceof IHeatSource source){int request=Math.min(1000-heatbuffer,source.maxrequestHeatTick(d.getOpposite()));if(request>0){int received=source.requestHeat(d.getOpposite(),request);if(received>0){heatbuffer+=received;setChanged();}}}}
        setActive(work());for(int i=0;i<upgrades.size();i++){var stack=upgrades.get(i);if(!stack.isEmpty()&&stack.getItem() instanceof IUpgradeItem u&&u.onTick(stack,this))setChanged();}
    }
    public boolean work(){
        if(inputTank.isEmpty()||!inputTank.isFluidValid(inputTank.getFluid()))return false;
        var property=Recipes.liquidHeatupManager.getHeatExchangeProperty(inputTank.getFluid().getFluid());if(property==null||property.huPerMB<=0)return false;
        int capacity=outputTank.fill(new FluidStack(property.outputFluid,Math.min(2000,inputTank.getFluidAmount())),FluidAction.SIMULATE);if(capacity<=0)return false;
        // Four hU yield twelve kU, with one quarter of the heat warming the working fluid.
        int units=Math.min(heatbuffer/4,Math.min((2000-kUBuffer)/12,Math.max(0,capacity*property.huPerMB-liquidHeatStored)));
        if(units>0){kUBuffer+=units*12;heatbuffer-=units*4;liquidHeatStored+=units;setChanged();}
        int amount=Math.min(capacity,liquidHeatStored/property.huPerMB);
        if(amount>0){inputTank.drain(amount,FluidAction.EXECUTE);outputTank.fill(new FluidStack(property.outputFluid,amount),FluidAction.EXECUTE);liquidHeatStored-=amount*property.huPerMB;setChanged();}
        return units>0;
    }
    public int maxrequestkineticenergyTick(Direction side){return side==getFacing()?kUBuffer:0;}
    public int requestkineticenergy(Direction side,int amount){if(side!=getFacing()||amount<=0)return 0;int out=Math.min(amount,kUBuffer);kUBuffer-=out;if(out>0)setChanged();return out;}
    public FluidTank getinputtank(){return inputTank;}public FluidTank getoutputtank(){return outputTank;}
    @Override public boolean wrenchCanSetFacing(net.minecraft.world.entity.player.Player p,Direction d){return d!=getFacing();}
    @Override public ContainerBase<?> getGuiContainer(int id,Inventory i){return new ic2.core.block.machine.container.ContainerStirlingKineticGenerator(id,i,this);}
    @Override public void readFromNBT(CompoundTag n,HolderLookup.Provider r){super.readFromNBT(n,r);inputTank.readFromNBT(r,n.getCompound("inputTank"));outputTank.readFromNBT(r,n.getCompound("outputTank"));heatbuffer=Math.max(0,Math.min(1000,n.getInt("heatbuffer")));kUBuffer=Math.max(0,Math.min(2000,n.contains("kUBuffer")?n.getInt("kUBuffer"):n.getInt("kubuffer")));liquidHeatStored=Math.max(0,n.getInt("liquidHeatStored"));}
    @Override public void writeToNBT(CompoundTag n,HolderLookup.Provider r){super.writeToNBT(n,r);n.put("inputTank",inputTank.writeToNBT(r,new CompoundTag()));n.put("outputTank",outputTank.writeToNBT(r,new CompoundTag()));n.putInt("heatbuffer",heatbuffer);n.putInt("kUBuffer",kUBuffer);n.putInt("liquidHeatStored",liquidHeatStored);}
    public double getEnergy(){return 40;}public boolean useEnergy(double n){return true;}
    public Set<UpgradableProperty> getUpgradableProperties(){return EnumSet.of(UpgradableProperty.ItemConsuming,UpgradableProperty.ItemProducing,UpgradableProperty.FluidConsuming,UpgradableProperty.FluidProducing);}
}
