package ic2.core.block.machine.tileentity;
import ic2.core.*;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.*;
import ic2.core.fluid.*;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.upgrade.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import java.util.*;
public class TileEntitySolarDistiller extends TileEntityInventory implements IHasGui,FluidMachine,IUpgradableBlock {
    public final FluidTank inputTank=new FluidTank(10000,f->f.is(Fluids.WATER)){protected void onContentsChanged(){setChanged();}};
    public final FluidTank outputTank=new FluidTank(10000,f->f.is(Ic2Fluids.DISTILLED_WATER.get())){protected void onContentsChanged(){setChanged();}};
    private final FluidTank[] tanks={inputTank,outputTank};
    public final InvSlotConsumableLiquid inputSlot=FluidMachine.slot(this,"input",false,Fluids.WATER);
    public final InvSlotOutput emptySlot=new InvSlotOutput(this,"empty",1);
    public final InvSlotConsumableLiquid containerSlot=FluidMachine.slot(this,"container",true,Ic2Fluids.DISTILLED_WATER.get());
    public final InvSlotOutput outputSlot=new InvSlotOutput(this,"output",1);
    public final InvSlotUpgrade upgrades=new InvSlotUpgrade(this,"upgrade",3);
    public int ticker;
    public TileEntitySolarDistiller(BlockPos p,BlockState s){super(Ic2BlockEntities.SOLAR_DISTILLER.get(),p,s);}
    public FluidTank[] tanks(){return tanks;}
    public int getTickRate(){float temperature=level.getBiome(worldPosition).value().getBaseTemperature();return temperature>1?36:temperature<0.2f?144:72;}
    public boolean isSunVisible(){
        return level.dimensionType().hasSkyLight()&&level.getChunkAt(worldPosition).getSkyLightSources().getLowestSourceY(worldPosition.getX() & 15,worldPosition.getZ() & 15)<=worldPosition.getY()+1&&level.getSkyDarken()<11&&(!level.isRaining()&&!level.isThundering()||level.getBiome(worldPosition).value().getPrecipitationAt(worldPosition)==net.minecraft.world.level.biome.Biome.Precipitation.NONE);
    }
    @Override protected void updateEntityServer(){
        FluidMachine.container(inputSlot,emptySlot,inputTank,false);setActive(isSunVisible());
        if(++ticker>=getTickRate()){ticker=0;distill();}
        FluidMachine.container(containerSlot,outputSlot,outputTank,true);
        for(int i=0;i<upgrades.size();i++){var u=upgrades.get(i);if(!u.isEmpty()&&u.getItem() instanceof IUpgradeItem upgrade&&upgrade.onTick(u,this))setChanged();}
    }
    public boolean distill(){if(!isSunVisible()||inputTank.isEmpty()||outputTank.getFluidAmount()>=outputTank.getCapacity())return false;inputTank.drain(1,FluidAction.EXECUTE);outputTank.fill(new FluidStack(Ic2Fluids.DISTILLED_WATER.get(),1),FluidAction.EXECUTE);return true;}
    @Override public ContainerBase<?> getGuiContainer(int id,Inventory i){return new ic2.core.block.machine.container.ContainerFluidMachines.Solar(id,i,this);}
    @Override public void readFromNBT(CompoundTag n,HolderLookup.Provider r){super.readFromNBT(n,r);inputTank.readFromNBT(r,n.getCompound("inputTank"));outputTank.readFromNBT(r,n.getCompound("outputTank"));ticker=Math.max(0,n.getInt("ticker"));}
    @Override public void writeToNBT(CompoundTag n,HolderLookup.Provider r){super.writeToNBT(n,r);n.put("inputTank",inputTank.writeToNBT(r,new CompoundTag()));n.put("outputTank",outputTank.writeToNBT(r,new CompoundTag()));n.putInt("ticker",ticker);}
    public double getEnergy(){return 40;}public boolean useEnergy(double n){return true;}
    public Set<UpgradableProperty> getUpgradableProperties(){return EnumSet.of(UpgradableProperty.ItemConsuming,UpgradableProperty.ItemProducing,UpgradableProperty.FluidProducing);}
}
