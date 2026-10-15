package ic2.core.energy;

import ic2.api.energy.tile.IEnergyConductor;
import java.util.ArrayList;
import java.util.List;

/** Energy net node; optimized copies keep a parent and delegate their state to it (getTop). */
class Node {
    final int uid;
    final Tile tile;
    final NodeType nodeType;
    private final Node parent;
    private boolean isExtraNode;
    private int tier;
    private double amount, voltage, currentIn, currentOut;
    private Grid grid;
    List<NodeLink> links = new ArrayList<>();
    private final MutableNodeStats lastNodeStats = new MutableNodeStats();

    Node(EnergyNetLocal energyNet, Tile tile, NodeType nodeType) {
        this.uid = EnergyNetLocal.getNextNodeUid();
        this.tile = tile;
        this.nodeType = nodeType;
        this.parent = null;
    }

    Node(Node node) {
        this.uid = node.uid;
        this.tile = node.tile;
        this.nodeType = node.nodeType;
        this.parent = node;
        for (NodeLink link : node.links) links.add(new NodeLink(link));
    }

    double getInnerLoss() {
        return switch (nodeType) {
            case Source, Sink -> 0.4;
            case Conductor -> ((IEnergyConductor)tile.entity).getConductionLoss();
        };
    }

    boolean isExtraNode() { return getTop().isExtraNode; }
    void setExtraNode(boolean extra) {
        if (nodeType == NodeType.Conductor) throw new IllegalStateException("A conductor can't be an extra node.");
        getTop().isExtraNode = extra;
    }
    int getTier() { return getTop().tier; }
    void setTier(int tier) {
        if (tier < 0) tier = 0;
        else if (tier > 20 && (tier != Integer.MAX_VALUE || nodeType != NodeType.Sink)) tier = 20;
        getTop().tier = tier;
    }
    double getAmount() { return getTop().amount; }
    void setAmount(double amount) { getTop().amount = amount; }
    double getVoltage() { return getTop().voltage; }
    void setVoltage(double voltage) { getTop().voltage = voltage; }
    void resetCurrents() { getTop().currentIn = 0; getTop().currentOut = 0; }
    void addCurrent(double current) { if (current >= 0) getTop().currentIn += current; else getTop().currentOut += -current; }
    Node getTop() { return parent != null ? parent.getTop() : this; }
    NodeLink getConnectionTo(Node node) {
        for (NodeLink link : links) if (link.getNeighbor(this) == node) return link;
        return null;
    }
    MutableNodeStats getStats() { return lastNodeStats; }
    void updateStats() { lastNodeStats.set(currentIn, currentOut, voltage); }
    Grid getGrid() { return getTop().grid; }
    void setGrid(Grid grid) {
        if (grid == null) throw new NullPointerException("null grid");
        getTop().grid = grid;
    }
    void clearGrid() { getTop().grid = null; }

    @Override public String toString() {
        String type = switch (nodeType) { case Source -> "E"; case Sink -> "A"; case Conductor -> "C"; };
        return tile.entity.getClass().getSimpleName().replace("TileEntity", "") + "|" + type + "|" + tier + "|" + uid;
    }
}
