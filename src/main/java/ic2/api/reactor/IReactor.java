package ic2.api.reactor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Nuclear reactor as seen by reactor components; grid coordinates are column x (0..8) and row y (0..5). */
public interface IReactor {
    BlockPos getPosition();
    Level getWorld();
    int getHeat();
    void setHeat(int heat);
    int addHeat(int amount);
    int getMaxHeat();
    void setMaxHeat(int maxHeat);
    void addEmitHeat(int heat);
    float getHeatEffectModifier();
    void setHeatEffectModifier(float hem);
    float getReactorEnergyOutput();
    double getReactorEUEnergyOutput();
    float addOutput(float energy);
    /** EMPTY outside the grid or for an empty chamber */
    ItemStack getItemAt(int x, int y);
    void setItemAt(int x, int y, ItemStack stack);
    void explode();
    int getTickRate();
    boolean produceEnergy();
    void setRedstoneSignal(boolean redstone);
    boolean isFluidCooled();
}
