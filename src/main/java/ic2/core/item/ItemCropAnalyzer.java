package ic2.core.item;

import ic2.api.crops.*;
import ic2.api.item.ElectricItem;
import ic2.core.ContainerBase;
import ic2.core.ref.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;

/** Four scan stages, original 10/90/900/9000 EU costs; analyzed seed stays in the output slot. */
public class ItemCropAnalyzer extends BaseElectricItem implements IHandHeldInventory {
    public ItemCropAnalyzer(Properties p){super(p,100000,128,2);}
    @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){
        if(p instanceof ServerPlayer sp)IHandHeldInventory.open(sp,h,id->createContainer(id,sp.getInventory(),sp.getItemInHand(h),h));
        return InteractionResultHolder.sidedSuccess(p.getItemInHand(h),l.isClientSide);
    }
    @Override public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext ctx){
        if(ctx.getPlayer()!=null&&!ctx.getPlayer().isShiftKeyDown()&&ctx.getLevel().getBlockEntity(ctx.getClickedPos()) instanceof ic2.core.crop.TileEntityCrop c&&c.getCrop()!=null){
            if(!ctx.getLevel().isClientSide&&ElectricItem.manager.use(ctx.getItemInHand(),900,ctx.getPlayer())){
                var p=ctx.getPlayer();p.displayClientMessage(net.minecraft.network.chat.Component.translatable("ic2.crop."+c.getCrop().name()),false);
                p.displayClientMessage(net.minecraft.network.chat.Component.translatable("ic2.crop.stats",c.statGrowth,c.statGain,c.statResistance),false);
                p.displayClientMessage(net.minecraft.network.chat.Component.translatable("ic2.crop.environment",c.nutrientStorage,c.waterStorage,c.exStorage,c.getHumidity(),c.getNutrients(),c.getAirQuality()),false);
            }
            return InteractionResult.sidedSuccess(ctx.getLevel().isClientSide);
        }
        return InteractionResult.PASS;
    }
    @Override public ContainerBase<?> createContainer(int id,Inventory inv,ItemStack s,InteractionHand h){return new Container(id,inv,new HandHeldInventory(inv.player,h,3));}
    public static class Container extends ContainerBase<HandHeldInventory>{
        public Container(int id,Inventory inv,HandHeldInventory h){
            super(Ic2Menus.HANDHELD.get(),id,inv,h);
            addSlot(new Slot(h,0,8,7){public boolean mayPlace(ItemStack s){return s.getItem() instanceof ItemCropSeed;}});
            addSlot(new Slot(h,1,41,7){public boolean mayPlace(ItemStack s){return false;}});
            addSlot(new Slot(h,2,152,7){public boolean mayPlace(ItemStack s){return s.getItem() instanceof ic2.api.item.IElectricItem && !(s.getItem() instanceof ItemCropAnalyzer);}});
            addPlayerInventorySlots(inv,176,223);
        }
        @Override public void broadcastChanges(){
            if(!player.level().isClientSide){
                var tool=base.holder();if(tool.isEmpty()){super.broadcastChanges();return;}var battery=base.getItem(2);
                double missing=100000-ElectricItem.manager.getCharge(tool);
                double can=ElectricItem.manager.discharge(battery,Math.min(128,missing),2,true,true,true);
                double accepted=ElectricItem.manager.charge(tool,can,2,true,true);
                if(accepted>0){ElectricItem.manager.discharge(battery,accepted,2,true,true,false);ElectricItem.manager.charge(tool,accepted,2,true,false);base.setChanged();}
                var input=base.getItem(0);var n=input.get(Ic2Components.CROP_DATA.get());
                if(base.getItem(1).isEmpty() && n!=null && Crops.instance.getCropCard(input)!=null){
                    int stage=Math.max(0,Math.min(4,n.getByte("scan")));int cost=stage==4?0:new int[]{10,90,900,9000}[stage];
                    if(cost==0||ElectricItem.manager.use(tool,cost,player)){
                        var result=input.copy();var data=n.copy();data.putByte("scan",(byte)Math.min(4,stage+1));result.set(Ic2Components.CROP_DATA.get(),data);
                        base.setItem(1,result);base.setItem(0,ItemStack.EMPTY);base.setChanged();
                    }
                }
            }
            super.broadcastChanges();
        }
        @Override public void removed(Player p){super.removed(p);base.setChanged();}
    }
}
