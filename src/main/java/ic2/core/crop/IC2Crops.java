package ic2.core.crop;

import ic2.api.crops.*;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2Components;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.biome.Biome;

public final class IC2Crops extends Crops {
    private final Map<String, CropCard> cards = new LinkedHashMap<>();
    private final Map<Item, BaseSeed> seeds = new LinkedHashMap<>();
    private final CropCard[] legacy = new CropCard[256];
    private final Map<TagKey<Biome>, Integer> humidity = new LinkedHashMap<>(), nutrients = new LinkedHashMap<>();
    public static CropCard cropWheat, cropTerraWart, cropNetherWart;

    public static void init() {
        if (Crops.instance != null) return;
        var c = new IC2Crops(); Crops.instance = c;
        weed = new CropWeed(); cropWheat = new CropWheat(); cropTerraWart = new CropTerraWart(); cropNetherWart = new CropNetherWart();
        CropCard[] all = {weed, cropWheat, new CropPumpkin(), new CropMelon(),
            new CropColorFlower("dandelion", new String[]{"Yellow", "Flower"}, 11),
            new CropColorFlower("rose", new String[]{"Red", "Flower", "Rose"}, 1),
            new CropColorFlower("blackthorn", new String[]{"Black", "Flower", "Rose"}, 0),
            new CropColorFlower("tulip", new String[]{"Purple", "Flower", "Tulip"}, 5),
            new CropColorFlower("cyazint", new String[]{"Blue", "Flower"}, 6),
            new CropVenomilia(), new CropReed(), new CropStickreed(), new CropCocoa(), new CropFerru(), new CropAurelia(), new CropRedWheat(),
            cropNetherWart, cropTerraWart, new CropCoffee(), new CropHops(), new CropCarrots(), new CropPotato(), new CropRedMushroom(), new CropBrownMushroom(),
            new CropEating(), new CropCyprium(), new CropStagnium(), new CropPlumbiscus(), new CropShining()};
        for (int i = 0; i < all.length; i++) if (!c.registerCrop(all[i], i)) throw new IllegalStateException("Duplicate crop " + i);
        c.addBiomenutrientsBonus(BiomeTags.IS_JUNGLE, 10); c.addBiomenutrientsBonus(BiomeTags.IS_FOREST, 5); c.addBiomenutrientsBonus(BiomeTags.IS_RIVER, 2);
        c.addBiomenutrientsBonus(tag("c:is_swamp"), 10); c.addBiomenutrientsBonus(tag("c:is_mushroom"), 5);
        Object[][] base = {{Items.WHEAT_SEEDS,1,1},{Items.PUMPKIN_SEEDS,2,1},{Items.MELON_SEEDS,3,1},{Items.NETHER_WART,16,1},
            {Ic2Items.get("terra_wart"),17,1},{Ic2Items.get("coffee_beans"),18,1},{Items.SUGAR_CANE,10,1},{Items.COCOA_BEANS,12,1},
            {Items.POPPY,5,4},{Items.DANDELION,4,4},{Items.CARROT,20,1},{Items.POTATO,21,1},{Items.RED_MUSHROOM,22,4},{Items.BROWN_MUSHROOM,23,4}};
        for (Object[] b : base) {
            int id = (int)b[1], count = (int)b[2];
            c.registerBaseSeed(new ItemStack((Item)b[0], count), all[id], id == 4 || id == 5 ? 4 : 1, id == 10 ? 3 : id == 12 ? 0 : 1, id == 10 || id == 12 ? 0 : 1, id == 10 ? 2 : id == 12 ? 0 : 1);
        }
    }
    private static TagKey<Biome> tag(String id) { return TagKey.create(Registries.BIOME, ResourceLocation.parse(id)); }
    public static Item dye(int legacy) {
        return switch (legacy) { case 0 -> Items.BLACK_DYE; case 1 -> Items.RED_DYE; case 5 -> Items.PURPLE_DYE; case 6 -> Items.CYAN_DYE; case 11 -> Items.YELLOW_DYE; default -> Items.WHITE_DYE; };
    }
    @Override public void addBiomenutrientsBonus(TagKey<Biome> type, int bonus) { nutrients.put(type, bonus); }
    @Override public void addBiomehumidityBonus(TagKey<Biome> type, int bonus) { humidity.put(type, bonus); }
    private int bonus(Holder<Biome> biome, Map<TagKey<Biome>,Integer> map) { int n=0; for(var e:map.entrySet()) if(biome.is(e.getKey())) n=Math.max(n,e.getValue()); return n; }
    @Override public int getHumidityBiomeBonus(Holder<Biome> biome) { return bonus(biome,humidity); }
    @Override public int getNutrientBiomeBonus(Holder<Biome> biome) { return bonus(biome,nutrients); }
    @Override public CropCard getCropCard(String owner,String name) { return cards.get(owner+":"+name); }
    @Override public CropCard getCropCard(ItemStack stack) { var n=stack.get(Ic2Components.CROP_DATA.get()); return n==null?null:getCropCard(n.getString("owner"),n.getString("name")); }
    @Override public Collection<CropCard> getCrops() { return Collections.unmodifiableCollection(cards.values()); }
    @Override public CropCard[] getCropList() { return legacy.clone(); }
    @Override public short registerCrop(CropCard crop) { for(int i=0;i<legacy.length;i++) if(legacy[i]==null) return registerCrop(crop,i)?(short)i:-1; return registerCrop(crop,-1)?(short)-1:-1; }
    @Override public boolean registerCrop(CropCard crop,int id) {
        if(crop==null || cards.containsKey(crop.owner()+":"+crop.name()) || id >= legacy.length || id < -1 || id >= 0 && legacy[id]!=null) return false;
        cards.put(crop.owner()+":"+crop.name(),crop); if(id>=0) legacy[id]=crop; return true;
    }
    @Override public boolean registerBaseSeed(ItemStack stack,CropCard crop,int size,int growth,int gain,int resistance) {
        if(stack.isEmpty() || crop==null || seeds.containsKey(stack.getItem())) return false;
        seeds.put(stack.getItem(),new BaseSeed(crop,size,growth,gain,resistance,stack.getCount())); return true;
    }
    @Override public BaseSeed getBaseSeed(ItemStack stack) { return seeds.get(stack.getItem()); }
    @Override public int getIdFor(CropCard crop) { if(crop!=null) for(int i=0;i<legacy.length;i++) if(legacy[i]==crop) return i; return -1; }
    public static CropCard getCropFromId(int id) { var a=Crops.instance.getCropList(); return id>=0&&id<a.length?a[id]:null; }
}
