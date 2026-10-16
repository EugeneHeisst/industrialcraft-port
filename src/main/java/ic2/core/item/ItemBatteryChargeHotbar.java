package ic2.core.item;
import ic2.api.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import java.util.*;
public class ItemBatteryChargeHotbar extends ItemBattery implements IBoxable {
    public enum Mode{ENABLED,DISABLED,NOT_IN_HAND}
    public ItemBatteryChargeHotbar(Properties p,double capacity,double transfer,int tier){super(p.stacksTo(1),capacity,transfer,tier,false);}
    public Mode getMode(ItemStack s){int n=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getByte("mode");return n>=0&&n<Mode.values().length?Mode.values()[n]:Mode.ENABLED;}
    public void setMode(ItemStack s,Mode mode){var tag=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();tag.putByte("mode",(byte)mode.ordinal());s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));}
    public boolean canBeStoredInToolbox(ItemStack s){return getMode(s)==Mode.DISABLED;}
    @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand){var s=p.getItemInHand(hand);if(!l.isClientSide){setMode(s,Mode.values()[(getMode(s).ordinal()+1)%3]);p.displayClientMessage(Component.translatable("ic2.tooltip.mode",Component.translatable("ic2.tooltip.mode."+getMode(s).name().toLowerCase(Locale.ROOT))),false);}return InteractionResultHolder.sidedSuccess(s,l.isClientSide);}
    @Override public void inventoryTick(ItemStack s,Level l,Entity e,int slot,boolean selected){if(e instanceof ServerPlayer p&&l.getGameTime()%10==getTier(s))chargeHotbar(s,p);}
    public double chargeHotbar(ItemStack source,Player p){var mode=getMode(source);if(mode==Mode.DISABLED)return 0;double left=getTransferLimit(source),moved=0;int tier=getTier(source);for(int n=0;n<9&&left>0;n++){var target=p.getInventory().items.get(n);if(!(target.getItem() instanceof IElectricItem)||target.getItem() instanceof ItemBatteryChargeHotbar||mode==Mode.NOT_IN_HAND&&n==p.getInventory().selected)continue;double accepted=ElectricItem.manager.charge(target,left,tier,false,true);double taken=ElectricItem.manager.discharge(source,accepted,tier,true,false,false);if(taken<=0)continue;double charged=ElectricItem.manager.charge(target,taken,tier,true,false);if(charged<taken)ElectricItem.manager.charge(source,taken-charged,tier,true,false);left-=charged;moved+=charged;}return moved;}
    @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){super.appendHoverText(s,c,lines,f);lines.add(Component.translatable("ic2.tooltip.mode."+getMode(s).name().toLowerCase(Locale.ROOT)));}
}
