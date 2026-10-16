package ic2.core.block.machine.container;
import ic2.core.block.machine.tileentity.TileEntityAdvMiner;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;
public class ContainerAdvMiner extends ContainerElectricMachine<TileEntityAdvMiner>{
    public ContainerAdvMiner(int id,Inventory i,TileEntityAdvMiner b){super(id,i,b,203,8,80);addSlot(new SlotInvSlot(b.scannerSlot,0,8,26));for(int n=0;n<4;n++)addSlot(new SlotInvSlot(b.upgradeSlot,n,152,26+n*18));for(int n=0;n<15;n++)addSlot(new SlotInvSlot(b.ListSlot,n,36+n%5*18,44+n/5*18));sync(()->b.minelayer,v->b.minelayer=(int)v);sync(()->b.xcounter,v->b.xcounter=(int)v);sync(()->b.zcounter,v->b.zcounter=(int)v);sync(()->b.blacklist?1:0,v->b.blacklist=v!=0);sync(()->b.silktouch?1:0,v->b.silktouch=v!=0);}
    @Override public void onContainerEvent(String s){try{base.event(Integer.parseInt(s));}catch(NumberFormatException ignored){}}
}
