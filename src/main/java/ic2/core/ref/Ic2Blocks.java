package ic2.core.ref;

import ic2.core.IC2;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.wiring.BlockElectric;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2Blocks {
    public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(IC2.MODID);

    private static BlockBehaviour.Properties stone(float hardness, float resistance) { return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(hardness, resistance).requiresCorrectToolForDrops().sound(SoundType.STONE); }
    /** Deepslate ore variants: 1.5 harder than the stone ones like vanilla, same blast resistance as deepslate ores. */
    private static BlockBehaviour.Properties deepslate(float hardness) { return BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).strength(hardness, 3).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE); }
    private static BlockBehaviour.Properties metal(float hardness, float resistance) { return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(hardness, resistance).requiresCorrectToolForDrops().sound(SoundType.METAL); }

    public static final DeferredBlock<ic2.core.crop.BlockCrop> CROP = REGISTRY.register("crop", () -> new ic2.core.crop.BlockCrop(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.CROP).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
    public static final DeferredBlock<Block> TIN_ORE = REGISTRY.register("tin_ore", () -> new Block(stone(3, 5)));
    public static final DeferredBlock<Block> URANIUM_ORE = REGISTRY.register("uranium_ore", () -> new Block(stone(4, 6)));
    public static final DeferredBlock<Block> LEAD_ORE = REGISTRY.register("lead_ore", () -> new Block(stone(2, 4)));
    public static final DeferredBlock<Block> RAW_TIN_BLOCK = REGISTRY.register("raw_tin_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().strength(5, 6)));
    public static final DeferredBlock<Block> RAW_LEAD_BLOCK = REGISTRY.register("raw_lead_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).requiresCorrectToolForDrops().strength(5, 6)));
    public static final DeferredBlock<Block> DEEPSLATE_TIN_ORE = REGISTRY.register("deepslate_tin_ore", () -> new Block(deepslate(4.5f)));
    public static final DeferredBlock<Block> DEEPSLATE_URANIUM_ORE = REGISTRY.register("deepslate_uranium_ore", () -> new Block(deepslate(5.5f)));
    public static final DeferredBlock<Block> DEEPSLATE_LEAD_ORE = REGISTRY.register("deepslate_lead_ore", () -> new Block(deepslate(3.5f)));
    public static final DeferredBlock<Block> TIN_BLOCK = REGISTRY.register("tin_block", () -> new Block(metal(5, 10)));
    public static final DeferredBlock<Block> BRONZE_BLOCK = REGISTRY.register("bronze_block", () -> new Block(metal(5, 10)));
    public static final DeferredBlock<Block> URANIUM_BLOCK = REGISTRY.register("uranium_block", () -> new Block(metal(5, 10)));
    public static final DeferredBlock<Block> LEAD_BLOCK = REGISTRY.register("lead_block", () -> new Block(metal(5, 10)));
    public static final DeferredBlock<Block> REFINED_IRON_BLOCK = REGISTRY.register("refined_iron_block", () -> new Block(metal(5, 10)));
    public static final DeferredBlock<Block> REINFORCED_STONE = REGISTRY.register("reinforced_stone", () -> new Block(metal(80, 180)));
    public static final DeferredBlock<Block> BASALT = REGISTRY.register("basalt", () -> new Block(stone(20, 45)));
    public static final DeferredBlock<ic2.core.block.rubber.BlockRubWood> RUBBER_WOOD = REGISTRY.register("rubber_wood", () -> new ic2.core.block.rubber.BlockRubWood(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<ic2.core.block.rubber.BlockRubLeaves> RUBBER_LEAVES = REGISTRY.register("rubber_leaves", () -> new ic2.core.block.rubber.BlockRubLeaves(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.2f).randomTicks().sound(SoundType.GRASS).noOcclusion().isValidSpawn((s, l, p, e) -> false).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false).ignitedByLava().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).isRedstoneConductor((s, l, p) -> false)));
    public static final DeferredBlock<ic2.core.block.rubber.BlockRubSapling> RUBBER_SAPLING = REGISTRY.register("rubber_sapling", () -> new ic2.core.block.rubber.BlockRubSapling(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
    public static final DeferredBlock<Block> MACHINE = REGISTRY.register("machine", () -> new Block(metal(2, 10)));
    public static final DeferredBlock<Block> ADVANCED_MACHINE = REGISTRY.register("advanced_machine", () -> new Block(metal(2, 10)));

    public static final DeferredBlock<BlockTileEntity> IRON_FURNACE = machine("iron_furnace", () -> Ic2BlockEntities.IRON_FURNACE.get());
    public static final DeferredBlock<BlockTileEntity> ELECTRIC_FURNACE = machine("electric_furnace", () -> Ic2BlockEntities.ELECTRIC_FURNACE.get());
    public static final DeferredBlock<BlockTileEntity> MACERATOR = machine("macerator", () -> Ic2BlockEntities.MACERATOR.get());
    public static final DeferredBlock<BlockTileEntity> EXTRACTOR = machine("extractor", () -> Ic2BlockEntities.EXTRACTOR.get());
    public static final DeferredBlock<BlockTileEntity> COMPRESSOR = machine("compressor", () -> Ic2BlockEntities.COMPRESSOR.get());
    public static final DeferredBlock<BlockTileEntity> RECYCLER = machine("recycler", () -> Ic2BlockEntities.RECYCLER.get());
    public static final DeferredBlock<BlockTileEntity> INDUCTION_FURNACE = machine("induction_furnace", () -> Ic2BlockEntities.INDUCTION_FURNACE.get());
    public static final DeferredBlock<BlockTileEntity> METAL_FORMER = machine("metal_former", () -> Ic2BlockEntities.METAL_FORMER.get());
    public static final DeferredBlock<BlockTileEntity> ORE_WASHING_PLANT = machine("ore_washing_plant", () -> Ic2BlockEntities.ORE_WASHING_PLANT.get());
    public static final DeferredBlock<BlockTileEntity> THERMAL_CENTRIFUGE = machine("thermal_centrifuge", () -> Ic2BlockEntities.THERMAL_CENTRIFUGE.get());
    public static final DeferredBlock<BlockTileEntity> BLOCK_CUTTER = machine("block_cutter", () -> Ic2BlockEntities.BLOCK_CUTTER.get());
    public static final DeferredBlock<BlockTileEntity> BLAST_FURNACE = machine("blast_furnace", () -> Ic2BlockEntities.BLAST_FURNACE.get());
    public static final DeferredBlock<BlockTileEntity> SOLID_HEAT_GENERATOR = generator("solid_heat_generator", () -> Ic2BlockEntities.SOLID_HEAT_GENERATOR.get());
    public static final DeferredBlock<BlockTileEntity> RT_HEAT_GENERATOR = generator("rt_heat_generator", () -> Ic2BlockEntities.RT_HEAT_GENERATOR.get());
    public static final DeferredBlock<BlockTileEntity> ELECTRIC_HEAT_GENERATOR = generator("electric_heat_generator", () -> Ic2BlockEntities.ELECTRIC_HEAT_GENERATOR.get());
    public static final DeferredBlock<BlockTileEntity> RT_GENERATOR = generator("rt_generator", () -> Ic2BlockEntities.RT_GENERATOR.get());
    public static final DeferredBlock<BlockTileEntity> SEMIFLUID_GENERATOR = generator("semifluid_generator", () -> Ic2BlockEntities.SEMIFLUID_GENERATOR.get());
    public static final DeferredBlock<BlockTileEntity> STIRLING_GENERATOR = generator("stirling_generator", () -> Ic2BlockEntities.STIRLING_GENERATOR.get());
    public static final DeferredBlock<BlockTileEntity> KINETIC_GENERATOR = generator("kinetic_generator", () -> Ic2BlockEntities.KINETIC_GENERATOR.get());
    public static final DeferredBlock<BlockTileEntity> WIND_KINETIC_GENERATOR = generator("wind_kinetic_generator", () -> Ic2BlockEntities.WIND_KINETIC_GENERATOR.get());
    public static final DeferredBlock<BlockTileEntity> WATER_KINETIC_GENERATOR = generator("water_kinetic_generator", () -> Ic2BlockEntities.WATER_KINETIC_GENERATOR.get());
    public static final DeferredBlock<BlockTileEntity> ELECTRIC_KINETIC_GENERATOR = generator("electric_kinetic_generator", () -> Ic2BlockEntities.ELECTRIC_KINETIC_GENERATOR.get());
    public static final DeferredBlock<BlockTileEntity> MANUAL_KINETIC_GENERATOR = generator("manual_kinetic_generator", () -> Ic2BlockEntities.MANUAL_KINETIC_GENERATOR.get());
    public static final DeferredBlock<BlockTileEntity> MASS_FABRICATOR = machine("mass_fabricator", () -> Ic2BlockEntities.MASS_FABRICATOR.get());
    public static final DeferredBlock<ic2.core.block.misc.BlockScaffold> SCAFFOLD = REGISTRY.register("scaffold", () -> new ic2.core.block.misc.BlockScaffold(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5f, 0.2f).sound(SoundType.WOOD).noOcclusion().ignitedByLava(), false));
    public static final DeferredBlock<ic2.core.block.misc.BlockScaffold> IRON_SCAFFOLD = REGISTRY.register("iron_scaffold", () -> new ic2.core.block.misc.BlockScaffold(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.8f, 10).sound(SoundType.METAL).noOcclusion(), true));
    public static final DeferredBlock<ic2.core.block.misc.BlockRubberSheet> RUBBER_SHEET = REGISTRY.register("rubber_sheet", () -> new ic2.core.block.misc.BlockRubberSheet(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.8f, 2).sound(SoundType.WOOL).noOcclusion()));
    public static final DeferredBlock<ic2.core.block.misc.BlockResinSheet> RESIN_SHEET = REGISTRY.register("resin_sheet", () -> new ic2.core.block.misc.BlockResinSheet(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(1.6f, 0.5f).sound(SoundType.SAND).noOcclusion().noCollission()));
    public static final DeferredBlock<net.minecraft.world.level.block.TransparentBlock> REINFORCED_GLASS = REGISTRY.register("reinforced_glass", () -> new net.minecraft.world.level.block.TransparentBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(5, 180).sound(SoundType.GLASS).noOcclusion().isValidSpawn((s, l, p, e) -> false).isRedstoneConductor((s, l, p) -> false).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false)));
    public static final DeferredBlock<net.minecraft.world.level.block.DoorBlock> REINFORCED_DOOR = REGISTRY.register("reinforced_door", () -> new net.minecraft.world.level.block.DoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.IRON, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(50, 150).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
    public static final DeferredBlock<BlockTileEntity> NUCLEAR_REACTOR = generator("nuclear_reactor", () -> Ic2BlockEntities.NUCLEAR_REACTOR.get());
    public static final DeferredBlock<ic2.core.block.reactor.BlockReactorChamber> REACTOR_CHAMBER = REGISTRY.register("reactor_chamber", () -> new ic2.core.block.reactor.BlockReactorChamber(metal(2, 10), () -> Ic2BlockEntities.REACTOR_CHAMBER.get()));
    public static final DeferredBlock<Block> REACTOR_VESSEL = REGISTRY.register("reactor_vessel", () -> new Block(metal(40, 90)));
    public static final DeferredBlock<BlockTileEntity> REACTOR_ACCESS_HATCH = REGISTRY.register("reactor_access_hatch", () -> new BlockTileEntity(metal(40, 90), () -> Ic2BlockEntities.REACTOR_ACCESS_HATCH.get()));
    public static final DeferredBlock<BlockTileEntity> REACTOR_REDSTONE_PORT = REGISTRY.register("reactor_redstone_port", () -> new BlockTileEntity(metal(40, 90), () -> Ic2BlockEntities.REACTOR_REDSTONE_PORT.get()));
    public static final DeferredBlock<BlockTileEntity> REACTOR_FLUID_PORT = REGISTRY.register("reactor_fluid_port", () -> new BlockTileEntity(metal(40, 90), () -> Ic2BlockEntities.REACTOR_FLUID_PORT.get()));
    public static final DeferredBlock<BlockTileEntity> STEAM_GENERATOR = machine("steam_generator", () -> Ic2BlockEntities.STEAM_GENERATOR.get());
    public static final DeferredBlock<ic2.core.block.wiring.BlockLuminator> LUMINATOR = REGISTRY.register("luminator", () -> new ic2.core.block.wiring.BlockLuminator(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(.3f,.5f).sound(SoundType.GLASS).noOcclusion().lightLevel(s -> s.getValue(BlockTileEntity.ACTIVE)?15:0)));
    public static final DeferredBlock<ic2.core.block.explosive.BlockIndustrialTnt> INDUSTRIAL_TNT = REGISTRY.register("industrial_tnt", () -> new ic2.core.block.explosive.BlockIndustrialTnt(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instabreak().sound(SoundType.GRASS).ignitedByLava()));
    public static final DeferredBlock<ic2.core.block.explosive.BlockNuke> NUKE = REGISTRY.register("nuke", () -> new ic2.core.block.explosive.BlockNuke(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instabreak().sound(SoundType.GRASS).ignitedByLava()));
    public static final DeferredBlock<BlockTileEntity> ADV_MINER = machine("adv_miner", () -> Ic2BlockEntities.ADV_MINER.get());
    public static final DeferredBlock<BlockTileEntity> LATHE = machine("lathe", () -> Ic2BlockEntities.LATHE.get());
    public static final DeferredBlock<BlockTileEntity> STIRLING_KINETIC_GENERATOR = machine("stirling_kinetic_generator", () -> Ic2BlockEntities.STIRLING_KINETIC_GENERATOR.get());
    public static final DeferredBlock<BlockTileEntity> STEAM_KINETIC_GENERATOR = machine("steam_kinetic_generator", () -> Ic2BlockEntities.STEAM_KINETIC_GENERATOR.get());
    public static final DeferredBlock<BlockTileEntity> FLUID_HEAT_GENERATOR = machine("fluid_heat_generator", () -> Ic2BlockEntities.FLUID_HEAT_GENERATOR.get());
    public static final DeferredBlock<BlockTileEntity> LIQUID_HEAT_EXCHANGER = machine("liquid_heat_exchanger", () -> Ic2BlockEntities.LIQUID_HEAT_EXCHANGER.get());
    public static final DeferredBlock<BlockTileEntity> FERMENTER = machine("fermenter", () -> Ic2BlockEntities.FERMENTER.get());
    public static final DeferredBlock<BlockTileEntity> FLUID_DISTRIBUTOR = machine("fluid_distributor", () -> Ic2BlockEntities.FLUID_DISTRIBUTOR.get());
    public static final DeferredBlock<BlockTileEntity> FLUID_REGULATOR = machine("fluid_regulator", () -> Ic2BlockEntities.FLUID_REGULATOR.get());
    public static final DeferredBlock<BlockTileEntity> CONDENSER = machine("condenser", () -> Ic2BlockEntities.CONDENSER.get());
    public static final DeferredBlock<BlockTileEntity> SOLAR_DISTILLER = machine("solar_distiller", () -> Ic2BlockEntities.SOLAR_DISTILLER.get());
    public static final DeferredBlock<BlockTileEntity> SORTING_MACHINE = machine("sorting_machine", () -> Ic2BlockEntities.SORTING_MACHINE.get());
    public static final DeferredBlock<BlockTileEntity> ITEM_BUFFER = machine("item_buffer", () -> Ic2BlockEntities.ITEM_BUFFER.get());
    public static final DeferredBlock<BlockTileEntity> CROPMATRON = machine("cropmatron", () -> Ic2BlockEntities.CROPMATRON.get());
    public static final DeferredBlock<BlockTileEntity> CROP_HARVESTER = machine("crop_harvester", () -> Ic2BlockEntities.CROP_HARVESTER.get());
    public static final DeferredBlock<BlockTileEntity> TERRAFORMER = machine("terraformer", () -> Ic2BlockEntities.TERRAFORMER.get());
    public static final DeferredBlock<BlockTileEntity> SCANNER = machine("scanner", () -> Ic2BlockEntities.SCANNER.get());
    public static final DeferredBlock<BlockTileEntity> REPLICATOR = machine("replicator", () -> Ic2BlockEntities.REPLICATOR.get());
    public static final DeferredBlock<BlockTileEntity> PATTERN_STORAGE = machine("pattern_storage", () -> Ic2BlockEntities.PATTERN_STORAGE.get());
    public static final DeferredBlock<ic2.core.block.misc.BlockFoam> CONSTRUCTION_FOAM = REGISTRY.register("foam", () -> new ic2.core.block.misc.BlockFoam(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.01f, 10).sound(SoundType.WOOL).noOcclusion().isValidSpawn((s, l, p, e) -> false).noLootTable(), false));
    public static final DeferredBlock<ic2.core.block.misc.BlockFoam> REINFORCED_CONSTRUCTION_FOAM = REGISTRY.register("reinforced_foam", () -> new ic2.core.block.misc.BlockFoam(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.01f, 10).sound(SoundType.WOOL).noOcclusion().isValidSpawn((s, l, p, e) -> false), true));
    public static final DeferredBlock<ic2.core.block.misc.BlockFoamWall> CONSTRUCTION_FOAM_WALL = REGISTRY.register("foam_wall", () -> new ic2.core.block.misc.BlockFoamWall(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3, 30).sound(SoundType.STONE).noLootTable()));
    public static final DeferredBlock<ic2.core.block.explosive.BlockDynamite> DYNAMITE = REGISTRY.register("dynamite", () -> new ic2.core.block.explosive.BlockDynamite(BlockBehaviour.Properties.of().instabreak().sound(SoundType.GRASS).noCollission().noOcclusion().noLootTable().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
    public static final DeferredBlock<ic2.core.block.explosive.BlockDynamite> DYNAMITE_REMOTE = REGISTRY.register("dynamite_remote", () -> new ic2.core.block.explosive.BlockDynamite(BlockBehaviour.Properties.of().instabreak().sound(SoundType.GRASS).noCollission().noOcclusion().noLootTable().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
    public static final DeferredBlock<BlockTileEntity> TELEPORTER = machine("teleporter", () -> Ic2BlockEntities.TELEPORTER.get());
    public static final DeferredBlock<BlockTileEntity> TESLA_COIL = machine("tesla_coil", () -> Ic2BlockEntities.TESLA_COIL.get());
    public static final DeferredBlock<BlockTileEntity> MAGNETIZER = machine("magnetizer", () -> Ic2BlockEntities.MAGNETIZER.get());
    public static final DeferredBlock<BlockTileEntity> ELECTROLYZER = machine("electrolyzer", () -> Ic2BlockEntities.ELECTROLYZER.get());
    public static final DeferredBlock<ic2.core.block.misc.BlockPoleFence> IRON_FENCE = REGISTRY.register("iron_fence", () -> new ic2.core.block.misc.BlockPoleFence(metal(1.5f, 5).noOcclusion()));
    public static final DeferredBlock<BlockTileEntity> CANNER = machine("canner", () -> Ic2BlockEntities.CANNER.get());
    public static final DeferredBlock<BlockTileEntity> SOLID_CANNER = machine("solid_canner", () -> Ic2BlockEntities.SOLID_CANNER.get());
    public static final DeferredBlock<BlockTileEntity> FLUID_BOTTLER = machine("fluid_bottler", () -> Ic2BlockEntities.FLUID_BOTTLER.get());
    public static final DeferredBlock<BlockTileEntity> MINER = machine("miner", () -> Ic2BlockEntities.MINER.get());
    public static final DeferredBlock<BlockTileEntity> PUMP = machine("pump", () -> Ic2BlockEntities.PUMP.get());
    public static final DeferredBlock<ic2.core.block.machine.block.BlockMiningPipe> MINING_PIPE = REGISTRY.register("mining_pipe", () -> new ic2.core.block.machine.block.BlockMiningPipe(metal(6, 10).noOcclusion(), false));
    public static final DeferredBlock<ic2.core.block.machine.block.BlockMiningPipe> MINING_PIPE_TIP = REGISTRY.register("mining_pipe_tip", () -> new ic2.core.block.machine.block.BlockMiningPipe(metal(6, 10), true));
    public static final DeferredBlock<BlockTileEntity> GENERATOR = generator("generator", () -> Ic2BlockEntities.GENERATOR.get());
    public static final DeferredBlock<BlockTileEntity> GEOTHERMAL_GENERATOR = generator("geothermal_generator", () -> Ic2BlockEntities.GEOTHERMAL_GENERATOR.get());
    public static final DeferredBlock<BlockTileEntity> WATER_MILL = generator("water_mill", () -> Ic2BlockEntities.WATER_MILL.get());
    public static final DeferredBlock<BlockTileEntity> WIND_MILL = generator("wind_mill", () -> Ic2BlockEntities.WIND_MILL.get());
    public static final DeferredBlock<BlockTileEntity> SOLAR_PANEL = generator("solar_panel", () -> Ic2BlockEntities.SOLAR_PANEL.get());
    public static final DeferredBlock<BlockElectric> BATBOX = electric("batbox", () -> Ic2BlockEntities.BATBOX.get());
    public static final DeferredBlock<BlockElectric> CESU = electric("cesu", () -> Ic2BlockEntities.CESU.get());
    public static final DeferredBlock<BlockElectric> MFE = electric("mfe", () -> Ic2BlockEntities.MFE.get());
    public static final DeferredBlock<BlockElectric> MFSU = electric("mfsu", () -> Ic2BlockEntities.MFSU.get());
    public static final DeferredBlock<BlockElectric> LV_TRANSFORMER = electric("lv_transformer", () -> Ic2BlockEntities.LV_TRANSFORMER.get());
    public static final DeferredBlock<BlockElectric> MV_TRANSFORMER = electric("mv_transformer", () -> Ic2BlockEntities.MV_TRANSFORMER.get());
    public static final DeferredBlock<BlockElectric> HV_TRANSFORMER = electric("hv_transformer", () -> Ic2BlockEntities.HV_TRANSFORMER.get());
    public static final DeferredBlock<BlockElectric> EV_TRANSFORMER = electric("ev_transformer", () -> Ic2BlockEntities.EV_TRANSFORMER.get());

    public static final DeferredBlock<ic2.core.block.wiring.BlockChargepad> CHARGEPAD_BATBOX = chargepad("chargepad_batbox", () -> Ic2BlockEntities.CHARGEPAD_BATBOX.get());
    public static final DeferredBlock<ic2.core.block.wiring.BlockChargepad> CHARGEPAD_CESU = chargepad("chargepad_cesu", () -> Ic2BlockEntities.CHARGEPAD_CESU.get());
    public static final DeferredBlock<ic2.core.block.wiring.BlockChargepad> CHARGEPAD_MFE = chargepad("chargepad_mfe", () -> Ic2BlockEntities.CHARGEPAD_MFE.get());
    public static final DeferredBlock<ic2.core.block.wiring.BlockChargepad> CHARGEPAD_MFSU = chargepad("chargepad_mfsu", () -> Ic2BlockEntities.CHARGEPAD_MFSU.get());
    public static final java.util.Map<ic2.core.block.wiring.CableType, DeferredBlock<ic2.core.block.wiring.BlockCable>> CABLES = new java.util.EnumMap<>(ic2.core.block.wiring.CableType.class);
    static {
        for (var type : ic2.core.block.wiring.CableType.values())
            CABLES.put(type, REGISTRY.register(type.id(), () -> new ic2.core.block.wiring.BlockCable(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.2f, 6).sound(SoundType.WOOL).noOcclusion().dynamicShape(), type)));
    }

    static { ic2.core.fluid.Ic2Fluids.ALL.size(); }

    private static DeferredBlock<BlockTileEntity> machine(String name, Supplier<BlockEntityType<? extends TileEntityBlock>> type) { return REGISTRY.register(name, () -> new BlockTileEntity(metal(2, 10), type)); }
    private static DeferredBlock<BlockTileEntity> generator(String name, Supplier<BlockEntityType<? extends TileEntityBlock>> type) { return REGISTRY.register(name, () -> new BlockTileEntity(metal(3, 10), type)); }
    private static DeferredBlock<ic2.core.block.wiring.BlockChargepad> chargepad(String name, Supplier<BlockEntityType<? extends TileEntityBlock>> type) { return REGISTRY.register(name, () -> new ic2.core.block.wiring.BlockChargepad(metal(1.5f, 10).noOcclusion(), type)); }
    private static DeferredBlock<BlockElectric> electric(String name, Supplier<BlockEntityType<? extends TileEntityBlock>> type) { return REGISTRY.register(name, () -> new BlockElectric(metal(1.5f, 10), type)); }

    private static BlockBehaviour.Properties personal() { return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(-1, 6000000).noLootTable().sound(SoundType.METAL).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK); }
    public static final DeferredBlock<ic2.core.block.personal.BlockPersonal> PERSONAL_CHEST = REGISTRY.register("personal_chest", () -> new ic2.core.block.personal.BlockPersonal(personal().noOcclusion(), () -> Ic2BlockEntities.PERSONAL_CHEST.get(), true));
    public static final DeferredBlock<ic2.core.block.personal.BlockPersonal> TRADE_O_MAT = REGISTRY.register("trade_o_mat", () -> new ic2.core.block.personal.BlockPersonal(personal(), () -> Ic2BlockEntities.TRADE_O_MAT.get(), false));
    public static final DeferredBlock<ic2.core.block.personal.BlockPersonal> ENERGY_O_MAT = REGISTRY.register("energy_o_mat", () -> new ic2.core.block.personal.BlockPersonal(personal(), () -> Ic2BlockEntities.ENERGY_O_MAT.get(), false));

    public static final DeferredBlock<ic2.core.block.misc.BlockBarrel> BARREL = REGISTRY.register("barrel", () -> new ic2.core.block.misc.BlockBarrel(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1).sound(SoundType.WOOD).ignitedByLava()));

    private Ic2Blocks() {}
}
