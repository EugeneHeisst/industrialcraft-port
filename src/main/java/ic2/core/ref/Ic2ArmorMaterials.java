package ic2.core.ref;

import ic2.core.IC2;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Armor materials; the layer name selects textures/models/armor/<name>_layer_1/2.png (the original textures/armor/<name>_1/2.png). */
public final class Ic2ArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> REGISTRY = DeferredRegister.create(Registries.ARMOR_MATERIAL, IC2.MODID);

    public static final Holder<ArmorMaterial> BRONZE = reg("bronze", new int[]{2, 5, 6, 2}, 9, () -> Ingredient.of(Ic2Items.get("bronze_ingot")));
    public static final Holder<ArmorMaterial> ALLOY = reg("alloy", new int[]{4, 7, 9, 4}, 12, () -> Ingredient.of(Ic2Items.get("advanced_alloy")));
    public static final Holder<ArmorMaterial> NANO = reg("nano", new int[]{0, 0, 0, 0}, 0, () -> Ingredient.EMPTY);
    public static final Holder<ArmorMaterial> QUANTUM = reg("quantum", new int[]{0, 0, 0, 0}, 0, () -> Ingredient.EMPTY);
    public static final Holder<ArmorMaterial> HAZMAT = reg("hazmat", new int[]{0, 0, 0, 0}, 0, () -> Ingredient.of(Ic2Items.get("rubber")));
    public static final Holder<ArmorMaterial> RUBBER = reg("rubber", new int[]{0, 0, 0, 0}, 0, () -> Ingredient.of(Ic2Items.get("rubber")));
    public static final Holder<ArmorMaterial> BATPACK = reg("batpack", new int[]{0, 0, 0, 0}, 0, () -> Ingredient.EMPTY);
    public static final Holder<ArmorMaterial> ADVBATPACK = reg("advbatpack", new int[]{0, 0, 0, 0}, 0, () -> Ingredient.EMPTY);
    public static final Holder<ArmorMaterial> ENERGYPACK = reg("energypack", new int[]{0, 0, 0, 0}, 0, () -> Ingredient.EMPTY);
    public static final Holder<ArmorMaterial> LAPPACK = reg("lappack", new int[]{0, 0, 0, 0}, 0, () -> Ingredient.EMPTY);
    public static final Holder<ArmorMaterial> JETPACK = reg("jetpack", new int[]{0, 0, 0, 0}, 0, () -> Ingredient.EMPTY);
    public static final Holder<ArmorMaterial> NIGHTVISION = reg("nightvision", new int[]{0, 0, 0, 0}, 0, () -> Ingredient.EMPTY);
    public static final Holder<ArmorMaterial> SOLAR = reg("solar", new int[]{0, 0, 0, 0}, 0, () -> Ingredient.EMPTY);

    /** defense in boots, legs, chest, helmet order */
    private static Holder<ArmorMaterial> reg(String name, int[] defense, int enchantability, Supplier<Ingredient> repair) {
        var map = new EnumMap<ArmorItem.Type, Integer>(ArmorItem.Type.class);
        map.put(ArmorItem.Type.BOOTS, defense[0]);
        map.put(ArmorItem.Type.LEGGINGS, defense[1]);
        map.put(ArmorItem.Type.CHESTPLATE, defense[2]);
        map.put(ArmorItem.Type.HELMET, defense[3]);
        map.put(ArmorItem.Type.BODY, defense[2]);
        return REGISTRY.register(name, () -> new ArmorMaterial(map, enchantability, SoundEvents.ARMOR_EQUIP_IRON, repair, List.of(new ArmorMaterial.Layer(IC2.id(name))), 0, 0));
    }
    private Ic2ArmorMaterials() {}
}
