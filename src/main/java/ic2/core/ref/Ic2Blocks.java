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
    private static BlockBehaviour.Properties metal(float hardness, float resistance) { return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(hardness, resistance).requiresCorrectToolForDrops().sound(SoundType.METAL); }

    public static final DeferredBlock<Block> TIN_ORE = REGISTRY.register("tin_ore", () -> new Block(stone(3, 5)));
    public static final DeferredBlock<Block> URANIUM_ORE = REGISTRY.register("uranium_ore", () -> new Block(stone(4, 6)));
    public static final DeferredBlock<Block> LEAD_ORE = REGISTRY.register("lead_ore", () -> new Block(stone(2, 4)));
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

    public static final java.util.Map<ic2.core.block.wiring.CableType, DeferredBlock<ic2.core.block.wiring.BlockCable>> CABLES = new java.util.EnumMap<>(ic2.core.block.wiring.CableType.class);
    static {
        for (var type : ic2.core.block.wiring.CableType.values())
            CABLES.put(type, REGISTRY.register(type.id(), () -> new ic2.core.block.wiring.BlockCable(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.2f, 6).sound(SoundType.WOOL).noOcclusion(), type)));
    }

    static { ic2.core.fluid.Ic2Fluids.ALL.size(); }

    private static DeferredBlock<BlockTileEntity> machine(String name, Supplier<BlockEntityType<? extends TileEntityBlock>> type) { return REGISTRY.register(name, () -> new BlockTileEntity(metal(2, 10), type)); }
    private static DeferredBlock<BlockTileEntity> generator(String name, Supplier<BlockEntityType<? extends TileEntityBlock>> type) { return REGISTRY.register(name, () -> new BlockTileEntity(metal(3, 10), type)); }
    private static DeferredBlock<BlockElectric> electric(String name, Supplier<BlockEntityType<? extends TileEntityBlock>> type) { return REGISTRY.register(name, () -> new BlockElectric(metal(1.5f, 10), type)); }

    private Ic2Blocks() {}
}
