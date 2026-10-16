package ic2.core.block.machine.tileentity;

import ic2.core.*;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.*;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.upgrade.*;
import net.minecraft.core.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Two isolated 24-slot buffers. Vertical faces access the left bank, horizontal faces the right bank. */
public class TileEntityItemBuffer extends TileEntityInventory implements IHasGui,IUpgradableBlock {
    public final InvSlot rightcontentSlot=new InvSlot(this,"rightcontent",InvSlot.Access.IO,24,InvSlot.InvSide.SIDE);
    public final InvSlot leftcontentSlot=new InvSlot(this,"leftcontent",InvSlot.Access.IO,24,InvSlot.InvSide.NOTSIDE);
    public final InvSlotUpgrade upgradeSlot=new InvSlotUpgrade(this,"upgrade",2);
    private int activeBank=-1;
    private boolean alternate;
    public TileEntityItemBuffer(BlockPos p,BlockState s){super(Ic2BlockEntities.ITEM_BUFFER.get(),p,s);}
    @Override protected void updateEntityServer(){
        super.updateEntityServer();boolean both=!upgradeSlot.get(0).isEmpty()&&!upgradeSlot.get(1).isEmpty();
        for(int i=0;i<2;i++){
            if(both && i!=(alternate?1:0))continue;
            var u=upgradeSlot.get(i);if(u.isEmpty()||!(u.getItem() instanceof IUpgradeItem upgrade))continue;
            activeBank=i;
            try{if(upgrade.onTick(u,this))setChanged();}finally{activeBank=-1;}
        }
        alternate=!alternate;
    }
    private InvSlot bank(Direction d){return activeBank==0?leftcontentSlot:activeBank==1?rightcontentSlot:d.getAxis()==Direction.Axis.Y?leftcontentSlot:rightcontentSlot;}
    @Override public int[] getSlotsForFace(Direction d){int first=getBaseIndex(bank(d));return java.util.stream.IntStream.range(first,first+24).toArray();}
    private boolean inBank(int index,Direction d){int first=getBaseIndex(bank(d));return index>=first&&index<first+24;}
    @Override public boolean canPlaceItemThroughFace(int i,ItemStack s,Direction d){return inBank(i,d);}
    @Override public boolean canTakeItemThroughFace(int i,ItemStack s,Direction d){return inBank(i,d);}
    @Override public ContainerBase<?> getGuiContainer(int id,Inventory i){return new ic2.core.block.machine.container.ContainerLogistics.Buffer(id,i,this);}
    public double getEnergy(){return 40;}public boolean useEnergy(double n){return true;}
    public Set<UpgradableProperty> getUpgradableProperties(){return EnumSet.of(UpgradableProperty.ItemProducing);}
}
