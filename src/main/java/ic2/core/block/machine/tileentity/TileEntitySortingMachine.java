package ic2.core.block.machine.tileentity;

import ic2.core.*;
import ic2.core.block.invslot.*;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.upgrade.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import java.util.*;

/** Six independent seven-item whitelists and an unfiltered default route; 20 EU per transferred item. */
public class TileEntitySortingMachine extends TileEntityElectricMachine implements IHasGui,IUpgradableBlock {
    public final InvSlotUpgrade upgradeSlot=new InvSlotUpgrade(this,"upgrade",3);
    public final InvSlot buffer=new InvSlot(this,"buffer",InvSlot.Access.I,11);
    public final ItemStack[][] filters=new ItemStack[6][7];
    public int defaultRoute;
    public TileEntitySortingMachine(BlockPos p,BlockState s){super(Ic2BlockEntities.SORTING_MACHINE.get(),p,s,100000,1);for(var side:filters)Arrays.fill(side,ItemStack.EMPTY);}
    @Override protected void updateEntityServer(){super.updateEntityServer();routeItems();}
    public void routeItems(){
        boolean moved=false;
        for(int i=0;i<buffer.size();i++){
            var stack=buffer.get(i);if(stack.isEmpty())continue;
            boolean filtered=false;for(var side:filters)for(var f:side)if(matches(f,stack))filtered=true;
            for(var dir:Direction.values()){
                if(energy<20){setActive(moved);return;}
                boolean selected=false;
                if(dir.ordinal()==defaultRoute)selected=!filtered;
                else for(var f:filters[dir.ordinal()])if(matches(f,stack)){selected=true;break;}
                if(!selected || !level.hasChunkAt(worldPosition.relative(dir)))continue;
                var target=level.getCapability(Capabilities.ItemHandler.BLOCK,worldPosition.relative(dir),dir.getOpposite());if(target==null)continue;
                var rest=ItemHandlerHelper.insertItemStacked(target,stack.copyWithCount(1),false);
                if(rest.isEmpty()){stack.shrink(1);if(stack.isEmpty())buffer.put(i,ItemStack.EMPTY);energy-=20;moved=true;setChanged();if(stack.isEmpty())break;}
            }
        }
        setActive(moved);
    }
    private static boolean matches(ItemStack a,ItemStack b){return !a.isEmpty()&&!b.isEmpty()&&ItemStack.isSameItemSameComponents(a,b);}
    public ItemStack[] getFilterSlots(Direction d){return filters[d.ordinal()];}
    public void setFilter(int side,int index,ItemStack stack){if(side<0||side>=6||index<0||index>=7)return;filters[side][index]=stack.isEmpty()?ItemStack.EMPTY:stack.copyWithCount(1);setChanged();}
    @Override public void setChanged(){super.setChanged();if(upgradeSlot!=null){upgradeSlot.onChanged();setTier(1+upgradeSlot.extraTier);}}
    @Override public ContainerBase<?> getGuiContainer(int id,Inventory i){return new ic2.core.block.machine.container.ContainerLogistics.Sorter(id,i,this);}
    @Override public void readFromNBT(CompoundTag n,HolderLookup.Provider r){
        super.readFromNBT(n,r);for(var side:filters)Arrays.fill(side,ItemStack.EMPTY);
        for(var value:n.getList("filters",Tag.TAG_COMPOUND)){var entry=(CompoundTag)value;int i=entry.getInt("index");if(i>=0&&i<42)filters[i/7][i%7]=ItemStack.parseOptional(r,entry.getCompound("stack"));}
        defaultRoute=Math.max(0,Math.min(5,n.getInt("defaultRoute")));
    }
    @Override public void writeToNBT(CompoundTag n,HolderLookup.Provider r){
        super.writeToNBT(n,r);var list=new ListTag();
        for(int i=0;i<42;i++)if(!filters[i/7][i%7].isEmpty()){var entry=new CompoundTag();entry.putInt("index",i);entry.put("stack",filters[i/7][i%7].save(r));list.add(entry);}
        n.put("filters",list);n.putInt("defaultRoute",defaultRoute);
    }
    public double getEnergy(){return energy;}public boolean useEnergy(double n){if(energy<n)return false;energy-=n;return true;}
    public Set<UpgradableProperty> getUpgradableProperties(){return EnumSet.of(UpgradableProperty.Transformer);}
}
