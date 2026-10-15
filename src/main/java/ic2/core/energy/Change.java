package ic2.core.energy;

import net.minecraft.core.Direction;

class Change {
    Node node;
    final Direction dir;
    private double amount, voltage;
    Change(Node node, Direction dir, double amount, double voltage) { this.node = node; this.dir = dir; setAmount(amount); setVoltage(voltage); }
    double getAmount() { return amount; }
    void setAmount(double amount) {
        double rounded = Math.rint(amount);
        this.amount = Math.abs(amount - rounded) < 0.001 ? rounded : amount;
    }
    double getVoltage() { return voltage; }
    private void setVoltage(double voltage) {
        double rounded = Math.rint(amount);
        this.voltage = Math.abs(voltage - rounded) < 0.001 ? rounded : voltage;
    }
    @Override public String toString() { return node + "@" + dir + " " + amount + " EU / " + voltage + " V"; }
}
