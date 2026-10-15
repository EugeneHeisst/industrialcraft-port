package ic2.core.energy;

import ic2.api.energy.IEnergyNet;
import ic2.api.energy.NodeStats;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class EnergyNetGlobal implements IEnergyNet {
    private final Map<Level, EnergyNetLocal> nets = new WeakHashMap<>();

    private EnergyNetLocal get(Level level) {
        return level instanceof ServerLevel server ? nets.computeIfAbsent(level, l -> new EnergyNetLocal(server)) : null;
    }

    @Override public BlockEntity getTileEntity(Level level, BlockPos pos) { var net = get(level); return net == null ? null : net.getTileEntity(pos); }
    @Override public BlockEntity getNeighbor(BlockEntity te, Direction dir) { return getTileEntity(te.getLevel(), te.getBlockPos().relative(dir)); }
    @Override public NodeStats getNodeStats(BlockEntity te) { var net = get(te.getLevel()); return net == null ? new NodeStats(0, 0, 0) : net.getNodeStats(te); }
    @Override public double getPowerFromTier(int tier) { return tier < 14 ? (double)(8 << tier * 2) : 8 * Math.pow(4, tier); }
    @Override public int getTierFromPower(double power) { return power <= 0 ? 0 : (int)Math.ceil(Math.log(power / 8) / Math.log(4)); }
    @Override public void addTile(BlockEntity te) { var net = get(te.getLevel()); if (net != null) net.addTileEntity(te); }
    @Override public void removeTile(BlockEntity te) { var net = get(te.getLevel()); if (net != null) net.removeTileEntity(te); }

    @SubscribeEvent public void onLoad(EnergyTileLoadEvent event) { addTile(event.tile); }
    @SubscribeEvent public void onUnload(EnergyTileUnloadEvent event) { removeTile(event.tile); }
    @SubscribeEvent public void onLevelTick(LevelTickEvent.Post event) { if (event.getLevel() instanceof ServerLevel) { var net = nets.get(event.getLevel()); if (net != null) net.onTickEnd(); } }
    @SubscribeEvent public void onLevelUnload(LevelEvent.Unload event) { nets.remove(event.getLevel()); }
}
