package ic2.core.block.machine.container;
import ic2.core.*;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.*;
import ic2.core.fluid.FluidMachine;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;
public abstract class ContainerFluidMachines<T extends TileEntityInventory & FluidMachine> extends ContainerBase<T>{
    public final FluidStack[] fluids;
    private final int[] ids;
    protected ContainerFluidMachines(int id,Inventory inv,T b){this(id,inv,b,184);}
    protected ContainerFluidMachines(int id,Inventory inv,T b,int height){this(id,inv,b,height,true);}
    protected ContainerFluidMachines(int id,Inventory inv,T b,int height,boolean inventory){
        super(id,inv,b);if(inventory)addPlayerInventorySlots(inv,height);fluids=new FluidStack[b.getTanks()];ids=new int[fluids.length];java.util.Arrays.fill(fluids,FluidStack.EMPTY);
        for(int i=0;i<fluids.length;i++){final int n=i;
            sync(()->BuiltInRegistries.FLUID.getId(b.getFluidInTank(n).getFluid()),v->ids[n]=(int)v);
            sync(()->b.getFluidInTank(n).getAmount(),v->fluids[n]=(int)v<=0?FluidStack.EMPTY:new FluidStack(BuiltInRegistries.FLUID.byId(ids[n]),(int)v));
        }
        if(b instanceof TileEntityElectricMachine e)sync(()->e.energy,v->e.energy=v);
    }
    protected void slot(InvSlot s,int index,int x,int y){addSlot(new SlotInvSlot(s,index,x,y));}
    public static class Distributor extends ContainerFluidMachines<TileEntityFluidDistributor>{
        public Distributor(int id,Inventory i,TileEntityFluidDistributor b){super(id,i,b);slot(b.inputSlot,0,9,54);slot(b.outputSlot,0,9,72);}
        @Override public void onContainerEvent(String e){if(e.equals("toggle")){base.setActive(!base.getActive());base.setChanged();}}
    }
    public static class Regulator extends ContainerFluidMachines<TileEntityFluidRegulator>{
        public Regulator(int id,Inventory i,TileEntityFluidRegulator b){super(id,i,b);slot(b.dischargeSlot,0,8,57);slot(b.inputSlot,0,58,53);slot(b.outputSlot,0,58,71);sync(()->b.outputmb,v->b.outputmb=(int)v);sync(()->b.mode,v->b.mode=(int)v);}
        @Override public void onContainerEvent(String e){try{base.event(Integer.parseInt(e));}catch(NumberFormatException ignored){}}
    }
    public static class Condenser extends ContainerFluidMachines<TileEntityCondenser>{
        public Condenser(int id,Inventory i,TileEntityCondenser b){super(id,i,b);slot(b.dischargeSlot,0,8,44);slot(b.containerSlot,0,26,73);slot(b.outputSlot,0,134,73);slot(b.upgrades,0,152,73);
            for(int n=0;n<4;n++)slot(b.vents,n,26+n%2*108,26+n/2*18);sync(()->b.progress,v->b.progress=(int)v);
        }
    }
    public static class Solar extends ContainerFluidMachines<TileEntitySolarDistiller>{
        public Solar(int id,Inventory i,TileEntitySolarDistiller b){super(id,i,b);slot(b.inputSlot,0,17,27);slot(b.emptySlot,0,17,45);slot(b.containerSlot,0,136,64);slot(b.outputSlot,0,136,82);for(int n=0;n<3;n++)slot(b.upgrades,n,152,8+n*18);}
    }
}
