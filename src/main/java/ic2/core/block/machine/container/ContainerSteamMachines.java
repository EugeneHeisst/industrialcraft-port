package ic2.core.block.machine.container;
import ic2.core.block.machine.tileentity.TileEntitySteamGenerator;
import ic2.core.block.kineticgenerator.TileEntitySteamKineticGenerator;
import net.minecraft.world.entity.player.Inventory;
public final class ContainerSteamMachines {
    public static class Boiler extends ContainerFluidMachines<TileEntitySteamGenerator>{
        public Boiler(int id,Inventory i,TileEntitySteamGenerator b){super(id,i,b,220,false);sync(()->b.inputmb,v->b.inputmb=(int)v);sync(()->b.outputmb,v->b.outputmb=(int)v);sync(()->b.pressurevalve,v->b.pressurevalve=(int)v);sync(()->b.systemheat,v->b.systemheat=(float)v);sync(()->b.calcification,v->b.calcification=(int)v);sync(()->b.heatinput,v->b.heatinput=(int)v);}
        public void onContainerEvent(String e){try{base.event(Integer.parseInt(e));}catch(NumberFormatException ignored){}}
    }
    public static class Turbine extends ContainerFluidMachines<TileEntitySteamKineticGenerator>{
        public Turbine(int id,Inventory i,TileEntitySteamKineticGenerator b){super(id,i,b,166);slot(b.turbineSlot,0,80,26);slot(b.upgrades,0,152,26);sync(()->b.kUoutput,v->b.kUoutput=(int)v);}
    }
}
