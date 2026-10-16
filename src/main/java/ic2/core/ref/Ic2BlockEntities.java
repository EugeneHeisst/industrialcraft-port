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
    public static final Supplier<BlockEntityType<ic2.core.block.generator.tileentity.TileEntityGenerators2.RTG>> RT_GENERATOR = reg("rt_generator", ic2.core.block.generator.tileentity.TileEntityGenerators2.RTG::new, Ic2Blocks.RT_GENERATOR);
    public static final Supplier<BlockEntityType<ic2.core.block.generator.tileentity.TileEntityGenerators2.Semifluid>> SEMIFLUID_GENERATOR = reg("semifluid_generator", ic2.core.block.generator.tileentity.TileEntityGenerators2.Semifluid::new, Ic2Blocks.SEMIFLUID_GENERATOR);
    public static final Supplier<BlockEntityType<ic2.core.block.generator.tileentity.TileEntityGenerators2.Stirling>> STIRLING_GENERATOR = reg("stirling_generator", ic2.core.block.generator.tileentity.TileEntityGenerators2.Stirling::new, Ic2Blocks.STIRLING_GENERATOR);
    public static final Supplier<BlockEntityType<ic2.core.block.generator.tileentity.TileEntityGenerators2.Kinetic>> KINETIC_GENERATOR = reg("kinetic_generator", ic2.core.block.generator.tileentity.TileEntityGenerators2.Kinetic::new, Ic2Blocks.KINETIC_GENERATOR);
    public static final Supplier<BlockEntityType<ic2.core.block.kineticgenerator.TileEntityKineticGenerators.Wind>> WIND_KINETIC_GENERATOR = reg("wind_kinetic_generator", ic2.core.block.kineticgenerator.TileEntityKineticGenerators.Wind::new, Ic2Blocks.WIND_KINETIC_GENERATOR);
    public static final Supplier<BlockEntityType<ic2.core.block.kineticgenerator.TileEntityKineticGenerators.Water>> WATER_KINETIC_GENERATOR = reg("water_kinetic_generator", ic2.core.block.kineticgenerator.TileEntityKineticGenerators.Water::new, Ic2Blocks.WATER_KINETIC_GENERATOR);
    public static final Supplier<BlockEntityType<ic2.core.block.kineticgenerator.TileEntityKineticGenerators.Electric>> ELECTRIC_KINETIC_GENERATOR = reg("electric_kinetic_generator", ic2.core.block.kineticgenerator.TileEntityKineticGenerators.Electric::new, Ic2Blocks.ELECTRIC_KINETIC_GENERATOR);
    public static final Supplier<BlockEntityType<ic2.core.block.kineticgenerator.TileEntityKineticGenerators.Manual>> MANUAL_KINETIC_GENERATOR = reg("manual_kinetic_generator", ic2.core.block.kineticgenerator.TileEntityKineticGenerators.Manual::new, Ic2Blocks.MANUAL_KINETIC_GENERATOR);
    public static final Supplier<BlockEntityType<TileEntityMatter>> MASS_FABRICATOR = reg("mass_fabricator", TileEntityMatter::new, Ic2Blocks.MASS_FABRICATOR);
    public static final Supplier<BlockEntityType<ic2.core.block.reactor.TileEntityNuclearReactor>> NUCLEAR_REACTOR = reg("nuclear_reactor", ic2.core.block.reactor.TileEntityNuclearReactor::new, Ic2Blocks.NUCLEAR_REACTOR);
    public static final Supplier<BlockEntityType<ic2.core.block.reactor.TileEntityReactorChamber>> REACTOR_CHAMBER = reg("reactor_chamber", ic2.core.block.reactor.TileEntityReactorChamber::new, Ic2Blocks.REACTOR_CHAMBER);
    public static final Supplier<BlockEntityType<ic2.core.block.reactor.TileEntityReactorPort.AccessHatch>> REACTOR_ACCESS_HATCH = reg("reactor_access_hatch", ic2.core.block.reactor.TileEntityReactorPort.AccessHatch::new, Ic2Blocks.REACTOR_ACCESS_HATCH);
    public static final Supplier<BlockEntityType<ic2.core.block.reactor.TileEntityReactorPort.RedstonePort>> REACTOR_REDSTONE_PORT = reg("reactor_redstone_port", ic2.core.block.reactor.TileEntityReactorPort.RedstonePort::new, Ic2Blocks.REACTOR_REDSTONE_PORT);
    public static final Supplier<BlockEntityType<ic2.core.block.reactor.TileEntityReactorPort.FluidPort>> REACTOR_FLUID_PORT = reg("reactor_fluid_port", ic2.core.block.reactor.TileEntityReactorPort.FluidPort::new, Ic2Blocks.REACTOR_FLUID_PORT);
    public static final Supplier<BlockEntityType<TileEntityTerra>> TERRAFORMER = reg("terraformer", TileEntityTerra::new, Ic2Blocks.TERRAFORMER);
    public static final Supplier<BlockEntityType<TileEntityScanner>> SCANNER = reg("scanner", TileEntityScanner::new, Ic2Blocks.SCANNER);
    public static final Supplier<BlockEntityType<TileEntityReplicator>> REPLICATOR = reg("replicator", TileEntityReplicator::new, Ic2Blocks.REPLICATOR);
    public static final Supplier<BlockEntityType<TileEntityPatternStorage>> PATTERN_STORAGE = reg("pattern_storage", TileEntityPatternStorage::new, Ic2Blocks.PATTERN_STORAGE);
    public static final Supplier<BlockEntityType<TileEntityTeleporter>> TELEPORTER = reg("teleporter", TileEntityTeleporter::new, Ic2Blocks.TELEPORTER);
    public static final Supplier<BlockEntityType<TileEntityTesla>> TESLA_COIL = reg("tesla_coil", TileEntityTesla::new, Ic2Blocks.TESLA_COIL);
    public static final Supplier<BlockEntityType<TileEntityMagnetizer>> MAGNETIZER = reg("magnetizer", TileEntityMagnetizer::new, Ic2Blocks.MAGNETIZER);
    public static final Supplier<BlockEntityType<TileEntityElectrolyzer>> ELECTROLYZER = reg("electrolyzer", TileEntityElectrolyzer::new, Ic2Blocks.ELECTROLYZER);
    public static final Supplier<BlockEntityType<TileEntityCanner>> CANNER = reg("canner", TileEntityCanner::new, Ic2Blocks.CANNER);
    public static final Supplier<BlockEntityType<TileEntitySolidCanner>> SOLID_CANNER = reg("solid_canner", TileEntitySolidCanner::new, Ic2Blocks.SOLID_CANNER);
    public static final Supplier<BlockEntityType<TileEntityFluidBottler>> FLUID_BOTTLER = reg("fluid_bottler", TileEntityFluidBottler::new, Ic2Blocks.FLUID_BOTTLER);
    public static final Supplier<BlockEntityType<TileEntityMiner>> MINER = reg("miner", TileEntityMiner::new, Ic2Blocks.MINER);
    public static final Supplier<BlockEntityType<TileEntityPump>> PUMP = reg("pump", TileEntityPump::new, Ic2Blocks.PUMP);
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

    public static final Supplier<BlockEntityType<ic2.core.block.wiring.TileEntityChargepad.BatBox>> CHARGEPAD_BATBOX = reg("chargepad_batbox", ic2.core.block.wiring.TileEntityChargepad.BatBox::new, Ic2Blocks.CHARGEPAD_BATBOX);
    public static final Supplier<BlockEntityType<ic2.core.block.wiring.TileEntityChargepad.CESU>> CHARGEPAD_CESU = reg("chargepad_cesu", ic2.core.block.wiring.TileEntityChargepad.CESU::new, Ic2Blocks.CHARGEPAD_CESU);
    public static final Supplier<BlockEntityType<ic2.core.block.wiring.TileEntityChargepad.MFE>> CHARGEPAD_MFE = reg("chargepad_mfe", ic2.core.block.wiring.TileEntityChargepad.MFE::new, Ic2Blocks.CHARGEPAD_MFE);
    public static final Supplier<BlockEntityType<ic2.core.block.wiring.TileEntityChargepad.MFSU>> CHARGEPAD_MFSU = reg("chargepad_mfsu", ic2.core.block.wiring.TileEntityChargepad.MFSU::new, Ic2Blocks.CHARGEPAD_MFSU);
    public static final Supplier<BlockEntityType<ic2.core.block.wiring.TileEntityCable>> CABLE = REGISTRY.register("cable", () -> BlockEntityType.Builder.of(ic2.core.block.wiring.TileEntityCable::new,
        Ic2Blocks.CABLES.values().stream().map(d -> (Block)d.get()).filter(b -> ((ic2.core.block.wiring.BlockCable)b).type != ic2.core.block.wiring.CableType.detector && ((ic2.core.block.wiring.BlockCable)b).type != ic2.core.block.wiring.CableType.splitter).toArray(Block[]::new)).build(null));
    public static final Supplier<BlockEntityType<ic2.core.block.wiring.TileEntityCableDetector>> DETECTOR_CABLE = REGISTRY.register("detector_cable", () -> BlockEntityType.Builder.of(ic2.core.block.wiring.TileEntityCableDetector::new, Ic2Blocks.CABLES.get(ic2.core.block.wiring.CableType.detector).get()).build(null));
    public static final Supplier<BlockEntityType<ic2.core.block.wiring.TileEntityCableSplitter>> SPLITTER_CABLE = REGISTRY.register("splitter_cable", () -> BlockEntityType.Builder.of(ic2.core.block.wiring.TileEntityCableSplitter::new, Ic2Blocks.CABLES.get(ic2.core.block.wiring.CableType.splitter).get()).build(null));

    public static final Supplier<BlockEntityType<ic2.core.block.machine.tileentity.TileEntitySortingMachine>> SORTING_MACHINE = reg("sorting_machine", ic2.core.block.machine.tileentity.TileEntitySortingMachine::new, Ic2Blocks.SORTING_MACHINE);
    public static final Supplier<BlockEntityType<ic2.core.block.machine.tileentity.TileEntityItemBuffer>> ITEM_BUFFER = reg("item_buffer", ic2.core.block.machine.tileentity.TileEntityItemBuffer::new, Ic2Blocks.ITEM_BUFFER);
    public static final Supplier<BlockEntityType<ic2.core.block.machine.tileentity.TileEntityCropmatron>> CROPMATRON = reg("cropmatron", ic2.core.block.machine.tileentity.TileEntityCropmatron::new, Ic2Blocks.CROPMATRON);
    public static final Supplier<BlockEntityType<ic2.core.block.machine.tileentity.TileEntityCropHarvester>> CROP_HARVESTER = reg("crop_harvester", ic2.core.block.machine.tileentity.TileEntityCropHarvester::new, Ic2Blocks.CROP_HARVESTER);
    public static final Supplier<BlockEntityType<ic2.core.crop.TileEntityCrop>> CROP = reg("crop", ic2.core.crop.TileEntityCrop::new, Ic2Blocks.CROP);

    private static <T extends TileEntityBlock> Supplier<BlockEntityType<T>> reg(String name, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block> block) {
        return REGISTRY.register(name, () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
    }

    private Ic2BlockEntities() {}
}
