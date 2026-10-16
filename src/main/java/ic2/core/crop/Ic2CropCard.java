package ic2.core.crop;
import ic2.api.crops.CropCard;
public abstract class Ic2CropCard extends CropCard {
    @Override public String owner(){return "IC2";}
    @Override public String displayName(){return "ic2.crop."+name();}
    @Override public String discoveredBy(){return "IC2 Team";}
    @Override public net.minecraft.resources.ResourceLocation texture(int size){return ic2.core.IC2.id("block/crop/"+name().toLowerCase(java.util.Locale.ROOT)+"."+Math.max(1,Math.min(size,maxSize())));}
}
