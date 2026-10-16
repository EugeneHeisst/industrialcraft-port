package ic2.core.item.armor;
import ic2.core.fluid.Ic2Fluids;
import ic2.core.ref.Ic2ArmorMaterials;
import ic2.core.util.Keyboard;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
public class ItemArmorJetpack extends ItemArmorFluidTank {
    public ItemArmorJetpack(Properties p){super(Ic2ArmorMaterials.JETPACK,p,Ic2Fluids.BIOGAS::get,30000);}
    @Override public void onArmorTick(Level level,Player p,ItemStack s){var tag=ArmorUtil.tag(s);boolean hover=tag.getBoolean("hoverMode");int timer=tag.getInt("toggleTimer");if(Keyboard.isJumpKeyDown(p)&&Keyboard.isModeSwitchKeyDown(p)&&timer<=0){timer=10;hover=!hover;if(!level.isClientSide){tag.putBoolean("hoverMode",hover);p.displayClientMessage(Component.translatable("ic2.jetpack.hover."+(hover?"enabled":"disabled")),false);}}if(Keyboard.isJumpKeyDown(p)||hover&&p.getDeltaMovement().y<-.03)useJetpack(p,hover);if(!level.isClientSide){tag.putInt("toggleTimer",Math.max(0,timer-1));ArmorUtil.save(s,tag);}}
    public boolean drainfromJetpack(ItemStack s,int amount){if(amount<0||drain(s,amount,false).getAmount()<amount)return false;drain(s,amount,true);return true;}
    public boolean useJetpack(Player p,boolean hover){var s=p.getItemBySlot(EquipmentSlot.CHEST);if(!s.is(this))return false;var fuel=getFluid(s);int amount=fuel.getAmount();if(amount<=0||!fuel.is(Ic2Fluids.BIOGAS.get()))return false;
        float power=Math.min(1,amount/6000f);if(Keyboard.isForwardKeyDown(p))p.moveRelative(.02f,new Vec3(0,0,.4f*power*(hover?1:.15f)*2));
        int ceiling=p.level().getMaxBuildHeight();power*=Math.max(0,Math.min(1,(ceiling-p.getY())/25f));var movement=p.getDeltaMovement();double up=Math.min(movement.y+power*.2f,.6);
        if(hover){double max=Keyboard.isJumpKeyDown(p)?.2:p.isShiftKeyDown()?-.2:0;if(up>max)up=Math.max(max,movement.y);}
        p.setDeltaMovement(movement.x,up,movement.z);if(!p.level().isClientSide&&!p.onGround())drain(s,hover?1:2,true);p.fallDistance=0;ic2.core.util.Util.resetPlayerInAirTime(p);return true;
    }
}
