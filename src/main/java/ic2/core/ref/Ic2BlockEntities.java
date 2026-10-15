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
    public static final Supplier<BlockEntityType<TileEntityGenerator>> GENERATOR = reg("generator", TileEntityGenerator::new, Ic2Blocks.GENERATOR);
    public static final Supplier<BlockEntityType<TileEntitySolarGenerator>> SOLAR_PANEL = reg("solar_panel", TileEntitySolarGenerator::new, Ic2Blocks.SOLAR_PANEL);
    public static final Supplier<BlockEntityType<TileEntityElectricBlock.BatBox>> BATBOX = reg("batbox", TileEntityElectricBlock.BatBox::new, Ic2Blocks.BATBOX);
    public static final Supplier<BlockEntityType<TileEntityElectricBlock.CESU>> CESU = reg("cesu", TileEntityElectricBlock.CESU::new, Ic2Blocks.CESU);
    public static final Supplier<BlockEntityType<TileEntityElectricBlock.MFE>> MFE = reg("mfe", TileEntityElectricBlock.MFE::new, Ic2Blocks.MFE);
    public static final Supplier<BlockEntityType<TileEntityElectricBlock.MFSU>> MFSU = reg("mfsu", TileEntityElectricBlock.MFSU::new, Ic2Blocks.MFSU);
    public static final Supplier<BlockEntityType<TileEntityTransformer.LV>> LV_TRANSFORMER = reg("lv_transformer", TileEntityTransformer.LV::new, Ic2Blocks.LV_TRANSFORMER);
    public static final Supplier<BlockEntityType<TileEntityTransformer.MV>> MV_TRANSFORMER = reg("mv_transformer", TileEntityTransformer.MV::new, Ic2Blocks.MV_TRANSFORMER);
    public static final Supplier<BlockEntityType<TileEntityTransformer.HV>> HV_TRANSFORMER = reg("hv_transformer", TileEntityTransformer.HV::new, Ic2Blocks.HV_TRANSFORMER);
    public static final Supplier<BlockEntityType<TileEntityTransformer.EV>> EV_TRANSFORMER = reg("ev_transformer", TileEntityTransformer.EV::new, Ic2Blocks.EV_TRANSFORMER);

    private static <T extends TileEntityBlock> Supplier<BlockEntityType<T>> reg(String name, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block> block) {
        return REGISTRY.register(name, () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
    }

    private Ic2BlockEntities() {}
}
