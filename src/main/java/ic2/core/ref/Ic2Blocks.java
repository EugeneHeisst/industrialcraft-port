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
    public static final DeferredBlock<Block> MACHINE = REGISTRY.register("machine", () -> new Block(metal(2, 10)));
    public static final DeferredBlock<Block> ADVANCED_MACHINE = REGISTRY.register("advanced_machine", () -> new Block(metal(2, 10)));

    public static final DeferredBlock<BlockTileEntity> IRON_FURNACE = machine("iron_furnace", () -> Ic2BlockEntities.IRON_FURNACE.get());
    public static final DeferredBlock<BlockTileEntity> ELECTRIC_FURNACE = machine("electric_furnace", () -> Ic2BlockEntities.ELECTRIC_FURNACE.get());
    public static final DeferredBlock<BlockTileEntity> MACERATOR = machine("macerator", () -> Ic2BlockEntities.MACERATOR.get());
    public static final DeferredBlock<BlockTileEntity> EXTRACTOR = machine("extractor", () -> Ic2BlockEntities.EXTRACTOR.get());
    public static final DeferredBlock<BlockTileEntity> COMPRESSOR = machine("compressor", () -> Ic2BlockEntities.COMPRESSOR.get());
    public static final DeferredBlock<BlockTileEntity> RECYCLER = machine("recycler", () -> Ic2BlockEntities.RECYCLER.get());
    public static final DeferredBlock<BlockTileEntity> GENERATOR = generator("generator", () -> Ic2BlockEntities.GENERATOR.get());
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

    private static DeferredBlock<BlockTileEntity> machine(String name, Supplier<BlockEntityType<? extends TileEntityBlock>> type) { return REGISTRY.register(name, () -> new BlockTileEntity(metal(2, 10), type)); }
    private static DeferredBlock<BlockTileEntity> generator(String name, Supplier<BlockEntityType<? extends TileEntityBlock>> type) { return REGISTRY.register(name, () -> new BlockTileEntity(metal(3, 10), type)); }
    private static DeferredBlock<BlockElectric> electric(String name, Supplier<BlockEntityType<? extends TileEntityBlock>> type) { return REGISTRY.register(name, () -> new BlockElectric(metal(1.5f, 10), type)); }

    private Ic2Blocks() {}
}
