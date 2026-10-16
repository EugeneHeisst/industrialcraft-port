package ic2.core.fluid;

import ic2.core.IC2;
import ic2.core.ref.Ic2Blocks;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.Registries;

/** The original IC2 fluids (ic2uumatter, ic2coolant, ...); gaseous ones are container-only. */
public final class Ic2Fluids {
    public static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, IC2.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, IC2.MODID);
    public static final List<Ic2Fluid> ALL = new ArrayList<>();

    public static final class Ic2Fluid {
        public final String name, texture;
        public final int color;
        public final boolean gaseous, hasFlowTexture;
        public final Supplier<FluidType> type;
        public Supplier<FlowingFluid> source, flowing;
        public DeferredBlock<LiquidBlock> block;

        Ic2Fluid(String name, String texture, int color, int density, int viscosity, int luminosity, int temperature, boolean gaseous, boolean flowTexture) {
            this.name = name;
            this.texture = texture;
            this.color = color;
            this.gaseous = gaseous;
            this.hasFlowTexture = flowTexture;
            type = TYPES.register(name, () -> new FluidType(FluidType.Properties.create().density(density).viscosity(viscosity).lightLevel(luminosity).temperature(temperature).descriptionId("fluid.ic2." + name)));
            BaseFlowingFluid.Properties[] props = new BaseFlowingFluid.Properties[1];
            source = FLUIDS.register(name, () -> new BaseFlowingFluid.Source(props[0]));
            flowing = FLUIDS.register("flowing_" + name, () -> new BaseFlowingFluid.Flowing(props[0]));
            props[0] = new BaseFlowingFluid.Properties(type, source, flowing);
            if (!gaseous) {
                block = Ic2Blocks.REGISTRY.register(name, () -> new Ic2LiquidBlock(source.get(), this, BlockBehaviour.Properties.of().mapColor(MapColor.WATER).replaceable().noCollission().strength(100).pushReaction(PushReaction.DESTROY).noLootTable().liquid().lightLevel(s -> luminosity)));
                props[0].block(block);
            }
            ALL.add(this);
        }
        public Fluid get() { return source.get(); }
    }

    public static final Ic2Fluid UU_MATTER = new Ic2Fluid("uu_matter", "uumatter", 3867955, 3000, 3000, 0, 300, false, true);
    public static final Ic2Fluid CONSTRUCTION_FOAM = new Ic2Fluid("construction_foam", "constructionfoam", 2105376, 10000, 50000, 0, 300, false, true);
    public static final Ic2Fluid COOLANT = new Ic2Fluid("coolant", "coolant", 1333866, 1000, 3000, 0, 300, false, true);
    public static final Ic2Fluid HOT_COOLANT = new Ic2Fluid("hot_coolant", "hotcoolant", 11872308, 1000, 3000, 0, 1200, false, true);
    public static final Ic2Fluid PAHOEHOE_LAVA = new Ic2Fluid("pahoehoe_lava", "pahoehoelava", 8090732, 50000, 250000, 10, 1200, false, false);
    public static final Ic2Fluid BIOMASS = new Ic2Fluid("biomass", "biomass", 3632933, 1000, 3000, 0, 300, false, true);
    public static final Ic2Fluid BIOGAS = new Ic2Fluid("biogas", "biogas", 10983500, 1000, 3000, 0, 300, true, false);
    public static final Ic2Fluid DISTILLED_WATER = new Ic2Fluid("distilled_water", "distilledwater", 4413173, 1000, 1000, 0, 300, false, true);
    public static final Ic2Fluid SUPERHEATED_STEAM = new Ic2Fluid("superheated_steam", "superheatedsteam", 13291985, -3000, 100, 0, 600, true, false);
    public static final Ic2Fluid STEAM = new Ic2Fluid("steam", "steam", 12369084, -800, 300, 0, 420, true, false);
    public static final Ic2Fluid HOT_WATER = new Ic2Fluid("hot_water", "hotwater", 4644607, 1000, 1000, 0, 350, false, true);

    private Ic2Fluids() {}

    /** Pahoehoe lava hardens into basalt next to cooler fluids or in light; hot water sources cool down to water unless heated by lava below. */
    public static class Ic2LiquidBlock extends LiquidBlock {
        private final Ic2Fluid fluid;
        public Ic2LiquidBlock(FlowingFluid f, Ic2Fluid fluid, Properties properties) { super(f, properties.randomTicks()); this.fluid = fluid; }

        @Override
        protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            boolean source = state.getValue(LEVEL) == 0;
            if (fluid == PAHOEHOE_LAVA) {
                if (source && level.getMaxLocalRawBrightness(pos) >= random.nextInt(120)) level.setBlock(pos, Ic2Blocks.BASALT.get().defaultBlockState(), 3);
                else hardenFromNeighbors(level, pos, state);
            } else if (fluid == HOT_WATER) {
                if (source && !level.getBlockState(pos.below(2)).is(Blocks.LAVA) && !level.getBlockState(pos.below()).is(this) && random.nextInt(60) == 0)
                    level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
            }
        }

        @Override
        protected void neighborChanged(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.level.block.Block block, BlockPos from, boolean moving) {
            super.neighborChanged(state, level, pos, block, from, moving);
            if (fluid == PAHOEHOE_LAVA && level instanceof ServerLevel server) hardenFromNeighbors(server, pos, state);
        }

        private void hardenFromNeighbors(ServerLevel level, BlockPos pos, BlockState state) {
            for (Direction dir : Direction.values()) {
                var other = level.getFluidState(pos.relative(dir));
                if (other.isEmpty() || other.getType().isSame(fluid.get())) continue;
                if (other.getFluidType().getTemperature() <= fluid.type.get().getTemperature() / 4) {
                    level.setBlock(pos, state.getValue(LEVEL) == 0 ? Ic2Blocks.BASALT.get().defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
                    return;
                }
            }
        }
    }
}
