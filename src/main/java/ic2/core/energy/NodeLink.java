package ic2.core.energy;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

class NodeLink {
    Node nodeA, nodeB;
    Direction dirFromA, dirFromB;
    double loss;
    List<Node> skippedNodes = new ArrayList<>();

    NodeLink(Node nodeA, Node nodeB, double loss) {
        this(nodeA, nodeB, loss, null, null);
        calculateDirections();
    }
    NodeLink(NodeLink link) {
        this(link.nodeA, link.nodeB, link.loss, link.dirFromA, link.dirFromB);
        skippedNodes.addAll(link.skippedNodes);
    }
    private NodeLink(Node nodeA, Node nodeB, double loss, Direction dirFromA, Direction dirFromB) {
        this.nodeA = nodeA; this.nodeB = nodeB; this.loss = loss; this.dirFromA = dirFromA; this.dirFromB = dirFromB;
    }

    Node getNeighbor(Node node) { return nodeA == node ? nodeB : nodeA; }
    Node getNeighbor(int uid) { return nodeA.uid == uid ? nodeB : nodeA; }
    void replaceNode(Node oldNode, Node newNode) {
        if (nodeA == oldNode) nodeA = newNode;
        else if (nodeB == oldNode) nodeB = newNode;
        else throw new IllegalArgumentException("Node " + oldNode + " isn't in " + this + ".");
    }
    Direction getDirFrom(Node node) { return nodeA == node ? dirFromA : nodeB == node ? dirFromB : null; }
    void updateCurrent() {
        double currentAB = (nodeA.getVoltage() - nodeB.getVoltage()) / loss;
        nodeA.addCurrent(-currentAB);
        nodeB.addCurrent(currentAB);
    }
    private void calculateDirections() {
        for (BlockEntity posA : nodeA.tile.positions) for (BlockEntity posB : nodeB.tile.positions) {
            var d = posB.getBlockPos().subtract(posA.getBlockPos());
            for (Direction dir : Direction.values()) if (dir.getNormal().equals(d)) { dirFromA = dir; dirFromB = dir.getOpposite(); return; }
        }
        dirFromA = null; dirFromB = null;
    }
    @Override public String toString() { return "NodeLink:" + nodeA + "@" + dirFromA + "->" + nodeB + "@" + dirFromB; }
}
