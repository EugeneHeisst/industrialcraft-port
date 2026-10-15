package ic2.core.ref;

import ic2.core.IC2;
import ic2.core.item.ItemBattery;
import ic2.core.item.ItemBatterySU;
import ic2.core.item.ItemUpgradeModule;
import ic2.core.item.tool.ItemToolWrench;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2Items {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(IC2.MODID);
    /** Plain crafting items generated from tools/content.py (ic2/content/simple_items.txt). */
    public static final Map<String, DeferredItem<Item>> SIMPLE = new LinkedHashMap<>();

    static {
        var in = Ic2Items.class.getResourceAsStream("/ic2/content/simple_items.txt");
        if (in != null) try (var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) SIMPLE.put(line, REGISTRY.registerSimpleItem(line));
            }
        } catch (java.io.IOException e) { throw new RuntimeException(e); }
        for (var block : Ic2Blocks.REGISTRY.getEntries()) REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static final DeferredItem<ItemToolWrench> WRENCH = REGISTRY.register("wrench", () -> new ItemToolWrench(new Item.Properties().durability(160)));
    public static final DeferredItem<ItemBattery> RE_BATTERY = REGISTRY.register("re_battery", () -> new ItemBattery(new Item.Properties().stacksTo(16), 10000, 100, 1, true));
    public static final DeferredItem<ItemBattery> ADVANCED_RE_BATTERY = REGISTRY.register("advanced_re_battery", () -> new ItemBattery(new Item.Properties().stacksTo(1), 100000, 256, 2, false));
    public static final DeferredItem<ItemBattery> ENERGY_CRYSTAL = REGISTRY.register("energy_crystal", () -> new ItemBattery(new Item.Properties().stacksTo(1), 1000000, 2048, 3, false));
    public static final DeferredItem<ItemBattery> LAPOTRON_CRYSTAL = REGISTRY.register("lapotron_crystal", () -> new ItemBattery(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), 1e7, 8092, 4, false));
    public static final DeferredItem<ItemBatterySU> SU_BATTERY = REGISTRY.register("single_use_battery", () -> new ItemBatterySU(new Item.Properties(), 1200, 1));
    public static final DeferredItem<ItemUpgradeModule> OVERCLOCKER_UPGRADE = upgrade("overclocker_upgrade", ItemUpgradeModule.Type.Overclocker);
    public static final DeferredItem<ItemUpgradeModule> TRANSFORMER_UPGRADE = upgrade("transformer_upgrade", ItemUpgradeModule.Type.Transformer);
    public static final DeferredItem<ItemUpgradeModule> ENERGY_STORAGE_UPGRADE = upgrade("energy_storage_upgrade", ItemUpgradeModule.Type.EnergyStorage);
    public static final DeferredItem<ItemUpgradeModule> EJECTOR_UPGRADE = upgrade("ejector_upgrade", ItemUpgradeModule.Type.Ejector);
    public static final DeferredItem<ItemUpgradeModule> FLUID_EJECTOR_UPGRADE = upgrade("fluid_ejector_upgrade", ItemUpgradeModule.Type.FluidEjector);
    public static final DeferredItem<ItemUpgradeModule> REDSTONE_INVERTER_UPGRADE = upgrade("redstone_inverter_upgrade", ItemUpgradeModule.Type.RedstoneInverter);
    public static final DeferredItem<ItemUpgradeModule> PULLING_UPGRADE = upgrade("pulling_upgrade", ItemUpgradeModule.Type.Pulling);

    private static DeferredItem<ItemUpgradeModule> upgrade(String name, ItemUpgradeModule.Type type) { return REGISTRY.register(name, () -> new ItemUpgradeModule(new Item.Properties(), type)); }

    /** Item of a simple or block id of this mod. */
    public static Item get(String id) { var d = SIMPLE.get(id); return d != null ? d.get() : BuiltInRegistries.ITEM.get(IC2.id(id)); }

    private Ic2Items() {}
}
