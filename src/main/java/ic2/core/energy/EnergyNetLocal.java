package ic2.core.energy;

import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IMetaDelegate;
import ic2.core.IC2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Energy net of one level. */
public final class EnergyNetLocal {
    private static int nextGridUid, nextNodeUid;
    final Set<Grid> grids = new HashSet<>();
    private List<Change> changes = new ArrayList<>();
    private final Map<BlockPos, Tile> registeredTiles = new HashMap<>();
    private Map<BlockEntity, Integer> pendingAdds = new WeakHashMap<>();
    private final Set<Tile> removedTiles = new HashSet<>();
    private boolean locked;
    final ServerLevel level;

    EnergyNetLocal(ServerLevel level) { this.level = level; }

    void addTileEntity(BlockEntity te) { addTileEntity(te, 0); }

    void addTileEntity(BlockEntity te, int retry) {
        if (!(te instanceof IEnergyTile)) { IC2.LOG.warn("EnergyNet.addTileEntity: {} doesn't implement IEnergyTile, aborting", te); return; }
        if (te.isRemoved()) return;
        if (locked) { pendingAdds.put(te, retry); return; }
        Tile tile = new Tile(this, te);
        ListIterator<BlockEntity> it = tile.positions.listIterator();
        while (it.hasNext()) {
            BlockEntity pos = it.next();
            BlockPos coords = pos.getBlockPos().immutable();
            Tile conflicting = registeredTiles.get(coords);
            if (conflicting != null) {
                if (te == conflicting.entity) conflicting = conflicting;
                else if (retry < 2) { pendingAdds.put(te, retry + 1); }
                else if (conflicting.entity.isRemoved()) { removeTileEntity(conflicting.entity); conflicting = null; }
                if (conflicting != null) {
                    it.previous();
                    while (it.hasPrevious()) registeredTiles.remove(it.previous().getBlockPos());
                    return;
                }
            }
            if (!level.isLoaded(coords)) {
                if (retry < 1) pendingAdds.put(te, retry + 1);
                it.previous();
                while (it.hasPrevious()) registeredTiles.remove(it.previous().getBlockPos());
                return;
            }
            registeredTiles.put(coords, tile);
        }
        addTileToGrids(tile);
        for (BlockEntity pos : tile.positions) notifyNeighbors(pos.getBlockPos());
    }

    void removeTileEntity(BlockEntity te) {
        if (locked) throw new IllegalStateException("removeTileEntity isn't allowed from this context");
        if (!(te instanceof IEnergyTile)) return;
        List<BlockEntity> positions = te instanceof IMetaDelegate d ? d.getSubTiles() : List.of(te);
        pendingAdds.remove(te);
        boolean removed = false;
        for (BlockEntity pos : positions) {
            BlockPos coords = pos.getBlockPos();
            Tile tile = registeredTiles.get(coords);
            if (tile == null || tile.entity != te) continue;
            if (!removed) { removeTileFromGrids(tile); removed = true; removedTiles.add(tile); }
            registeredTiles.remove(coords);
            if (level.isLoaded(coords)) notifyNeighbors(coords);
        }
    }

    private void notifyNeighbors(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos n = pos.relative(dir);
            if (level.isLoaded(n)) level.neighborChanged(n, level.getBlockState(pos).getBlock(), pos);
        }
    }

    NodeStats getNodeStats(BlockEntity te) {
        Tile tile = registeredTiles.get(te.getBlockPos());
        if (tile == null) return new NodeStats(0, 0, 0);
        double in = 0, out = 0, voltage = 0;
        for (Node node : tile.nodes) {
            NodeStats s = node.getStats();
            in += s.getEnergyIn(); out += s.getEnergyOut(); voltage = Math.max(voltage, s.getVoltage());
        }
        return new NodeStats(in, out, voltage);
    }

    BlockEntity getTileEntity(BlockPos pos) {
        Tile ret = registeredTiles.get(pos);
        return ret == null ? null : ret.entity;
    }

    void onTickEnd() {
        locked = true;
        for (Grid grid : grids) { grid.finishCalculation(); grid.updateStats(); }
        locked = false;
        processChanges();
        Map<BlockEntity, Integer> currentPendingAdds = pendingAdds;
        pendingAdds = new WeakHashMap<>();
        for (var entry : currentPendingAdds.entrySet()) addTileEntity(entry.getKey(), entry.getValue());
        locked = true;
        for (Grid grid : grids) grid.prepareCalculation();
        for (Grid grid : new ArrayList<>(grids)) grid.startCalculation();
        locked = false;
    }

    void addChange(Node node, Direction dir, double amount, double voltage) { changes.add(new Change(node, dir, amount, voltage)); }
    static int getNextGridUid() { return nextGridUid++; }
    static int getNextNodeUid() { return nextNodeUid++; }

    private void addTileToGrids(Tile tile) {
        List<Node> extraNodes = new ArrayList<>();
        for (Node node : tile.nodes) {
            List<Node> neighbors = new ArrayList<>();
            for (BlockEntity pos : tile.positions) {
                for (Direction dir : Direction.values()) {
                    BlockPos coords = pos.getBlockPos().relative(dir);
                    Tile neighborTile = registeredTiles.get(coords);
                    if (neighborTile == null || neighborTile == node.tile) continue;
                    for (Node neighbor : neighborTile.nodes) {
                        if (neighbor.isExtraNode()) continue;
                        boolean canEmit = false;
                        if ((node.nodeType == NodeType.Source || node.nodeType == NodeType.Conductor) && neighbor.nodeType != NodeType.Source) {
                            IEnergyEmitter emitter = (IEnergyEmitter)(pos instanceof IEnergyEmitter ? pos : node.tile.entity);
                            BlockEntity neighborSubTe = neighborTile.getSubEntityAt(coords);
                            IEnergyAcceptor acceptor = (IEnergyAcceptor)(neighborSubTe instanceof IEnergyAcceptor ? neighborSubTe : neighbor.tile.entity);
                            canEmit = emitter.emitsEnergyTo(neighbor.tile.entity, dir) && acceptor.acceptsEnergyFrom(node.tile.entity, dir.getOpposite());
                        }
                        boolean canAccept = false;
                        if (!canEmit && (node.nodeType == NodeType.Sink || node.nodeType == NodeType.Conductor) && neighbor.nodeType != NodeType.Sink) {
                            IEnergyAcceptor acceptor = (IEnergyAcceptor)(pos instanceof IEnergyAcceptor ? pos : node.tile.entity);
                            BlockEntity neighborSubTe = neighborTile.getSubEntityAt(coords);
                            IEnergyEmitter emitter = (IEnergyEmitter)(neighborSubTe instanceof IEnergyEmitter ? neighborSubTe : neighbor.tile.entity);
                            canAccept = acceptor.acceptsEnergyFrom(neighbor.tile.entity, dir) && emitter.emitsEnergyTo(node.tile.entity, dir.getOpposite());
                        }
                        if (canEmit || canAccept) neighbors.add(neighbor);
                    }
                }
            }
            if (neighbors.isEmpty()) {
                new Grid(this).add(node, neighbors);
                continue;
            }
            switch (node.nodeType) {
                case Conductor -> {
                    Grid grid = null;
                    for (Node neighbor : neighbors) if (neighbor.nodeType == NodeType.Conductor || neighbor.links.isEmpty()) { grid = neighbor.getGrid(); break; }
                    if (grid == null) grid = new Grid(this);
                    Map<Node, Node> replacements = new HashMap<>();
                    ListIterator<Node> it = neighbors.listIterator();
                    while (it.hasNext()) {
                        Node neighbor = it.next();
                        if (neighbor.getGrid() == grid) continue;
                        if (neighbor.nodeType != NodeType.Conductor && !neighbor.links.isEmpty()) {
                            boolean found = false;
                            for (int i = 0; i < it.previousIndex(); i++) {
                                Node neighbor2 = neighbors.get(i);
                                if (neighbor2.tile == neighbor.tile && neighbor2.nodeType == neighbor.nodeType && neighbor2.getGrid() == grid) { found = true; it.set(neighbor2); break; }
                            }
                            if (!found) {
                                neighbor = new Node(this, neighbor.tile, neighbor.nodeType);
                                neighbor.tile.addExtraNode(neighbor);
                                grid.add(neighbor, Collections.emptyList());
                                it.set(neighbor);
                            }
                        } else grid.merge(neighbor.getGrid(), replacements);
                    }
                    it = neighbors.listIterator();
                    while (it.hasNext()) { Node r = replacements.get(it.next()); if (r != null) it.set(r); }
                    grid.add(node, neighbors);
                }
                case Sink, Source -> {
                    List<List<Node>> groups = new ArrayList<>();
                    for (Node neighbor : neighbors) { List<Node> l = new ArrayList<>(); l.add(neighbor); groups.add(l); }
                    for (int i = 0; i < groups.size(); i++) {
                        List<Node> list = groups.get(i);
                        Node neighbor = list.get(0);
                        if (neighbor.nodeType != NodeType.Conductor && !neighbor.links.isEmpty()) {
                            neighbor = new Node(this, neighbor.tile, neighbor.nodeType);
                            neighbor.tile.addExtraNode(neighbor);
                            new Grid(this).add(neighbor, Collections.emptyList());
                            list.set(0, neighbor);
                        }
                        Node current;
                        if (i == 0) current = node;
                        else { current = new Node(this, tile, node.nodeType); current.setExtraNode(true); extraNodes.add(current); }
                        neighbor.getGrid().add(current, list);
                    }
                }
            }
        }
        for (Node node : extraNodes) tile.addExtraNode(node);
    }

    private void removeTileFromGrids(Tile tile) {
        for (Node node : new ArrayList<>(tile.nodes)) if (node.getGrid() != null) node.getGrid().remove(node);
    }

    private void processChanges() {
        for (Tile tile : removedTiles) {
            Iterator<Change> it = changes.iterator();
            while (it.hasNext()) {
                Change change = it.next();
                if (change.node.tile != tile) continue;
                Tile replacement = registeredTiles.get(change.node.tile.entity.getBlockPos());
                boolean valid = false;
                if (replacement != null) for (Node node : replacement.nodes)
                    if (node.nodeType == change.node.nodeType && node.getGrid() == change.node.getGrid()) { change.node = node; valid = true; break; }
                if (!valid) {
                    it.remove();
                    List<Change> same = new ArrayList<>();
                    for (Change c2 : changes) if (c2.node.nodeType == NodeType.Source && change.node.getGrid() == c2.node.getGrid()) same.add(c2);
                    for (Change c2 : same) c2.setAmount(c2.getAmount() - Math.abs(change.getAmount()) / same.size());
                }
            }
        }
        removedTiles.clear();
        for (Change change : changes) {
            if (change.node.nodeType != NodeType.Sink || !(change.getAmount() > 0)) continue;
            double returned = ((IEnergySink)change.node.tile.entity).injectEnergy(change.dir, change.getAmount(), change.getVoltage());
            if (returned > 0) {
                List<Change> same = new ArrayList<>();
                for (Change c2 : changes) if (c2.node.nodeType == NodeType.Source && change.node.getGrid() == c2.node.getGrid()) same.add(c2);
                for (Change c2 : same) c2.setAmount(c2.getAmount() - returned / same.size());
            }
        }
        for (Change change : changes)
            if (change.node.nodeType == NodeType.Source && change.getAmount() < 0) ((IEnergySource)change.node.tile.entity).drawEnergy(change.getAmount());
        changes.clear();
    }
}
