package ic2.core.block.machine.container;
import ic2.core.block.TileEntityHeatSourceInventory;
import ic2.core.block.heatgenerator.TileEntityFluidHeatGenerator;
import ic2.core.block.machine.tileentity.*;
import net.minecraft.world.entity.player.Inventory;
public final class ContainerThermalFluids {
    private static void heat(ContainerFluidMachines<?> c,TileEntityHeatSourceInventory b){c.sync(()->b.transmitHeat,v->b.transmitHeat=(int)v);c.sync(()->b.maxHeatEmitpeerTick,v->b.maxHeatEmitpeerTick=(int)v);}
    public static class Generator extends ContainerFluidMachines<TileEntityFluidHeatGenerator>{
        public Generator(int id,Inventory i,TileEntityFluidHeatGenerator b){super(id,i,b,166);slot(b.inputSlot,0,27,21);slot(b.outputSlot,0,27,54);heat(this,b);}
    }
    public static class Exchanger extends ContainerFluidMachines<TileEntityLiquidHeatExchanger>{
        public Exchanger(int id,Inventory i,TileEntityLiquidHeatExchanger b){super(id,i,b,204);slot(b.inputSlot,0,8,103);slot(b.emptySlot,0,26,103);slot(b.containerSlot,0,134,103);slot(b.outputSlot,0,152,103);
            for(int n=0;n<3;n++)slot(b.upgrades,n,62+n*18,103);for(int n=0;n<10;n++)slot(b.conductors,n,46+n%5*17,50+n/5*22);heat(this,b);
        }
    }
    public static class Fermenter extends ContainerFluidMachines<TileEntityFermenter>{
        public Fermenter(int id,Inventory i,TileEntityFermenter b){super(id,i,b);slot(b.inputSlot,0,14,46);slot(b.emptySlot,0,14,64);slot(b.containerSlot,0,148,43);slot(b.gasSlot,0,148,61);slot(b.fertilizerSlot,0,86,83);for(int n=0;n<2;n++)slot(b.upgrades,n,125+n*18,83);sync(()->b.heatbuffer,v->b.heatbuffer=(int)v);sync(()->b.progress,v->b.progress=(int)v);}
    }
}
