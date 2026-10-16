package ic2.core.block.machine.container;
import ic2.core.block.kineticgenerator.TileEntityStirlingKineticGenerator;
import net.minecraft.world.entity.player.Inventory;
public class ContainerStirlingKineticGenerator extends ContainerFluidMachines<TileEntityStirlingKineticGenerator>{
    public ContainerStirlingKineticGenerator(int id,Inventory inv,TileEntityStirlingKineticGenerator b){super(id,inv,b,204);slot(b.inputSlot,0,8,103);slot(b.emptySlot,0,26,103);slot(b.containerSlot,0,134,103);slot(b.outputSlot,0,152,103);for(int n=0;n<3;n++)slot(b.upgrades,n,62+n*18,103);sync(()->b.heatbuffer,v->b.heatbuffer=(int)v);sync(()->b.kUBuffer,v->b.kUBuffer=(int)v);}
}
