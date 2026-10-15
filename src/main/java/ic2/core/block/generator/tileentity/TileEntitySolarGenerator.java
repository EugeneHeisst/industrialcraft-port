package ic2.core.block.generator.tileentity;

import ic2.core.ContainerBase;
import ic2.core.block.generator.container.ContainerSolarGenerator;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySolarGenerator extends TileEntityBaseGenerator {
    public final double solarbasevalue = 1;
    public int ticker;
    public boolean sunIsVisible;

    public TileEntitySolarGenerator(BlockPos pos, BlockState state) {
        super(Ic2BlockEntities.SOLAR_PANEL.get(), pos, state, 1, 1, 2);
        ticker = (int)(Math.random() * tickRate());
    }

    @Override public void onLoaded() { super.onLoaded(); updateSunVisibility(); }
    @Override public int gaugeFuelScaled(int i) { return i; }

    @Override
    public boolean gainEnergy() {
        if (ticker++ % tickRate() == 0) updateSunVisibility();
        if (!sunIsVisible) return false;
        storage += solarbasevalue * (getSkylight(level, worldPosition) / 15f);
        return true;
    }

    @Override public boolean gainFuel() { return false; }

    /** Block light value at the top of the column: sky light reduced by the time of day, like getBlockLightValue(x, 255, z). */
    private static int getSkylight(Level level, BlockPos pos) {
        BlockPos top = new BlockPos(pos.getX(), level.getMaxBuildHeight() - 1, pos.getZ());
        return Math.max(level.getBrightness(LightLayer.SKY, top) - level.getSkyDarken(), level.getBrightness(LightLayer.BLOCK, top));
    }

    public void updateSunVisibility() { sunIsVisible = isSunVisible(level, worldPosition.above()); }

    public static boolean isSunVisible(Level level, BlockPos pos) {
        if (getSkylight(level, pos) <= 4 || !level.dimensionType().hasSkyLight() || !level.canSeeSky(pos)) return false;
        Biome biome = level.getBiome(pos).value();
        return !biome.hasPrecipitation() || !level.isRaining() && !level.isThundering();
    }

    @Override public boolean needsFuel() { return true; }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerSolarGenerator(id, inventory, this); }
    public int tickRate() { return 128; }
    @Override public boolean delayActiveUpdate() { return true; }
}
