package ic2.core.block.machine.tileentity;
import ic2.api.recipe.Recipes;
import ic2.core.*;
import ic2.core.block.TileEntityHeatSourceInventory;
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
public class TileEntityLiquidHeatExchanger extends TileEntityHeatSourceInventory implements IHasGui,FluidMachine,IUpgradableBlock {
    public final FluidTank inputTank=new FluidTank(2000,f->Recipes.liquidCooldownManager.acceptsFluid(f.getFluid())){protected void onContentsChanged(){setChanged();}};
    public final FluidTank outputTank=new FluidTank(2000){protected void onContentsChanged(){setChanged();}};
    private final FluidTank[] tanks={inputTank,outputTank};
    public final InvSlot conductors=new InvSlot(this,"conductors",InvSlot.Access.NONE,10){public boolean accepts(ItemStack s){return s.is(Ic2Items.get("heat_conductor"));}};
    public final InvSlotConsumableLiquid inputSlot=new InvSlotConsumableLiquid(this,"input",InvSlot.Access.I,1,InvSlot.InvSide.TOP){public boolean accepts(ItemStack s){var f=contained(s);return !f.isEmpty()&&Recipes.liquidCooldownManager.acceptsFluid(f.getFluid());}};
    public final InvSlotConsumableLiquid containerSlot=FluidMachine.slot(this,"container",true);
    public final InvSlotOutput emptySlot=new InvSlotOutput(this,"empty",1),outputSlot=new InvSlotOutput(this,"output",1);
    public final InvSlotUpgrade upgrades=new InvSlotUpgrade(this,"upgrade",3);
    public TileEntityLiquidHeatExchanger(BlockPos p,BlockState s){super(Ic2BlockEntities.LIQUID_HEAT_EXCHANGER.get(),p,s);conductors.setStackSizeLimit(1);}
    public FluidTank[] tanks(){return tanks;}
    public int getMaxHeatEmittedPerTick(){int n=0;if(conductors!=null)for(int i=0;i<conductors.size();i++)if(!conductors.get(i).isEmpty())n+=10;return n;}
    protected int fillHeatBuffer(int space){
        if(inputTank.isEmpty())return 0;var recipe=Recipes.liquidCooldownManager.getHeatExchangeProperty(inputTank.getFluid().getFluid());if(recipe==null)return 0;
        int amount=Math.min(inputTank.getFluidAmount(),Math.min(outputTank.getCapacity()-outputTank.getFluidAmount(),space/recipe.huPerMB));
        if(amount<=0)return 0;var output=new FluidStack(recipe.outputFluid,amount);if(outputTank.fill(output,FluidAction.SIMULATE)!=amount)return 0;
        inputTank.drain(amount,FluidAction.EXECUTE);outputTank.fill(output,FluidAction.EXECUTE);return amount*recipe.huPerMB;
    }
    protected void updateEntityServer(){FluidMachine.container(inputSlot,emptySlot,inputTank,false);FluidMachine.container(containerSlot,outputSlot,outputTank,true);super.updateEntityServer();setActive(getHeatBuffer()>0);for(int i=0;i<upgrades.size();i++){var u=upgrades.get(i);if(!u.isEmpty()&&u.getItem() instanceof IUpgradeItem item&&item.onTick(u,this))setChanged();}}
    @Override public ContainerBase<?> getGuiContainer(int id,Inventory i){return new ic2.core.block.machine.container.ContainerThermalFluids.Exchanger(id,i,this);}
    @Override public void readFromNBT(CompoundTag n,HolderLookup.Provider r){super.readFromNBT(n,r);inputTank.readFromNBT(r,n.getCompound("inputTank"));outputTank.readFromNBT(r,n.getCompound("outputTank"));}
    @Override public void writeToNBT(CompoundTag n,HolderLookup.Provider r){super.writeToNBT(n,r);n.put("inputTank",inputTank.writeToNBT(r,new CompoundTag()));n.put("outputTank",outputTank.writeToNBT(r,new CompoundTag()));}
    public double getEnergy(){return 40;}public boolean useEnergy(double n){return true;}
    public Set<UpgradableProperty> getUpgradableProperties(){return EnumSet.of(UpgradableProperty.ItemConsuming,UpgradableProperty.ItemProducing,UpgradableProperty.FluidConsuming,UpgradableProperty.FluidProducing);}
}
