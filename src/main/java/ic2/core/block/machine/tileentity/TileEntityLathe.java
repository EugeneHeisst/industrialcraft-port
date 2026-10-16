package ic2.core.block.machine.tileentity;
import ic2.api.energy.tile.IKineticSource;
import ic2.api.item.ILatheItem;
import ic2.core.*;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.*;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
public class TileEntityLathe extends TileEntityInventory implements IHasGui {
    public final InvSlot toolSlot=new InvSlot(this,"tool",InvSlot.Access.I,1){public boolean accepts(ItemStack s){return s.getItem() instanceof ILatheItem.ILatheTool;}};
    public final InvSlot latheSlot=new InvSlot(this,"lathe",InvSlot.Access.I,1){public boolean accepts(ItemStack s){return s.getItem() instanceof ILatheItem;}};
    public final InvSlotOutput outputSlot=new InvSlotOutput(this,"dusts",1);
    public int kUBuffer;
    public TileEntityLathe(BlockPos p,BlockState s){super(Ic2BlockEntities.LATHE.get(),p,s);toolSlot.setStackSizeLimit(1);latheSlot.setStackSizeLimit(1);}
    protected void updateEntityServer(){int before=kUBuffer;kUBuffer=Math.max(0,(int)(kUBuffer-kUBuffer/100-0.5f));for(var d:Direction.values()){var be=ic2.core.util.Util.loadedBlockEntity(level,worldPosition.relative(d));if(be instanceof IKineticSource source){int n=Math.min(10000-kUBuffer,source.maxrequestkineticenergyTick(d.getOpposite()));if(n>0){kUBuffer+=source.requestkineticenergy(d.getOpposite(),n);break;}}}setActive(kUBuffer>500);if(kUBuffer!=before)setChanged();}
    public boolean canWork(boolean power){return toolSlot.get().getItem() instanceof ILatheItem.ILatheTool tool&&latheSlot.get().getItem() instanceof ILatheItem item&&(power||kUBuffer>=1000)&&tool.getHardness(toolSlot.get())>item.getHardness(latheSlot.get());}
    public boolean process(int position){
        if(position<0||position>=5||!canWork(false))return false;
        var blank=latheSlot.get();var item=(ILatheItem)blank.getItem();int[] state=item.getCurrentState(blank);if(state.length!=5||state[position]<=1)return false;
        var drop=item.getOutputItem(blank,position);if(drop==null||!outputSlot.canAdd(drop))return false;
        item.setState(blank,position,state[position]-1);if(level.random.nextFloat()<item.getOutputChance(blank,position))outputSlot.add(drop.copy());
        var tool=(ILatheItem.ILatheTool)toolSlot.get().getItem();tool.setCustomDamage(toolSlot.get(),tool.getCustomDamage(toolSlot.get())+1);if(tool.getCustomDamage(toolSlot.get())>=tool.getMaxCustomDamage(toolSlot.get()))toolSlot.put(ItemStack.EMPTY);
        kUBuffer-=1000;setChanged();return true;
    }
    @Override public ContainerBase<?> getGuiContainer(int id,Inventory i){return new ic2.core.block.machine.container.ContainerLathe(id,i,this);}
    @Override public void readFromNBT(CompoundTag n,HolderLookup.Provider r){super.readFromNBT(n,r);kUBuffer=Math.max(0,Math.min(10000,n.getInt("kUBuffer")));}
    @Override public void writeToNBT(CompoundTag n,HolderLookup.Provider r){super.writeToNBT(n,r);n.putInt("kUBuffer",kUBuffer);}
}
