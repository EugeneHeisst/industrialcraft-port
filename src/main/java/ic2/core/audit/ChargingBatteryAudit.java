package ic2.core.audit;
import ic2.api.item.ElectricItem;
import ic2.core.item.ItemBatteryChargeHotbar;
import ic2.core.item.ItemBatteryChargeHotbar.Mode;
import ic2.core.ref.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
public final class ChargingBatteryAudit {
    private static double charge(ItemStack s){return ElectricItem.manager.getCharge(s);}
    public static void run(ServerLevel l,CropAudit.Check check){
        var p=FakePlayerFactory.getMinecraft(l);p.getInventory().clearContent();var battery=Ic2Items.CHARGING_RE_BATTERY.get();var source=battery.charged();p.getInventory().setItem(0,source);var first=new ItemStack(Ic2Items.OD_SCANNER.get());var second=new ItemStack(Ic2Items.OD_SCANNER.get());p.getInventory().setItem(1,first);p.getInventory().setItem(2,second);
        double moved=battery.chargeHotbar(source,p);check.test("charging battery shared tick budget conserves EU",moved==128&&charge(source)==39872&&charge(first)==128&&charge(second)==0,"source="+charge(source)+" target="+charge(first));
        battery.setMode(source,Mode.DISABLED);double before=charge(source);check.test("charging battery disabled mode and toolbox admission",battery.chargeHotbar(source,p)==0&&charge(source)==before&&battery.canBeStoredInToolbox(source),"disabled");
        battery.setMode(source,Mode.NOT_IN_HAND);p.getInventory().selected=1;double firstCharge=charge(first);battery.chargeHotbar(source,p);check.test("charging battery not in hand skips selected target",charge(first)==firstCharge&&charge(second)==128&&!battery.canBeStoredInToolbox(source),"not in hand");
        battery.setMode(source,Mode.ENABLED);p.getInventory().setItem(1,new ItemStack(Ic2Items.OV_SCANNER.get()));p.getInventory().setItem(2,new ItemStack(Ic2Items.OD_SCANNER.get()));battery.chargeHotbar(source,p);check.test("charging battery respects target tier",charge(p.getInventory().getItem(1))==0&&charge(p.getInventory().getItem(2))==128,"tier1 vs tier2");
        var other=Ic2Items.CHARGING_ADVANCED_BATTERY.get().charged();p.getInventory().setItem(1,other);double otherCharge=charge(other);battery.chargeHotbar(source,p);check.test("charging batteries cannot charge each other",charge(other)==otherCharge,charge(other));
        var outside=new ItemStack(Ic2Items.OD_SCANNER.get());p.getInventory().setItem(9,outside);battery.chargeHotbar(source,p);check.test("charging battery affects hotbar only",charge(outside)==0,charge(outside));
        ElectricItem.manager.discharge(source,charge(source)-50,Integer.MAX_VALUE,true,false,false);p.getInventory().setItem(1,new ItemStack(Ic2Items.OD_SCANNER.get()));p.getInventory().setItem(2,ItemStack.EMPTY);check.test("charging battery partial final charge cannot create EU",battery.chargeHotbar(source,p)==50&&charge(source)==0&&charge(p.getInventory().getItem(1))==50,charge(source));
        p.setItemInHand(InteractionHand.MAIN_HAND,source);battery.setMode(source,Mode.ENABLED);battery.use(l,p,InteractionHand.MAIN_HAND);boolean disabled=battery.getMode(source)==Mode.DISABLED;battery.use(l,p,InteractionHand.MAIN_HAND);boolean skip=battery.getMode(source)==Mode.NOT_IN_HAND;battery.use(l,p,InteractionHand.MAIN_HAND);check.test("charging battery right click cycles all three modes",disabled&&skip&&battery.getMode(source)==Mode.ENABLED,battery.getMode(source));
        battery.setMode(source,Mode.NOT_IN_HAND);ElectricItem.manager.charge(source,1234,1,true,false);var saved=ItemStack.parse(l.registryAccess(),source.save(l.registryAccess())).orElseThrow();check.test("charging battery charge and mode persist",charge(saved)==1234&&battery.getMode(saved)==Mode.NOT_IN_HAND,charge(saved));
        battery.setMode(source,Mode.ENABLED);p.getInventory().setItem(1,new ItemStack(Ic2Items.OD_SCANNER.get()));long time=l.getGameTime();try{l.getServer().getWorldData().overworldData().setGameTime(1000);before=charge(source);battery.inventoryTick(source,l,p,0,true);boolean idle=charge(source)==before;l.getServer().getWorldData().overworldData().setGameTime(1001);battery.inventoryTick(source,l,p,0,true);check.test("charging battery automatic ten tick scheduling",idle&&charge(source)==before-128,"period10 phase1");}finally{l.getServer().getWorldData().overworldData().setGameTime(time);}
        boolean recipes=true;int n=0;for(var item:new ItemBatteryChargeHotbar[]{Ic2Items.CHARGING_RE_BATTERY.get(),Ic2Items.CHARGING_ADVANCED_BATTERY.get(),Ic2Items.CHARGING_ENERGY_CRYSTAL.get(),Ic2Items.CHARGING_LAPOTRON_CRYSTAL.get()}){recipes&=l.getRecipeManager().getRecipes().stream().anyMatch(r->r.value().getResultItem(l.registryAccess()).is(item));check.test("charging battery tier "+(++n)+" capacity and transfer",item.getMaxCharge(new ItemStack(item))==new double[]{40000,400000,4000000,40000000}[n-1]&&item.getTransferLimit(new ItemStack(item))==new double[]{128,1024,8192,32768}[n-1]&&item.getTier(new ItemStack(item))==n,item.getMaxCharge(new ItemStack(item)));}check.test("all four charging battery original recipes",recipes,"recipes");p.getInventory().clearContent();p.getInventory().selected=0;
    }
}
