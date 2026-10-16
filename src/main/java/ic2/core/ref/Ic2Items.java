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
                if (!line.isEmpty() && !line.equals("terra_wart") && !line.equals("fertilizer")) SIMPLE.put(line, REGISTRY.registerSimpleItem(line));
            }
        } catch (java.io.IOException e) { throw new RuntimeException(e); }
        var fluidBlocks = new java.util.HashSet<String>();
        for (var f : ic2.core.fluid.Ic2Fluids.ALL) fluidBlocks.add(f.name);
        fluidBlocks.add("mining_pipe"); fluidBlocks.add("mining_pipe_tip"); fluidBlocks.add("resin_sheet"); fluidBlocks.add("foam_wall"); fluidBlocks.add("dynamite"); fluidBlocks.add("dynamite_remote"); fluidBlocks.add("reinforced_door"); fluidBlocks.add("barrel"); // the pipe item cannot be placed by hand, the tip has no item
        for (var block : Ic2Blocks.REGISTRY.getEntries()) if (!fluidBlocks.contains(block.getId().getPath())) REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), block.getId().getPath().equals("personal_chest") ? new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON) : new Item.Properties()));
    }

    public static final DeferredItem<ic2.core.item.ItemMug> EMPTY_MUG = REGISTRY.register("empty_mug", () -> new ic2.core.item.ItemMug(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.ItemMugCoffee> COLD_COFFEE = REGISTRY.register("cold_coffee", () -> new ic2.core.item.ItemMugCoffee(new Item.Properties(), 0));
    public static final DeferredItem<ic2.core.item.ItemMugCoffee> DARK_COFFEE = REGISTRY.register("dark_coffee", () -> new ic2.core.item.ItemMugCoffee(new Item.Properties(), 1));
    public static final DeferredItem<ic2.core.item.ItemMugCoffee> COFFEE = REGISTRY.register("coffee", () -> new ic2.core.item.ItemMugCoffee(new Item.Properties(), 2));
    public static final DeferredItem<ic2.core.item.ItemBooze> BOOZE_MUG = REGISTRY.register("booze_mug", () -> new ic2.core.item.ItemBooze(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.ItemBarrel> BARREL = REGISTRY.register("barrel", () -> new ic2.core.item.ItemBarrel(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.ItemBatteryChargeHotbar> CHARGING_RE_BATTERY = REGISTRY.register("charging_re_battery", () -> new ic2.core.item.ItemBatteryChargeHotbar(new Item.Properties(),40000,128,1));
    public static final DeferredItem<ic2.core.item.ItemBatteryChargeHotbar> CHARGING_ADVANCED_BATTERY = REGISTRY.register("charging_advanced_battery", () -> new ic2.core.item.ItemBatteryChargeHotbar(new Item.Properties(),400000,1024,2));
    public static final DeferredItem<ic2.core.item.ItemBatteryChargeHotbar> CHARGING_ENERGY_CRYSTAL = REGISTRY.register("charging_energy_crystal", () -> new ic2.core.item.ItemBatteryChargeHotbar(new Item.Properties(),4000000,8192,3));
    public static final DeferredItem<ic2.core.item.ItemBatteryChargeHotbar> CHARGING_LAPOTRON_CRYSTAL = REGISTRY.register("charging_lapotron_crystal", () -> new ic2.core.item.ItemBatteryChargeHotbar(new Item.Properties(),40000000,32768,4));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorJetpack> FUEL_JETPACK = REGISTRY.register("fuel_jetpack", () -> new ic2.core.item.armor.ItemArmorJetpack(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.resources.ItemLatheDefault> IRON_TURNING_BLANK = REGISTRY.register("iron_turning_blank", () -> new ic2.core.item.resources.ItemLatheDefault(new Item.Properties(), ic2.core.item.resources.ItemLatheDefault.LatheMaterial.IRON));
    public static final DeferredItem<ic2.core.item.resources.ItemLatheDefault> WOOD_TURNING_BLANK = REGISTRY.register("wood_turning_blank", () -> new ic2.core.item.resources.ItemLatheDefault(new Item.Properties(), ic2.core.item.resources.ItemLatheDefault.LatheMaterial.WOOD));
    public static final DeferredItem<ic2.core.item.tool.ItemLathingTool> LATHING_TOOL = REGISTRY.register("lathing_tool", () -> new ic2.core.item.tool.ItemLathingTool(new Item.Properties()));
    public static final DeferredItem<Item> STEAM_TURBINE = REGISTRY.register("steam_turbine", () -> new Item(new Item.Properties().durability(86400).setNoRepair()));
    public static final DeferredItem<ic2.core.item.ItemFertilizer> FERTILIZER = REGISTRY.register("fertilizer", () -> new ic2.core.item.ItemFertilizer(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.ItemTerraWart> TERRA_WART = REGISTRY.register("terra_wart", () -> new ic2.core.item.ItemTerraWart(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.ItemCropSeed> CROP_SEED = REGISTRY.register("crop_seed", () -> new ic2.core.item.ItemCropSeed(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.ItemCropAnalyzer> CROP_ANALYZER = REGISTRY.register("crop_analyzer", () -> new ic2.core.item.ItemCropAnalyzer(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ic2.core.item.ItemWeedingTrowel> WEEDING_TROWEL = REGISTRY.register("weeding_trowel", () -> new ic2.core.item.ItemWeedingTrowel(new Item.Properties()));
    public static final DeferredItem<Item> WEED_EX = REGISTRY.register("weed_ex", () -> new Item(new Item.Properties().durability(64)));
    public static final DeferredItem<Item> HYDRATION_CELL = REGISTRY.register("hydration_cell", () -> new Item(new Item.Properties().durability(10000)));
    public static final DeferredItem<ic2.core.item.ItemTinCan> FILLED_TIN_CAN = REGISTRY.register("filled_tin_can", () -> new ic2.core.item.ItemTinCan(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.tool.ItemFrequencyTransmitter> FREQUENCY_TRANSMITTER = REGISTRY.register("frequency_transmitter", () -> new ic2.core.item.tool.ItemFrequencyTransmitter(new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.DoubleHighBlockItem> REINFORCED_DOOR = REGISTRY.register("reinforced_door", () -> new net.minecraft.world.item.DoubleHighBlockItem(Ic2Blocks.REINFORCED_DOOR.get(), new Item.Properties().stacksTo(8)));
    public static final DeferredItem<ic2.core.item.tool.ItemToolMiningLaser> MINING_LASER = REGISTRY.register("mining_laser", () -> new ic2.core.item.tool.ItemToolMiningLaser(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.ItemToolbox> TOOL_BOX = REGISTRY.register("tool_box", () -> new ic2.core.item.ItemToolbox(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.tool.ItemToolMeter> EU_METER = REGISTRY.register("eu_meter", () -> new ic2.core.item.tool.ItemToolMeter(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.tool.ItemWindmeter> WIND_METER = REGISTRY.register("wind_meter", () -> new ic2.core.item.tool.ItemWindmeter(new Item.Properties()));
    public static final java.util.Map<net.minecraft.world.item.DyeColor, DeferredItem<ic2.core.item.tool.ItemToolPainter>> PAINTERS = new java.util.EnumMap<>(net.minecraft.world.item.DyeColor.class);
    static { for (var c : net.minecraft.world.item.DyeColor.values()) PAINTERS.put(c, REGISTRY.register(c.getName() + "_painter", () -> new ic2.core.item.tool.ItemToolPainter(new Item.Properties(), c))); }
    // reactor components (names of the 1.12 IC2 port)
    private static DeferredItem<Item> rod(String name, int cells, boolean mox) {
        return REGISTRY.register(name, () -> mox ? new ic2.core.item.reactor.ItemReactorUranium.MOX(new Item.Properties(), cells, () -> get("depleted_" + name)) : new ic2.core.item.reactor.ItemReactorUranium(new Item.Properties(), cells, 20000, () -> get("depleted_" + name)));
    }
    public static final DeferredItem<Item> URANIUM_FUEL_ROD = rod("uranium_fuel_rod", 1, false), DUAL_URANIUM_FUEL_ROD = rod("dual_uranium_fuel_rod", 2, false), QUAD_URANIUM_FUEL_ROD = rod("quad_uranium_fuel_rod", 4, false),
        MOX_FUEL_ROD = rod("mox_fuel_rod", 1, true), DUAL_MOX_FUEL_ROD = rod("dual_mox_fuel_rod", 2, true), QUAD_MOX_FUEL_ROD = rod("quad_mox_fuel_rod", 4, true);
    public static final DeferredItem<Item> LITHIUM_FUEL_ROD = REGISTRY.register("lithium_fuel_rod", () -> new ic2.core.item.reactor.ItemReactorLithiumCell(new Item.Properties()));
    public static final DeferredItem<Item> HEAT_STORAGE = REGISTRY.register("heat_storage", () -> new ic2.core.item.reactor.ItemReactorHeatStorage(new Item.Properties(), 10000));
    public static final DeferredItem<Item> TRI_HEAT_STORAGE = REGISTRY.register("tri_heat_storage", () -> new ic2.core.item.reactor.ItemReactorHeatStorage(new Item.Properties(), 30000));
    public static final DeferredItem<Item> HEX_HEAT_STORAGE = REGISTRY.register("hex_heat_storage", () -> new ic2.core.item.reactor.ItemReactorHeatStorage(new Item.Properties(), 60000));
    public static final DeferredItem<Item> PLATING = REGISTRY.register("plating", () -> new ic2.core.item.reactor.ItemReactorPlating(new Item.Properties(), 1000, 0.95f));
    public static final DeferredItem<Item> HEAT_PLATING = REGISTRY.register("heat_plating", () -> new ic2.core.item.reactor.ItemReactorPlating(new Item.Properties(), 2000, 0.99f));
    public static final DeferredItem<Item> CONTAINMENT_PLATING = REGISTRY.register("containment_plating", () -> new ic2.core.item.reactor.ItemReactorPlating(new Item.Properties(), 500, 0.9f));
    public static final DeferredItem<Item> HEAT_EXCHANGER = REGISTRY.register("heat_exchanger", () -> new ic2.core.item.reactor.ItemReactorHeatSwitch(new Item.Properties(), 2500, 12, 4));
    public static final DeferredItem<Item> REACTOR_HEAT_EXCHANGER = REGISTRY.register("reactor_heat_exchanger", () -> new ic2.core.item.reactor.ItemReactorHeatSwitch(new Item.Properties(), 5000, 0, 72));
    public static final DeferredItem<Item> COMPONENT_HEAT_EXCHANGER = REGISTRY.register("component_heat_exchanger", () -> new ic2.core.item.reactor.ItemReactorHeatSwitch(new Item.Properties(), 5000, 36, 0));
    public static final DeferredItem<Item> ADVANCED_HEAT_EXCHANGER = REGISTRY.register("advanced_heat_exchanger", () -> new ic2.core.item.reactor.ItemReactorHeatSwitch(new Item.Properties(), 10000, 24, 8));
    public static final DeferredItem<Item> HEAT_VENT = REGISTRY.register("heat_vent", () -> new ic2.core.item.reactor.ItemReactorVent(new Item.Properties(), 1000, 6, 0));
    public static final DeferredItem<Item> REACTOR_HEAT_VENT = REGISTRY.register("reactor_heat_vent", () -> new ic2.core.item.reactor.ItemReactorVent(new Item.Properties(), 1000, 5, 5));
    public static final DeferredItem<Item> OVERCLOCKED_HEAT_VENT = REGISTRY.register("overclocked_heat_vent", () -> new ic2.core.item.reactor.ItemReactorVent(new Item.Properties(), 1000, 20, 36));
    public static final DeferredItem<Item> COMPONENT_HEAT_VENT = REGISTRY.register("component_heat_vent", () -> new ic2.core.item.reactor.ItemReactorVentSpread(new Item.Properties(), 4));
    public static final DeferredItem<Item> ADVANCED_HEAT_VENT = REGISTRY.register("advanced_heat_vent", () -> new ic2.core.item.reactor.ItemReactorVent(new Item.Properties(), 1000, 12, 0));
    public static final DeferredItem<Item> NEUTRON_REFLECTOR = REGISTRY.register("neutron_reflector", () -> new ic2.core.item.reactor.ItemReactorReflector(new Item.Properties(), 10000));
    public static final DeferredItem<Item> THICK_NEUTRON_REFLECTOR = REGISTRY.register("thick_neutron_reflector", () -> new ic2.core.item.reactor.ItemReactorReflector(new Item.Properties(), 40000));
    public static final DeferredItem<Item> RSH_CONDENSATOR = REGISTRY.register("rsh_condensator", () -> new ic2.core.item.reactor.ItemReactorCondensator(new Item.Properties(), 20000));
    public static final DeferredItem<Item> LZH_CONDENSATOR = REGISTRY.register("lzh_condensator", () -> new ic2.core.item.reactor.ItemReactorCondensator(new Item.Properties(), 100000));
    public static final DeferredItem<ic2.core.item.ItemCrystalMemory> CRYSTAL_MEMORY = REGISTRY.register("crystal_memory", () -> new ic2.core.item.ItemCrystalMemory(new Item.Properties()));
    public static final java.util.Map<ic2.core.item.tfbp.ItemTFBP.Kind, DeferredItem<ic2.core.item.tfbp.ItemTFBP>> TFBPS = new java.util.EnumMap<>(ic2.core.item.tfbp.ItemTFBP.Kind.class);
    static { for (var k : ic2.core.item.tfbp.ItemTFBP.Kind.values()) TFBPS.put(k, REGISTRY.register(k.name().toLowerCase() + "_terraformer_blueprint", () -> new ic2.core.item.tfbp.ItemTFBP(new Item.Properties(), k))); }
    public static final DeferredItem<ic2.core.item.tool.ItemSprayer> FOAM_SPRAYER = REGISTRY.register("foam_sprayer", () -> new ic2.core.item.tool.ItemSprayer(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorFluidTank> CF_PACK = REGISTRY.register("cf_pack", () -> new ic2.core.item.armor.ItemArmorFluidTank(Ic2ArmorMaterials.BATPACK, new Item.Properties(), ic2.core.fluid.Ic2Fluids.CONSTRUCTION_FOAM::get, 80000));
    public static final DeferredItem<ic2.core.item.ItemDynamite> DYNAMITE = REGISTRY.register("dynamite", () -> new ic2.core.item.ItemDynamite(new Item.Properties(), false));
    public static final DeferredItem<ic2.core.item.ItemDynamite> STICKY_DYNAMITE = REGISTRY.register("sticky_dynamite", () -> new ic2.core.item.ItemDynamite(new Item.Properties(), true));
    public static final DeferredItem<ic2.core.item.tool.ItemRemote> DYNAMITE_REMOTE = REGISTRY.register("dynamite_remote", () -> new ic2.core.item.tool.ItemRemote(new Item.Properties()));
    public static final DeferredItem<Item> MINING_PIPE = REGISTRY.registerSimpleItem("mining_pipe");
    public static final DeferredItem<ic2.core.item.tool.ItemScanner> OD_SCANNER = REGISTRY.register("od_scanner", () -> new ic2.core.item.tool.ItemScanner(new Item.Properties(), 100000, 128, 1, 6, 50));
    public static final DeferredItem<ic2.core.item.tool.ItemScanner> OV_SCANNER = REGISTRY.register("ov_scanner", () -> new ic2.core.item.tool.ItemScanner(new Item.Properties().rarity(Rarity.UNCOMMON), 1000000, 512, 2, 12, 250));
    public static final DeferredItem<ItemToolWrench> WRENCH = REGISTRY.register("wrench", () -> new ItemToolWrench(new Item.Properties().durability(160)));
    public static final DeferredItem<ic2.core.item.tool.ItemCraftingTool> FORGE_HAMMER = REGISTRY.register("forge_hammer", () -> new ic2.core.item.tool.ItemCraftingTool(new Item.Properties().durability(80)));
    public static final DeferredItem<ic2.core.item.tool.ItemToolCutter> CUTTER = REGISTRY.register("cutter", () -> new ic2.core.item.tool.ItemToolCutter(new Item.Properties().durability(60)));
    public static final DeferredItem<ic2.core.item.tool.ItemTreetap> TREETAP = REGISTRY.register("treetap", () -> new ic2.core.item.tool.ItemTreetap(new Item.Properties().durability(16)));
    public static final DeferredItem<ic2.core.item.resources.ItemCell> EMPTY_CELL = REGISTRY.register("empty_cell", () -> new ic2.core.item.resources.ItemCell(new Item.Properties(), null));
    public static final DeferredItem<ic2.core.item.resources.ItemCell> WATER_CELL = REGISTRY.register("water_cell", () -> new ic2.core.item.resources.ItemCell(new Item.Properties(), () -> net.minecraft.world.level.material.Fluids.WATER));
    public static final DeferredItem<ic2.core.item.resources.ItemCell> LAVA_CELL = REGISTRY.register("lava_cell", () -> new ic2.core.item.resources.ItemCell(new Item.Properties(), () -> net.minecraft.world.level.material.Fluids.LAVA));
    public static final java.util.Map<String, DeferredItem<ic2.core.item.resources.ItemCell>> FLUID_CELLS = new java.util.LinkedHashMap<>();
    static {
        for (var f : ic2.core.fluid.Ic2Fluids.ALL) FLUID_CELLS.put(f.name, REGISTRY.register(f.name + "_cell", () -> new ic2.core.item.resources.ItemCell(new Item.Properties(), f::get)));
    }
    public static final DeferredItem<ic2.core.item.resources.ItemFluidCell> UNIVERSAL_FLUID_CELL = REGISTRY.register("universal_fluid_cell", () -> new ic2.core.item.resources.ItemFluidCell(new Item.Properties()));
    public static final DeferredItem<Item> AIR_CELL = REGISTRY.register("air_cell", () -> new Item(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.resources.ItemBlockCuttingBlade> IRON_BLOCK_CUTTING_BLADE = REGISTRY.register("iron_block_cutting_blade", () -> new ic2.core.item.resources.ItemBlockCuttingBlade(new Item.Properties(), 3));
    public static final DeferredItem<ic2.core.item.resources.ItemBlockCuttingBlade> REFINED_IRON_BLOCK_CUTTING_BLADE = REGISTRY.register("refined_iron_block_cutting_blade", () -> new ic2.core.item.resources.ItemBlockCuttingBlade(new Item.Properties(), 6));
    public static final DeferredItem<ic2.core.item.resources.ItemBlockCuttingBlade> DIAMOND_BLOCK_CUTTING_BLADE = REGISTRY.register("diamond_block_cutting_blade", () -> new ic2.core.item.resources.ItemBlockCuttingBlade(new Item.Properties(), 9));
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

    public static final DeferredItem<ic2.core.item.resources.ItemWindRotor> WOOD_ROTOR = REGISTRY.register("wood_rotor", () -> new ic2.core.item.resources.ItemWindRotor(new Item.Properties(), 5, 10800, 0.25f, 10, 60, IC2.id("textures/item/rotor/rotor_wood_model.png"), false));
    public static final DeferredItem<ic2.core.item.resources.ItemWindRotor> IRON_ROTOR = REGISTRY.register("iron_rotor", () -> new ic2.core.item.resources.ItemWindRotor(new Item.Properties(), 7, 86400, 0.5f, 14, 75, IC2.id("textures/item/rotor/rotor_iron_model.png"), true));
    public static final DeferredItem<ic2.core.item.resources.ItemWindRotor> STEEL_ROTOR = REGISTRY.register("steel_rotor", () -> new ic2.core.item.resources.ItemWindRotor(new Item.Properties(), 9, 172800, 0.75f, 17, 90, IC2.id("textures/item/rotor/rotor_steel_model.png"), true));
    public static final DeferredItem<ic2.core.item.resources.ItemWindRotor> CARBON_ROTOR = REGISTRY.register("carbon_rotor", () -> new ic2.core.item.resources.ItemWindRotor(new Item.Properties(), 11, 604800, 1.0f, 20, 110, IC2.id("textures/item/rotor/rotor_carbon_model.png"), true));
    public static final DeferredItem<ic2.core.item.ItemScrapbox> SCRAP_BOX = REGISTRY.register("scrap_box", () -> new ic2.core.item.ItemScrapbox(new Item.Properties()));
    // electric tools
    public static final DeferredItem<ic2.core.item.tool.ItemDrill> MINING_DRILL = REGISTRY.register("mining_drill", () -> new ic2.core.item.tool.ItemDrill(new Item.Properties(), 50, ic2.core.item.tool.ItemElectricTool.HarvestLevel.Iron, 30000, 100, 1, 8, false));
    public static final DeferredItem<ic2.core.item.tool.ItemDrill> DIAMOND_DRILL = REGISTRY.register("diamond_drill", () -> new ic2.core.item.tool.ItemDrill(new Item.Properties(), 80, ic2.core.item.tool.ItemElectricTool.HarvestLevel.Diamond, 30000, 100, 1, 16, false));
    public static final DeferredItem<ic2.core.item.tool.ItemDrill> IRIDIUM_DRILL = REGISTRY.register("iridium_drill", () -> new ic2.core.item.tool.ItemDrill(new Item.Properties().rarity(Rarity.RARE), 800, ic2.core.item.tool.ItemElectricTool.HarvestLevel.Iridium, 300000, 1000, 3, 24, true));
    public static final DeferredItem<ic2.core.item.tool.ItemElectricToolChainsaw> CHAINSAW = REGISTRY.register("chainsaw", () -> new ic2.core.item.tool.ItemElectricToolChainsaw(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.tool.ItemElectricToolSimple> ELECTRIC_HOE = REGISTRY.register("electric_hoe", () -> new ic2.core.item.tool.ItemElectricToolSimple(new Item.Properties(), ic2.core.item.tool.ItemElectricToolSimple.Kind.hoe));
    public static final DeferredItem<ic2.core.item.tool.ItemElectricToolSimple> ELECTRIC_TREETAP = REGISTRY.register("electric_treetap", () -> new ic2.core.item.tool.ItemElectricToolSimple(new Item.Properties(), ic2.core.item.tool.ItemElectricToolSimple.Kind.treetap));
    public static final DeferredItem<ic2.core.item.tool.ItemToolWrenchElectric> ELECTRIC_WRENCH = REGISTRY.register("electric_wrench", () -> new ic2.core.item.tool.ItemToolWrenchElectric(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.tool.ItemNanoSaber> NANO_SABER = REGISTRY.register("nano_saber", () -> new ic2.core.item.tool.ItemNanoSaber(new Item.Properties()));
    // bronze tools: iron tier, efficiency 5, bronze repair
    public static final net.minecraft.world.item.Tier BRONZE_TIER = new net.neoforged.neoforge.common.SimpleTier(net.minecraft.tags.BlockTags.INCORRECT_FOR_IRON_TOOL, 250, 5, 2, 14, () -> net.minecraft.world.item.crafting.Ingredient.of(get("bronze_ingot")));
    public static final DeferredItem<net.minecraft.world.item.PickaxeItem> BRONZE_PICKAXE = REGISTRY.register("bronze_pickaxe", () -> new net.minecraft.world.item.PickaxeItem(BRONZE_TIER, new Item.Properties().attributes(net.minecraft.world.item.PickaxeItem.createAttributes(BRONZE_TIER, 1, -2.8f))));
    public static final DeferredItem<net.minecraft.world.item.AxeItem> BRONZE_AXE = REGISTRY.register("bronze_axe", () -> new net.minecraft.world.item.AxeItem(BRONZE_TIER, new Item.Properties().attributes(net.minecraft.world.item.AxeItem.createAttributes(BRONZE_TIER, 6, -3.1f))));
    public static final DeferredItem<net.minecraft.world.item.SwordItem> BRONZE_SWORD = REGISTRY.register("bronze_sword", () -> new net.minecraft.world.item.SwordItem(BRONZE_TIER, new Item.Properties().attributes(net.minecraft.world.item.SwordItem.createAttributes(BRONZE_TIER, 4, -2.4f))));
    public static final DeferredItem<net.minecraft.world.item.ShovelItem> BRONZE_SHOVEL = REGISTRY.register("bronze_shovel", () -> new net.minecraft.world.item.ShovelItem(BRONZE_TIER, new Item.Properties().attributes(net.minecraft.world.item.ShovelItem.createAttributes(BRONZE_TIER, 1.5f, -3f))));
    public static final DeferredItem<net.minecraft.world.item.HoeItem> BRONZE_HOE = REGISTRY.register("bronze_hoe", () -> new net.minecraft.world.item.HoeItem(BRONZE_TIER, new Item.Properties().attributes(net.minecraft.world.item.HoeItem.createAttributes(BRONZE_TIER, -2, -1f))));
    // armor
    public static final DeferredItem<ic2.core.item.armor.ItemArmorIC2> BRONZE_HELMET = armor("bronze_helmet", ic2.core.ref.Ic2ArmorMaterials.BRONZE, net.minecraft.world.item.ArmorItem.Type.HELMET, 15);
    public static final DeferredItem<ic2.core.item.armor.ItemArmorIC2> BRONZE_CHESTPLATE = armor("bronze_chestplate", ic2.core.ref.Ic2ArmorMaterials.BRONZE, net.minecraft.world.item.ArmorItem.Type.CHESTPLATE, 15);
    public static final DeferredItem<ic2.core.item.armor.ItemArmorIC2> BRONZE_LEGGINGS = armor("bronze_leggings", ic2.core.ref.Ic2ArmorMaterials.BRONZE, net.minecraft.world.item.ArmorItem.Type.LEGGINGS, 15);
    public static final DeferredItem<ic2.core.item.armor.ItemArmorIC2> BRONZE_BOOTS = armor("bronze_boots", ic2.core.ref.Ic2ArmorMaterials.BRONZE, net.minecraft.world.item.ArmorItem.Type.BOOTS, 15);
    public static final DeferredItem<ic2.core.item.armor.ItemArmorIC2> COMPOSITE_VEST = armor("composite_vest", ic2.core.ref.Ic2ArmorMaterials.ALLOY, net.minecraft.world.item.ArmorItem.Type.CHESTPLATE, 50);
    public static final DeferredItem<ic2.core.item.armor.ItemArmorNanoSuit> NANO_HELMET = REGISTRY.register("nano_helmet", () -> new ic2.core.item.armor.ItemArmorNanoSuit(net.minecraft.world.item.ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorNanoSuit> NANO_CHESTPLATE = REGISTRY.register("nano_chestplate", () -> new ic2.core.item.armor.ItemArmorNanoSuit(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorNanoSuit> NANO_LEGGINGS = REGISTRY.register("nano_leggings", () -> new ic2.core.item.armor.ItemArmorNanoSuit(net.minecraft.world.item.ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorNanoSuit> NANO_BOOTS = REGISTRY.register("nano_boots", () -> new ic2.core.item.armor.ItemArmorNanoSuit(net.minecraft.world.item.ArmorItem.Type.BOOTS, new Item.Properties()));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorQuantumSuit> QUANTUM_HELMET = REGISTRY.register("quantum_helmet", () -> new ic2.core.item.armor.ItemArmorQuantumSuit(net.minecraft.world.item.ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorQuantumSuit> QUANTUM_CHESTPLATE = REGISTRY.register("quantum_chestplate", () -> new ic2.core.item.armor.ItemArmorQuantumSuit(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorQuantumSuit> QUANTUM_LEGGINGS = REGISTRY.register("quantum_leggings", () -> new ic2.core.item.armor.ItemArmorQuantumSuit(net.minecraft.world.item.ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorQuantumSuit> QUANTUM_BOOTS = REGISTRY.register("quantum_boots", () -> new ic2.core.item.armor.ItemArmorQuantumSuit(net.minecraft.world.item.ArmorItem.Type.BOOTS, new Item.Properties()));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorJetpackElectric> ELECTRIC_JETPACK = REGISTRY.register("electric_jetpack", () -> new ic2.core.item.armor.ItemArmorJetpackElectric(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorBatpack> BATPACK = REGISTRY.register("batpack", () -> new ic2.core.item.armor.ItemArmorBatpack(ic2.core.ref.Ic2ArmorMaterials.BATPACK, new Item.Properties(), 60000, 100, 1));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorBatpack> ADVANCED_BATPACK = REGISTRY.register("advanced_batpack", () -> new ic2.core.item.armor.ItemArmorBatpack(ic2.core.ref.Ic2ArmorMaterials.ADVBATPACK, new Item.Properties(), 600000, 1000, 2));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorBatpack> ENERGY_PACK = REGISTRY.register("energy_pack", () -> new ic2.core.item.armor.ItemArmorBatpack(ic2.core.ref.Ic2ArmorMaterials.ENERGYPACK, new Item.Properties(), 2000000, 1000, 3));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorBatpack> LAPPACK = REGISTRY.register("lappack", () -> new ic2.core.item.armor.ItemArmorBatpack(ic2.core.ref.Ic2ArmorMaterials.LAPPACK, new Item.Properties().rarity(Rarity.UNCOMMON), 2e7, 2500, 4));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorUtility.Hazmat> HAZMAT_HELMET = REGISTRY.register("hazmat_helmet", () -> new ic2.core.item.armor.ItemArmorUtility.Hazmat(net.minecraft.world.item.ArmorItem.Type.HELMET, new Item.Properties().durability(64)));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorUtility.Hazmat> HAZMAT_CHESTPLATE = REGISTRY.register("hazmat_chestplate", () -> new ic2.core.item.armor.ItemArmorUtility.Hazmat(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(64)));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorUtility.Hazmat> HAZMAT_LEGGINGS = REGISTRY.register("hazmat_leggings", () -> new ic2.core.item.armor.ItemArmorUtility.Hazmat(net.minecraft.world.item.ArmorItem.Type.LEGGINGS, new Item.Properties().durability(64)));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorUtility.Hazmat> RUBBER_BOOTS = REGISTRY.register("rubber_boots", () -> new ic2.core.item.armor.ItemArmorUtility.Hazmat(net.minecraft.world.item.ArmorItem.Type.BOOTS, new Item.Properties().durability(64)));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorUtility.NightvisionGoggles> NIGHTVISION_GOGGLES = REGISTRY.register("nightvision_goggles", () -> new ic2.core.item.armor.ItemArmorUtility.NightvisionGoggles(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorUtility.SolarHelmet> SOLAR_HELMET = REGISTRY.register("solar_helmet", () -> new ic2.core.item.armor.ItemArmorUtility.SolarHelmet(new Item.Properties()));
    public static final DeferredItem<ic2.core.item.armor.ItemArmorUtility.StaticBoots> STATIC_BOOTS = REGISTRY.register("static_boots", () -> new ic2.core.item.armor.ItemArmorUtility.StaticBoots(new Item.Properties()));

    private static DeferredItem<ic2.core.item.armor.ItemArmorIC2> armor(String name, net.minecraft.core.Holder<net.minecraft.world.item.ArmorMaterial> material, net.minecraft.world.item.ArmorItem.Type type, int durability) {
        return REGISTRY.register(name, () -> new ic2.core.item.armor.ItemArmorIC2(material, type, new Item.Properties().durability(type.getDurability(durability))));
    }

    private static DeferredItem<ItemUpgradeModule> upgrade(String name, ItemUpgradeModule.Type type) { return REGISTRY.register(name, () -> new ItemUpgradeModule(new Item.Properties(), type)); }

    /** Item of a simple or block id of this mod. */
    public static boolean has(String id) { return SIMPLE.containsKey(id) || BuiltInRegistries.ITEM.containsKey(IC2.id(id)); }
    public static Item get(String id) { var d = SIMPLE.get(id); return d != null ? d.get() : BuiltInRegistries.ITEM.get(IC2.id(id)); }

    private Ic2Items() {}
}
