package ic2.core.energy;

import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IMetaDelegate;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

class Tile {
    final BlockEntity entity;
    final List<BlockEntity> positions;
    final List<Node> nodes = new ArrayList<>();
    final double maxCurrent;

    Tile(EnergyNetLocal energyNet, BlockEntity te) {
        entity = te;
        if (te instanceof IMetaDelegate delegate) {
            positions = new ArrayList<>(delegate.getSubTiles());
            if (positions.isEmpty()) throw new RuntimeException("Tile entity " + te + " must return at least 1 sub tile for IMetaDelegate.getSubTiles().");
        } else positions = List.of(te);
        if (te instanceof IEnergySource) nodes.add(new Node(energyNet, this, NodeType.Source));
        if (te instanceof IEnergySink) nodes.add(new Node(energyNet, this, NodeType.Sink));
        if (te instanceof IEnergyConductor conductor) {
            nodes.add(new Node(energyNet, this, NodeType.Conductor));
            maxCurrent = conductor.getConductorBreakdownEnergy();
        } else maxCurrent = Double.MAX_VALUE;
    }

    void addExtraNode(Node node) { node.setExtraNode(true); nodes.add(node); }

    boolean removeExtraNode(Node node) {
        boolean canBeRemoved = false;
        if (node.isExtraNode()) canBeRemoved = true;
        else for (Node other : nodes) if (other != node && other.nodeType == node.nodeType && other.isExtraNode()) { other.setExtraNode(false); canBeRemoved = true; break; }
        if (canBeRemoved) nodes.remove(node);
        return canBeRemoved;
    }

    BlockEntity getSubEntityAt(BlockPos pos) {
        for (BlockEntity te : positions) if (te.getBlockPos().equals(pos)) return te;
        return null;
    }
}
