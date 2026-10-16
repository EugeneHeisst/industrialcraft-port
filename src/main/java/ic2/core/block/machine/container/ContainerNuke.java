package ic2.core.block.machine.container;
import ic2.core.ContainerBase;
import ic2.core.block.machine.tileentity.TileEntityNuke;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;
/** The outer ring is eight views of the same 64-item explosive charge, as in original IC2. */
public class ContainerNuke extends ContainerBase<TileEntityNuke>{
    public ContainerNuke(int id,Inventory i,TileEntityNuke b){super(id,i,b);addPlayerInventorySlots(i,219);addSlot(new SlotInvSlot(b.insideSlot,0,79,62));for(int[] pos:new int[][]{{52,8},{106,8},{26,35},{133,35},{26,89},{133,89},{52,116},{106,116}})addSlot(new SlotInvSlot(b.outsideSlot,0,pos[0],pos[1]));}
}
