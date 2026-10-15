package ic2.api.energy;

public class NodeStats {
    protected double energyIn, energyOut, voltage;
    public NodeStats(double energyIn, double energyOut, double voltage) { this.energyIn = energyIn; this.energyOut = energyOut; this.voltage = voltage; }
    public double getEnergyIn() { return energyIn; }
    public double getEnergyOut() { return energyOut; }
    public double getVoltage() { return voltage; }
}
