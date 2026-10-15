package ic2.api.energy.tile;

public interface IEnergySource extends IEnergyEmitter {
    double getOfferedEnergy();
    /** Negative amounts refund energy that could not be delivered. */
    void drawEnergy(double amount);
    int getSourceTier();
}
