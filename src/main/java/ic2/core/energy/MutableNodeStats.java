package ic2.core.energy;

import ic2.api.energy.NodeStats;

class MutableNodeStats extends NodeStats {
    MutableNodeStats() { super(0, 0, 0); }
    void set(double energyIn, double energyOut, double voltage) { this.energyIn = energyIn; this.energyOut = energyOut; this.voltage = voltage; }
}
