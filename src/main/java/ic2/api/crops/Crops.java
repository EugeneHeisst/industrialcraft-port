package ic2.api.crops;

import java.util.Collection;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;

/** Crop registry shared with addons. BiomeDictionary types are modern biome tags. */
public abstract class Crops {
    public static Crops instance;
    public static CropCard weed;
    public abstract void addBiomenutrientsBonus(TagKey<Biome> type, int bonus);
    public abstract void addBiomehumidityBonus(TagKey<Biome> type, int bonus);
    public abstract int getHumidityBiomeBonus(Holder<Biome> biome);
    public abstract int getNutrientBiomeBonus(Holder<Biome> biome);
    public abstract CropCard getCropCard(String owner, String name);
    public abstract CropCard getCropCard(ItemStack stack);
    public abstract Collection<CropCard> getCrops();
    public abstract CropCard[] getCropList();
    public abstract short registerCrop(CropCard crop);
    public abstract boolean registerCrop(CropCard crop, int id);
    public abstract boolean registerBaseSeed(ItemStack stack, CropCard crop, int size, int growth, int gain, int resistance);
    public boolean registerBaseSeed(ItemStack stack, int id, int size, int growth, int gain, int resistance) {
        CropCard[] list = getCropList();
        return id >= 0 && id < list.length && list[id] != null && registerBaseSeed(stack, list[id], size, growth, gain, resistance);
    }
    public abstract BaseSeed getBaseSeed(ItemStack stack);
    public abstract int getIdFor(CropCard crop);
}
