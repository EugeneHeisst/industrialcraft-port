package ic2.core.item;

import ic2.api.crops.*;
import ic2.core.ref.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import java.util.List;

public class ItemCropSeed extends Item {
    public ItemCropSeed(Properties p){super(p.stacksTo(1));}
    public static ItemStack create(CropCard c,int growth,int gain,int resistance,int scan){
        if(c==null)return ItemStack.EMPTY;
        var s=new ItemStack(Ic2Items.CROP_SEED.get());var n=new CompoundTag();
        n.putString("owner",c.owner());n.putString("name",c.name());n.putByte("growth",(byte)Math.max(0,Math.min(31,growth)));n.putByte("gain",(byte)Math.max(0,Math.min(31,gain)));n.putByte("resistance",(byte)Math.max(0,Math.min(31,resistance)));n.putByte("scan",(byte)Math.max(0,Math.min(4,scan)));s.set(Ic2Components.CROP_DATA.get(),n);return s;
    }
    public static ItemStack generateItemStackFromValues(CropCard c,byte g,byte a,byte r,byte scan){return create(c,g,a,r,scan);}
    private static byte stat(ItemStack s,String key){var n=s.get(Ic2Components.CROP_DATA.get());return n==null?-1:n.getByte(key);}
    public static byte getGrowthFromStack(ItemStack s){return stat(s,"growth");}
    public static byte getGainFromStack(ItemStack s){return stat(s,"gain");}
    public static byte getResistanceFromStack(ItemStack s){return stat(s,"resistance");}
    public static byte getScannedFromStack(ItemStack s){return stat(s,"scan");}
    public static void incrementScannedOfStack(ItemStack s){var n=s.get(Ic2Components.CROP_DATA.get());if(n!=null){n=n.copy();n.putByte("scan",(byte)Math.min(4,n.getByte("scan")+1));s.set(Ic2Components.CROP_DATA.get(),n);}}
    @Override public Component getName(ItemStack s){
        var n=s.get(Ic2Components.CROP_DATA.get());var c=Crops.instance==null?null:Crops.instance.getCropCard(s);
        return n!=null&&n.getByte("scan")>0&&c!=null?Component.translatable("ic2.crop.seed.named",Component.translatable("ic2.crop."+c.name())):super.getName(s);
    }
    @Override public void appendHoverText(ItemStack s,TooltipContext ctx,List<Component> lines,TooltipFlag flag){
        var n=s.get(Ic2Components.CROP_DATA.get());var c=Crops.instance==null?null:Crops.instance.getCropCard(s);if(n==null||c==null)return;
        int scan=n.getByte("scan");
        if(scan>=2)lines.add(Component.translatable("ic2.crop.tier",c.tier()));
        if(scan>=3)lines.add(Component.literal(String.join(", ",c.attributes())));
        if(scan>=4)lines.add(Component.translatable("ic2.crop.stats",n.getByte("growth"),n.getByte("gain"),n.getByte("resistance")));
        else lines.add(Component.translatable("ic2.crop.unanalyzed"));
    }
}
