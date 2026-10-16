package ic2.core.block.machine.container;
import ic2.core.ContainerBase;
import ic2.core.block.machine.tileentity.TileEntityLathe;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;
public class ContainerLathe extends ContainerBase<TileEntityLathe>{
    public ContainerLathe(int id,Inventory i,TileEntityLathe b){super(id,i,b);addPlayerInventorySlots(i,166);addSlot(new SlotInvSlot(b.toolSlot,0,10,30));addSlot(new SlotInvSlot(b.latheSlot,0,10,12));addSlot(new SlotInvSlot(b.outputSlot,0,10,57));sync(()->b.kUBuffer,v->b.kUBuffer=(int)v);}
    public void onContainerEvent(String event){try{base.process(Integer.parseInt(event));}catch(NumberFormatException ignored){}}
}
