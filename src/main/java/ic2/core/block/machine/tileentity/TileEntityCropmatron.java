package ic2.core.block.machine.tileentity;

import ic2.core.ContainerBase;
import ic2.core.block.invslot.*;
import ic2.core.crop.TileEntityCrop;
import ic2.core.ref.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.fluids.capability.*;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.apache.commons.lang3.mutable.MutableObject;

public class TileEntityCropmatron extends TileEntityElectricMachine implements IFluidHandler, ic2.core.IHasGui {
    public final FluidTank fluidTank=new FluidTank(2000,f->f.is(Fluids.WATER)) {protected void onContentsChanged(){setChanged();}};
    public final InvSlotConsumable fertilizerSlot=new InvSlotConsumable(this,"fertilizer",7){public boolean accepts(ItemStack s){return s.is(Ic2Items.get("fertilizer"));}};
    public final InvSlot weedExSlot=new InvSlot(this,"weed_ex",InvSlot.Access.I,7){public boolean accepts(ItemStack s){return s.is(Ic2Items.WEED_EX.get());}};
    public final InvSlotConsumableLiquid waterInput=new InvSlotConsumableLiquid(this,"water_input",InvSlot.Access.I,1,InvSlot.InvSide.TOP,Fluids.WATER);
    public final InvSlotOutput waterOutput=new InvSlotOutput(this,"water_output",1);
    public int scanX=-5,scanY=-1,scanZ=-5;
    public TileEntityCropmatron(BlockPos p,BlockState s){super(Ic2BlockEntities.CROPMATRON.get(),p,s,1000,1);}
    @Override protected void updateEntityServer(){
        super.updateEntityServer();var result=new MutableObject<ItemStack>();
        if(waterInput.transferToTank(fluidTank,result,true)&&(result.getValue()==null||waterOutput.canAdd(result.getValue()))){waterInput.transferToTank(fluidTank,result,false);if(result.getValue()!=null)waterOutput.add(result.getValue());setChanged();}
        if(energy>=31)scan();
    }
    public void scan(){
        if(++scanX>5){scanX=-5;if(++scanZ>5){scanZ=-5;if(++scanY>1)scanY=-1;}}
        energy--;setChanged();setActive(false);
        if(ic2.core.util.Util.loadedBlockEntity(level,worldPosition.offset(scanX,scanY,scanZ)) instanceof TileEntityCrop c){
            if(!fertilizerSlot.consume(1,true,false).isEmpty()&&c.applyFertilizer(false)){fertilizerSlot.consume(1);energy-=10;setActive(true);}
            int n=Math.min(200-c.waterStorage,fluidTank.getFluidAmount());
            if(n>0){fluidTank.drain(n,FluidAction.EXECUTE);c.waterStorage+=n;c.updateState();energy-=10;setActive(true);}
            for(int i=0;i<weedExSlot.size();i++){var s=weedExSlot.get(i);if(!s.isEmpty()&&c.applyWeedEx(false)){s.setDamageValue(s.getDamageValue()+1);if(s.getDamageValue()>=s.getMaxDamage())weedExSlot.put(i,ItemStack.EMPTY);energy-=10;setActive(true);break;}}
        }
    }
    @Override public ContainerBase<?> getGuiContainer(int id,Inventory i){return new ic2.core.crop.ContainerCrops.Matron(id,i,this);}
    @Override public void readFromNBT(CompoundTag n,HolderLookup.Provider r){super.readFromNBT(n,r);fluidTank.readFromNBT(r,n.getCompound("water"));}
    @Override public void writeToNBT(CompoundTag n,HolderLookup.Provider r){super.writeToNBT(n,r);n.put("water",fluidTank.writeToNBT(r,new CompoundTag()));}
    public int getTanks(){return 1;}public FluidStack getFluidInTank(int t){return fluidTank.getFluid();}public int getTankCapacity(int t){return 2000;}public boolean isFluidValid(int t,FluidStack f){return f.is(Fluids.WATER);}
    public int fill(FluidStack f,FluidAction a){return fluidTank.fill(f,a);}public FluidStack drain(FluidStack f,FluidAction a){return FluidStack.EMPTY;}public FluidStack drain(int n,FluidAction a){return FluidStack.EMPTY;}
}
