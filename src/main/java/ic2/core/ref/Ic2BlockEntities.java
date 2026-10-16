package ic2.core.ref;

import ic2.core.IC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.generator.tileentity.TileEntityGenerator;
import ic2.core.block.generator.tileentity.TileEntitySolarGenerator;
import ic2.core.block.machine.tileentity.*;
import ic2.core.block.wiring.TileEntityElectricBlock;
import ic2.core.block.wiring.TileEntityTransformer;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2BlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IC2.MODID);

    public static final Supplier<BlockEntityType<TileEntityIronFurnace>> IRON_FURNACE = reg("iron_furnace", TileEntityIronFurnace::new, Ic2Blocks.IRON_FURNACE);
    public static final Supplier<BlockEntityType<TileEntityElectricFurnace>> ELECTRIC_FURNACE = reg("electric_furnace", TileEntityElectricFurnace::new, Ic2Blocks.ELECTRIC_FURNACE);
    public static final Supplier<BlockEntityType<TileEntityMacerator>> MACERATOR = reg("macerator", TileEntityMacerator::new, Ic2Blocks.MACERATOR);
    public static final Supplier<BlockEntityType<TileEntityExtractor>> EXTRACTOR = reg("extractor", TileEntityExtractor::new, Ic2Blocks.EXTRACTOR);
    public static final Supplier<BlockEntityType<TileEntityCompressor>> COMPRESSOR = reg("compressor", TileEntityCompressor::new, Ic2Blocks.COMPRESSOR);
    public static final Supplier<BlockEntityType<TileEntityRecycler>> RECYCLER = reg("recycler", TileEntityRecycler::new, Ic2Blocks.RECYCLER);
    public static final Supplier<BlockEntityType<TileEntityInduction>> INDUCTION_FURNACE = reg("induction_furnace", TileEntityInduction::new, Ic2Blocks.INDUCTION_FURNACE);
    public static final Supplier<BlockEntityType<TileEntityMetalFormer>> METAL_FORMER = reg("metal_former", TileEntityMetalFormer::new, Ic2Blocks.METAL_FORMER);
    public static final Supplier<BlockEntityType<TileEntityOreWashing>> ORE_WASHING_PLANT = reg("ore_washing_plant", TileEntityOreWashing::new, Ic2Blocks.ORE_WASHING_PLANT);
    public static final Supplier<BlockEntityType<TileEntityCentrifuge>> THERMAL_CENTRIFUGE = reg("thermal_centrifuge", TileEntityCentrifuge::new, Ic2Blocks.THERMAL_CENTRIFUGE);
    public static final Supplier<BlockEntityType<TileEntityBlockCutter>> BLOCK_CUTTER = reg("block_cutter", TileEntityBlockCutter::new, Ic2Blocks.BLOCK_CUTTER);
    public static final Supplier<BlockEntityType<TileEntityBlastFurnace>> BLAST_FURNACE = reg("blast_furnace", TileEntityBlastFurnace::new, Ic2Blocks.BLAST_FURNACE);
    public static final Supplier<BlockEntityType<ic2.core.block.heatgenerator.TileEntityHeatGenerators.Solid>> SOLID_HEAT_GENERATOR = reg("solid_heat_generator", ic2.core.block.heatgenerator.TileEntityHeatGenerators.Solid::new, Ic2Blocks.SOLID_HEAT_GENERATOR);
    public static final Supplier<BlockEntityType<ic2.core.block.heatgenerator.TileEntityHeatGenerators.RT>> RT_HEAT_GENERATOR = reg("rt_heat_generator", ic2.core.block.heatgenerator.TileEntityHeatGenerators.RT::new, Ic2Blocks.RT_HEAT_GENERATOR);
    public static final Supplier<BlockEntityType<ic2.core.block.heatgenerator.TileEntityHeatGenerators.Electric>> ELECTRIC_HEAT_GENERATOR = reg("electric_heat_generator", ic2.core.block.heatgenerator.TileEntityHeatGenerators.Electric::new, Ic2Blocks.ELECTRIC_HEAT_GENERATOR);
    public static final Supplier<BlockEntityType<TileEntityGenerator>> GENERATOR = reg("generator", TileEntityGenerator::new, Ic2Blocks.GENERATOR);
    public static final Supplier<BlockEntityType<ic2.core.block.generator.tileentity.TileEntityGeoGenerator>> GEOTHERMAL_GENERATOR = reg("geothermal_generator", ic2.core.block.generator.tileentity.TileEntityGeoGenerator::new, Ic2Blocks.GEOTHERMAL_GENERATOR);
    public static final Supplier<BlockEntityType<ic2.core.block.generator.tileentity.TileEntityWaterGenerator>> WATER_MILL = reg("water_mill", ic2.core.block.generator.tileentity.TileEntityWaterGenerator::new, Ic2Blocks.WATER_MILL);
    public static final Supplier<BlockEntityType<ic2.core.block.generator.tileentity.TileEntityWindGenerator>> WIND_MILL = reg("wind_mill", ic2.core.block.generator.tileentity.TileEntityWindGenerator::new, Ic2Blocks.WIND_MILL);
    public static final Supplier<BlockEntityType<TileEntitySolarGenerator>> SOLAR_PANEL = reg("solar_panel", TileEntitySolarGenerator::new, Ic2Blocks.SOLAR_PANEL);
    public static final Supplier<BlockEntityType<TileEntityElectricBlock.BatBox>> BATBOX = reg("batbox", TileEntityElectricBlock.BatBox::new, Ic2Blocks.BATBOX);
    public static final Supplier<BlockEntityType<TileEntityElectricBlock.CESU>> CESU = reg("cesu", TileEntityElectricBlock.CESU::new, Ic2Blocks.CESU);
    public static final Supplier<BlockEntityType<TileEntityElectricBlock.MFE>> MFE = reg("mfe", TileEntityElectricBlock.MFE::new, Ic2Blocks.MFE);
    public static final Supplier<BlockEntityType<TileEntityElectricBlock.MFSU>> MFSU = reg("mfsu", TileEntityElectricBlock.MFSU::new, Ic2Blocks.MFSU);
    public static final Supplier<BlockEntityType<TileEntityTransformer.LV>> LV_TRANSFORMER = reg("lv_transformer", TileEntityTransformer.LV::new, Ic2Blocks.LV_TRANSFORMER);
    public static final Supplier<BlockEntityType<TileEntityTransformer.MV>> MV_TRANSFORMER = reg("mv_transformer", TileEntityTransformer.MV::new, Ic2Blocks.MV_TRANSFORMER);
    public static final Supplier<BlockEntityType<TileEntityTransformer.HV>> HV_TRANSFORMER = reg("hv_transformer", TileEntityTransformer.HV::new, Ic2Blocks.HV_TRANSFORMER);
    public static final Supplier<BlockEntityType<TileEntityTransformer.EV>> EV_TRANSFORMER = reg("ev_transformer", TileEntityTransformer.EV::new, Ic2Blocks.EV_TRANSFORMER);

    public static final Supplier<BlockEntityType<ic2.core.block.wiring.TileEntityCable>> CABLE = REGISTRY.register("cable", () -> BlockEntityType.Builder.of(ic2.core.block.wiring.TileEntityCable::new,
        Ic2Blocks.CABLES.values().stream().map(d -> (Block)d.get()).filter(b -> ((ic2.core.block.wiring.BlockCable)b).type != ic2.core.block.wiring.CableType.detector && ((ic2.core.block.wiring.BlockCable)b).type != ic2.core.block.wiring.CableType.splitter).toArray(Block[]::new)).build(null));
    public static final Supplier<BlockEntityType<ic2.core.block.wiring.TileEntityCableDetector>> DETECTOR_CABLE = REGISTRY.register("detector_cable", () -> BlockEntityType.Builder.of(ic2.core.block.wiring.TileEntityCableDetector::new, Ic2Blocks.CABLES.get(ic2.core.block.wiring.CableType.detector).get()).build(null));
    public static final Supplier<BlockEntityType<ic2.core.block.wiring.TileEntityCableSplitter>> SPLITTER_CABLE = REGISTRY.register("splitter_cable", () -> BlockEntityType.Builder.of(ic2.core.block.wiring.TileEntityCableSplitter::new, Ic2Blocks.CABLES.get(ic2.core.block.wiring.CableType.splitter).get()).build(null));

    private static <T extends TileEntityBlock> Supplier<BlockEntityType<T>> reg(String name, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block> block) {
        return REGISTRY.register(name, () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
    }

    private Ic2BlockEntities() {}
}
