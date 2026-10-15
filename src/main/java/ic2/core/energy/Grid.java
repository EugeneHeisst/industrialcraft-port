package ic2.core.energy;

import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.core.IC2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import net.minecraft.core.Direction;

/**
 * Connected part of the energy net. Sources inject their offered EU as current, sinks are unit resistors to ground and links
 * conduct with 1/loss; solving the nodal equations yields each sink's share (original non-linear transfer model).
 */
class Grid {
    private final int uid;
    private final EnergyNetLocal energyNet;
    private final Map<Integer, Node> nodes = new HashMap<>();
    private boolean hasNonZeroVoltages, lastVoltagesNeedUpdate;
    private final Set<Integer> activeSources = new HashSet<>(), activeSinks = new HashSet<>();
    private final StructureCache cache = new StructureCache();
    private List<Node> calculation;
    private boolean failed;

    Grid(EnergyNetLocal energyNet) {
        uid = EnergyNetLocal.getNextGridUid();
        this.energyNet = energyNet;
        energyNet.grids.add(this);
    }

    @Override public String toString() { return "Grid " + uid; }

    void add(Node node, Collection<Node> neighbors) {
        invalidate();
        add(node);
        for (Node neighbor : neighbors) {
            double loss = (node.getInnerLoss() + neighbor.getInnerLoss()) / 2;
            NodeLink link = new NodeLink(node, neighbor, loss);
            node.links.add(link);
            neighbor.links.add(link);
        }
    }

    void remove(Node node) {
        invalidate();
        Iterator<NodeLink> it = node.links.iterator();
        while (it.hasNext()) {
            NodeLink link = it.next();
            Node neighbor = link.getNeighbor(node);
            neighbor.links.removeIf(l -> l == link);
            if (neighbor.links.isEmpty() && neighbor.tile.removeExtraNode(neighbor)) {
                it.remove();
                nodes.remove(neighbor.uid);
                neighbor.clearGrid();
            }
        }
        nodes.remove(node.uid);
        node.clearGrid();
        if (node.links.isEmpty()) energyNet.grids.remove(this);
        else if (node.links.size() > 1 && node.nodeType == NodeType.Conductor) {
            List<Set<Node>> nodeTable = new ArrayList<>();
            for (NodeLink start : node.links) {
                Set<Node> connected = new HashSet<>();
                Queue<Node> toCheck = new LinkedList<>(Arrays.asList(start.getNeighbor(node)));
                Node cNode;
                while ((cNode = toCheck.poll()) != null) {
                    if (connected.add(cNode) && cNode.nodeType == NodeType.Conductor)
                        for (NodeLink link : cNode.links) { Node n = link.getNeighbor(cNode); if (!connected.contains(n)) toCheck.add(n); }
                }
                nodeTable.add(connected);
            }
            for (int i = 1; i < node.links.size(); i++) {
                Set<Node> connected = nodeTable.get(i);
                Node neighbor = node.links.get(i).getNeighbor(node);
                boolean split = true;
                for (int j = 0; j < i; j++) if (nodeTable.get(j).contains(neighbor)) { split = false; break; }
                if (!split) continue;
                Grid grid = new Grid(energyNet);
                for (Node cNode : connected) {
                    boolean needsExtraNode = false;
                    if (!cNode.links.isEmpty() && cNode.nodeType != NodeType.Conductor)
                        for (int j = 0; j < i; j++) if (nodeTable.get(j).contains(cNode)) { needsExtraNode = true; break; }
                    if (needsExtraNode) {
                        Node extraNode = new Node(energyNet, cNode.tile, cNode.nodeType);
                        cNode.tile.addExtraNode(extraNode);
                        Iterator<NodeLink> lit = cNode.links.iterator();
                        while (lit.hasNext()) {
                            NodeLink link = lit.next();
                            if (connected.contains(link.getNeighbor(cNode))) { link.replaceNode(cNode, extraNode); extraNode.links.add(link); lit.remove(); }
                        }
                        grid.add(extraNode);
                    } else {
                        nodes.remove(cNode.uid);
                        cNode.clearGrid();
                        grid.add(cNode);
                    }
                }
            }
        }
    }

    void merge(Grid grid, Map<Node, Node> nodeReplacements) {
        invalidate();
        for (Node node : grid.nodes.values()) {
            boolean found = false;
            if (node.nodeType != NodeType.Conductor) {
                for (Node node2 : nodes.values()) {
                    if (node2.tile == node.tile && node2.nodeType == node.nodeType) {
                        found = true;
                        for (NodeLink link : node.links) { link.replaceNode(node, node2); node2.links.add(link); }
                        node2.tile.removeExtraNode(node);
                        nodeReplacements.put(node, node2);
                        break;
                    }
                }
            }
            if (!found) { node.clearGrid(); add(node); }
        }
        energyNet.grids.remove(grid);
    }

    void prepareCalculation() {
        activeSources.clear();
        activeSinks.clear();
        List<Node> dynamicTierNodes = new ArrayList<>();
        int maxSourceTier = 0;
        for (Node node : nodes.values()) {
            switch (node.nodeType) {
                case Source -> {
                    IEnergySource source = (IEnergySource)node.tile.entity;
                    node.setTier(source.getSourceTier());
                    node.setAmount(source.getOfferedEnergy());
                    if (node.getAmount() > 0) { activeSources.add(node.uid); maxSourceTier = Math.max(node.getTier(), maxSourceTier); }
                    else node.setAmount(0);
                }
                case Sink -> {
                    IEnergySink sink = (IEnergySink)node.tile.entity;
                    node.setTier(sink.getSinkTier());
                    node.setAmount(sink.getDemandedEnergy());
                    if (node.getAmount() > 0) { activeSinks.add(node.uid); if (node.getTier() == Integer.MAX_VALUE) dynamicTierNodes.add(node); }
                    else node.setAmount(0);
                }
                case Conductor -> node.setAmount(0);
            }
        }
        for (Node node : dynamicTierNodes) node.setTier(maxSourceTier);
    }

    /** Draws the offered energy from the sources and solves the grid; results are applied by {@link #finishCalculation}. */
    void startCalculation() {
        if (failed) return;
        boolean run = hasNonZeroVoltages;
        if (!activeSinks.isEmpty() && !activeSources.isEmpty()) {
            run = true;
            for (int nodeId : activeSources) {
                Node node = nodes.get(nodeId);
                int shareCount = 1;
                for (Node shared : node.tile.nodes)
                    if (shared.uid != nodeId && shared.nodeType == NodeType.Source && !shared.getGrid().activeSinks.isEmpty()) shareCount++;
                node.setAmount(node.getAmount() / shareCount);
                ((IEnergySource)node.tile.entity).drawEnergy(node.getAmount());
            }
        }
        if (!run) return;
        try {
            calculation = calculate();
        } catch (RuntimeException e) {
            IC2.LOG.warn("Energy net calculation failed for {}.", this, e);
            failed = true;
            calculation = null;
        }
    }

    void finishCalculation() {
        if (calculation == null) return;
        for (Node node : calculation) {
            Direction dir = node.links.isEmpty() ? null : node.links.get(0).getDirFrom(node);
            energyNet.addChange(node, dir, node.getAmount(), node.getVoltage());
        }
        calculation = null;
    }

    void updateStats() {
        if (!lastVoltagesNeedUpdate) return;
        lastVoltagesNeedUpdate = false;
        for (Node node : nodes.values()) node.updateStats();
    }

    List<Node> calculate() {
        lastVoltagesNeedUpdate = true;
        if (!activeSources.isEmpty() && !activeSinks.isEmpty()) {
            StructureCache.Data data = calculateDistribution();
            calculateEffects(data);
            activeSources.clear();
            activeSinks.clear();
            List<Node> ret = new ArrayList<>();
            for (Node node : data.activeNodes) if (node.nodeType == NodeType.Sink || node.nodeType == NodeType.Source) ret.add(node.getTop());
            hasNonZeroVoltages = true;
            return ret;
        }
        for (Node node : nodes.values()) { node.setVoltage(0); node.resetCurrents(); }
        activeSources.clear();
        activeSinks.clear();
        hasNonZeroVoltages = false;
        return new ArrayList<>();
    }

    private void add(Node node) {
        node.setGrid(this);
        Node prev = nodes.put(node.uid, node);
        if (prev != null) throw new IllegalStateException("duplicate node uid, new " + node + ", old " + prev);
    }

    private void invalidate() {
        finishCalculation();
        cache.clear();
    }

    private StructureCache.Data calculateDistribution() {
        StructureCache.Data data = cache.get(activeSources, activeSinks);
        if (!data.isInitialized) {
            copyForOptimize(data);
            optimize(data);
            data.activeNodes = new ArrayList<>(data.optimizedNodes.values());
            int size = data.activeNodes.size();
            data.sourceMatrix = new double[size];
            data.solver = LinearSolver.decompose(networkMatrix(data));
            if (data.solver == null) throw new RuntimeException("Can't decompose network matrix.");
            data.isInitialized = true;
        }
        for (int row = 0; row < data.activeNodes.size(); row++) {
            Node node = data.activeNodes.get(row);
            data.sourceMatrix[row] = node.nodeType == NodeType.Source ? node.getAmount() : 0;
        }
        data.resultMatrix = data.solver.solve(data.sourceMatrix);
        return data;
    }

    private static double[][] networkMatrix(StructureCache.Data data) {
        int size = data.activeNodes.size();
        double[][] m = new double[size][size];
        Map<Node, Integer> index = new HashMap<>();
        for (int i = 0; i < size; i++) index.put(data.activeNodes.get(i), i);
        for (int row = 0; row < size; row++) {
            Node node = data.activeNodes.get(row);
            for (NodeLink link : node.links) {
                Node neighbor = link.getNeighbor(node);
                if (neighbor == node) continue;
                Integer col = index.get(neighbor);
                if (col != null) m[row][col] -= 1 / link.loss;
                m[row][row] += 1 / link.loss;
            }
            if (node.nodeType == NodeType.Sink) m[row][row] += 1;
        }
        return m;
    }

    private void calculateEffects(StructureCache.Data data) {
        for (Node node : nodes.values()) { node.setVoltage(Double.NaN); node.resetCurrents(); }
        for (int row = 0; row < data.activeNodes.size(); row++) {
            Node node = data.activeNodes.get(row);
            node.setVoltage(data.resultMatrix[row]);
            switch (node.nodeType) {
                case Source -> node.setAmount(0);
                case Sink -> node.setAmount(node.getVoltage());
                default -> {}
            }
        }
        for (Node node : data.activeNodes) {
            for (NodeLink link : node.links) {
                if (link.nodeA != node) continue;
                Node nodeA = link.nodeA.getTop(), nodeB = link.nodeB.getTop();
                double totalLoss = link.loss;
                for (Node skipped : link.skippedNodes) {
                    skipped = skipped.getTop();
                    if (!Double.isNaN(skipped.getVoltage())) break;
                    NodeLink link2 = nodeA.getConnectionTo(skipped);
                    if (link2 == null) break;
                    skipped.setVoltage(nodeA.getVoltage() + (nodeB.getVoltage() - nodeA.getVoltage()) * (link2.loss / totalLoss));
                    link2.updateCurrent();
                    nodeA = skipped;
                    totalLoss -= link2.loss;
                }
                NodeLink last = nodeA.getConnectionTo(nodeB);
                if (last != null) last.updateCurrent();
            }
        }
    }

    private void copyForOptimize(StructureCache.Data data) {
        data.optimizedNodes = new HashMap<>();
        for (Node node : nodes.values())
            if (node.getAmount() > 0 || node.nodeType == NodeType.Conductor) data.optimizedNodes.put(node.uid, new Node(node));
        for (Node node : data.optimizedNodes.values()) {
            ListIterator<NodeLink> it = node.links.listIterator();
            while (it.hasNext()) {
                NodeLink link = it.next();
                Node neighbor = link.getNeighbor(node.uid);
                if ((neighbor.nodeType == NodeType.Sink || neighbor.nodeType == NodeType.Source) && neighbor.getAmount() <= 0) it.remove();
                else if (link.nodeA.uid == node.uid) {
                    link.nodeA = data.optimizedNodes.get(link.nodeA.uid);
                    link.nodeB = data.optimizedNodes.get(link.nodeB.uid);
                    List<Node> newSkipped = new ArrayList<>();
                    for (Node skipped : link.skippedNodes) newSkipped.add(data.optimizedNodes.get(skipped.uid));
                    link.skippedNodes = newSkipped;
                } else {
                    for (NodeLink reverse : data.optimizedNodes.get(link.nodeA.uid).links) {
                        if (reverse.nodeB.uid == node.uid && !node.links.contains(reverse)) { it.set(reverse); break; }
                    }
                }
            }
        }
    }

    private void optimize(StructureCache.Data data) {
        int removed;
        do {
            removed = 0;
            Iterator<Node> it = data.optimizedNodes.values().iterator();
            while (it.hasNext()) {
                Node node = it.next();
                if (node.nodeType != NodeType.Conductor) continue;
                if (node.links.size() < 2) {
                    it.remove();
                    removed++;
                    for (NodeLink link : node.links) link.getNeighbor(node).links.removeIf(l -> l == link);
                } else if (node.links.size() == 2) {
                    it.remove();
                    removed++;
                    NodeLink linkA = node.links.get(0), linkB = node.links.get(1);
                    Node neighborA = linkA.getNeighbor(node), neighborB = linkB.getNeighbor(node);
                    if (neighborA != neighborB) {
                        linkA.loss += linkB.loss;
                        if (linkA.nodeA == node) {
                            linkA.nodeA = neighborB;
                            linkA.dirFromA = linkB.getDirFrom(neighborB);
                            if (linkB.nodeA == node) Collections.reverse(linkB.skippedNodes);
                            linkB.skippedNodes.add(node);
                            linkB.skippedNodes.addAll(linkA.skippedNodes);
                            linkA.skippedNodes = linkB.skippedNodes;
                        } else {
                            linkA.nodeB = neighborB;
                            linkA.dirFromB = linkB.getDirFrom(neighborB);
                            if (linkB.nodeB == node) Collections.reverse(linkB.skippedNodes);
                            linkA.skippedNodes.add(node);
                            linkA.skippedNodes.addAll(linkB.skippedNodes);
                        }
                        ListIterator<NodeLink> it2 = neighborB.links.listIterator();
                        while (it2.hasNext()) if (it2.next() == linkB) { it2.set(linkA); break; }
                    } else {
                        neighborA.links.remove(linkA);
                        neighborB.links.remove(linkB);
                    }
                }
            }
        } while (removed > 0);
    }
}
