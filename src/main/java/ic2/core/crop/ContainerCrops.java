package ic2.core.crop;
import ic2.core.ContainerBase;
import ic2.core.block.machine.tileentity.*;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;
public final class ContainerCrops {
    public static class Matron extends ContainerBase<TileEntityCropmatron>{
        public int water;
        public Matron(int id,Inventory i,TileEntityCropmatron b){super(id,i,b);addPlayerInventorySlots(i,191);
            addSlot(new SlotInvSlot(b.dischargeSlot,0,134,80));
            for(int x=0;x<7;x++){addSlot(new SlotInvSlot(b.fertilizerSlot,x,26+x*18,50));addSlot(new SlotInvSlot(b.weedExSlot,x,26+x*18,27));}
            addSlot(new SlotInvSlot(b.waterInput,0,26,71));addSlot(new SlotInvSlot(b.waterOutput,0,26,89));
            sync(()->b.energy,v->b.energy=v);sync(()->b.fluidTank.getFluidAmount(),v->water=(int)v);
        }
    }
    public static class Harvester extends ContainerBase<TileEntityCropHarvester>{
        public Harvester(int id,Inventory i,TileEntityCropHarvester b){super(id,i,b);addPlayerInventorySlots(i,191);
            addSlot(new SlotInvSlot(b.dischargeSlot,0,152,58));
            for(int y=0;y<3;y++)for(int x=0;x<5;x++)addSlot(new SlotInvSlot(b.contentSlot,x+y*5,44+x*18,22+y*18));
            addSlot(new SlotInvSlot(b.upgradeSlot,0,80,80));addSlot(new SlotInvSlot(b.analyzerSlot,0,15,40));sync(()->b.energy,v->b.energy=v);
        }
    }
}
