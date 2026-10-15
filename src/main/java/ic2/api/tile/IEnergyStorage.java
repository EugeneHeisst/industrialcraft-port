package ic2.api.tile;

public interface IEnergyStorage {
    int getStored();
    void setStored(int energy);
    int addEnergy(int amount);
    int getCapacity();
    int getOutput();
    double getOutputEnergyUnitsPerTick();
    boolean isTeleporterCompatible(net.minecraft.core.Direction side);
}
