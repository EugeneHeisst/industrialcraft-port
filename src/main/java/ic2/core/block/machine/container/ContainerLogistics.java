package ic2.core.block.machine.container;

import ic2.core.ContainerBase;
import ic2.core.block.machine.tileentity.*;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class ContainerLogistics {
    public static class Buffer extends ContainerBase<TileEntityItemBuffer>{
        public Buffer(int id,Inventory i,TileEntityItemBuffer b){super(id,i,b);addPlayerInventorySlots(i,232);
            for(int y=0;y<6;y++)for(int x=0;x<4;x++){addSlot(new SlotInvSlot(b.leftcontentSlot,x+y*4,8+x*18,18+y*18));addSlot(new SlotInvSlot(b.rightcontentSlot,x+y*4,98+x*18,18+y*18));}
            for(int n=0;n<2;n++)addSlot(new SlotInvSlot(b.upgradeSlot,n,35+n*90,128));
        }
    }
    public static class Sorter extends ContainerBase<TileEntitySortingMachine>{
        private final SimpleContainer ghosts=new SimpleContainer(42);
        public Sorter(int id,Inventory i,TileEntitySortingMachine b){super(id,i,b);addPlayerInventorySlots(i,212,243);
            addSlot(new SlotInvSlot(b.dischargeSlot,0,188,219));
            for(int n=0;n<3;n++)addSlot(new SlotInvSlot(b.upgradeSlot,n,188,161+n*18));
            for(int n=0;n<11;n++)addSlot(new SlotInvSlot(b.buffer,n,8+n*18,141));
            for(int side=0;side<6;side++)for(int n=0;n<7;n++)addSlot(new Ghost(side*7+n,80+n*18,19+side*20));
            sync(()->b.energy,v->b.energy=v);sync(()->b.defaultRoute,v->b.defaultRoute=(int)v);
        }
        private final class Ghost extends Slot{
            Ghost(int n,int x,int y){super(ghosts,n,x,y);}
            public boolean mayPlace(ItemStack s){return false;}public boolean mayPickup(Player p){return false;}
        }
        @Override public void broadcastChanges(){for(int n=0;n<42;n++)ghosts.setItem(n,base.filters[n/7][n%7].copy());super.broadcastChanges();}
        @Override public void clicked(int index,int button,ClickType type,Player p){
            if(index>=0&&index<slots.size()&&slots.get(index) instanceof Ghost slot){
                if(type==ClickType.PICKUP){int n=slot.getContainerSlot();base.setFilter(n/7,n%7,button==1?ItemStack.EMPTY:getCarried());ghosts.setItem(n,base.filters[n/7][n%7].copy());}
                return;
            }
            super.clicked(index,button,type,p);
        }
        @Override public ItemStack quickMoveStack(Player p,int n){return n>=0&&n<slots.size()&&slots.get(n) instanceof Ghost?ItemStack.EMPTY:super.quickMoveStack(p,n);}
        @Override public void onContainerEvent(String event){try{int side=Integer.parseInt(event);if(side>=0&&side<6){base.defaultRoute=side;base.setChanged();}}catch(NumberFormatException ignored){}}
    }
}
