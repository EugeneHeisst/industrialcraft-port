package ic2.core.fluid;

import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.*;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.apache.commons.lang3.mutable.MutableObject;

/** Shared tank ports and transactional container operations for fluid machines. */
public interface FluidMachine extends IFluidHandler {
    FluidTank[] tanks();
    default boolean canInput(Direction side){return true;}
    default boolean canOutput(Direction side){return tanks().length>1;}
    default int getTanks(){return tanks().length;}
    default FluidStack getFluidInTank(int i){return tanks()[i].getFluid();}
    default int getTankCapacity(int i){return tanks()[i].getCapacity();}
    default boolean isFluidValid(int i,FluidStack f){return i==0&&tanks()[0].isFluidValid(f);}
    default int fill(FluidStack f,FluidAction a){return tanks()[0].fill(f,a);}
    default FluidStack drain(int n,FluidAction a){return canOutput(null)?tanks()[tanks().length-1].drain(n,a):FluidStack.EMPTY;}
    default FluidStack drain(FluidStack f,FluidAction a){return canOutput(null)?tanks()[tanks().length-1].drain(f,a):FluidStack.EMPTY;}
    default IFluidHandler port(Direction side){var owner=this;return new IFluidHandler(){
        public int getTanks(){return owner.getTanks();}public FluidStack getFluidInTank(int i){return owner.getFluidInTank(i);}public int getTankCapacity(int i){return owner.getTankCapacity(i);}
        public boolean isFluidValid(int i,FluidStack f){return owner.canInput(side)&&owner.isFluidValid(i,f);}
        public int fill(FluidStack f,FluidAction a){return owner.canInput(side)?owner.fill(f,a):0;}
        public FluidStack drain(int n,FluidAction a){return owner.canOutput(side)?owner.drain(n,a):FluidStack.EMPTY;}
        public FluidStack drain(FluidStack f,FluidAction a){return owner.canOutput(side)?owner.drain(f,a):FluidStack.EMPTY;}
    };}
    static InvSlotConsumableLiquid slot(TileEntityInventory base,String name,boolean fill,Fluid... fluids){
        var slot=new InvSlotConsumableLiquid(base,name,InvSlot.Access.I,1,fill?InvSlot.InvSide.BOTTOM:InvSlot.InvSide.TOP,fluids){
            @Override public boolean accepts(ItemStack s){return !fill&&fluids.length==0?!contained(s).isEmpty():super.accepts(s);}
        };slot.fillMode=fill;return slot;
    }
    static void container(InvSlotConsumableLiquid input,InvSlotOutput output,FluidTank tank,boolean fill){
        var result=new MutableObject<ItemStack>();
        boolean ok=fill?input.transferFromTank(tank,result,true):input.transferToTank(tank,result,true);
        if(ok&&(result.getValue()==null||output.canAdd(result.getValue()))){
            if(fill)input.transferFromTank(tank,result,false);else input.transferToTank(tank,result,false);
            if(result.getValue()!=null)output.add(result.getValue());input.base.setChanged();
        }
    }
    static int transfer(FluidTank source,IFluidHandler target,int limit){
        var offered=source.drain(limit,FluidAction.SIMULATE);if(offered.isEmpty())return 0;
        int accepted=Math.min(offered.getAmount(),target.fill(offered.copy(),FluidAction.SIMULATE));if(accepted<=0)return 0;
        var taken=source.drain(accepted,FluidAction.EXECUTE);int moved=target.fill(taken.copy(),FluidAction.EXECUTE);
        if(moved<taken.getAmount())source.fill(taken.copyWithAmount(taken.getAmount()-moved),FluidAction.EXECUTE);
        return moved;
    }
}
