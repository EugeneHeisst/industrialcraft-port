package ic2.core.block.machine.tileentity;

import ic2.api.tile.IEnergyStorage;
import ic2.core.audio.AudioSource;
import ic2.core.block.TileEntityBlock;
import ic2.core.network.TileEventPayload;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Sounds;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

/**
 * Redstone-powered teleporter linked to another one with a frequency transmitter. Moves the closest entity standing on it,
 * paying weight * (distance + 10)^0.7 * 5 EU from adjacent energy storage blocks.
 */
public class TileEntityTeleporter extends TileEntityBlock implements TileEventPayload.Listener {
    public boolean targetSet;
    public BlockPos target = BlockPos.ZERO;
    private AudioSource audioSource;
    private static final DustParticleOptions BLUE = new DustParticleOptions(new Vector3f(0, 0, 1), 1);

    public TileEntityTeleporter(BlockPos pos, BlockState state) { super(Ic2BlockEntities.TELEPORTER.get(), pos, state); }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) {
        super.readFromNBT(nbt, r);
        targetSet = nbt.getBoolean("targetSet");
        target = new BlockPos(nbt.getInt("targetX"), nbt.getInt("targetY"), nbt.getInt("targetZ"));
    }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) {
        super.writeToNBT(nbt, r);
        nbt.putBoolean("targetSet", targetSet);
        nbt.putInt("targetX", target.getX()); nbt.putInt("targetY", target.getY()); nbt.putInt("targetZ", target.getZ());
    }
    @Override protected void writeClientData(CompoundTag nbt, HolderLookup.Provider r) { nbt.putLong("target", target.asLong()); }
    @Override protected void readClientData(CompoundTag nbt, HolderLookup.Provider r) { target = BlockPos.of(nbt.getLong("target")); }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (!level.hasNeighborSignal(worldPosition) || !targetSet) { setActive(false); return; }
        if (!level.getBlockState(target).is(Ic2Blocks.TELEPORTER.get())) { targetSet = false; setActive(false); return; }
        setActive(true);
        Entity closest = null;
        double min = Double.MAX_VALUE;
        for (Entity e : level.getEntitiesOfClass(Entity.class, new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 3, worldPosition.getZ() + 2))) {
            if (e.isPassenger()) continue;
            double d = e.distanceToSqr(worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ());
            if (d < min) { min = d; closest = e; }
        }
        if (closest != null) teleport(closest);
    }

    public void teleport(Entity user) {
        double distance = Math.sqrt(worldPosition.distSqr(target));
        int weight = getWeightOf(user);
        if (weight == 0) return;
        int cost = (int)(weight * Math.pow(distance + 10, 0.7) * 5);
        if (cost > getAvailableEnergy()) return;
        consumeEnergy(cost);
        double x = target.getX() + 0.5, y = target.getY() + 1, z = target.getZ() + 0.5;
        if (user instanceof ServerPlayer sp) sp.teleportTo(sp.serverLevel(), x, y, z, sp.getYRot(), sp.getXRot());
        else user.moveTo(x, y, z, user.getYRot(), user.getXRot());
        TileEventPayload.send(this, 0);
    }

    private List<IEnergyStorage> storages() {
        List<IEnergyStorage> ret = new ArrayList<>();
        for (Direction d : Direction.values())
            if (ic2.core.util.Util.loadedBlockEntity(level, worldPosition.relative(d)) instanceof IEnergyStorage s && s.isTeleporterCompatible(d.getOpposite())) ret.add(s);
        return ret;
    }

    public int getAvailableEnergy() { int e = 0; for (IEnergyStorage s : storages()) e += s.getStored(); return e; }

    public void consumeEnergy(int energy) {
        List<IEnergyStorage> sources = storages();
        sources.removeIf(s -> s.getStored() <= 0);
        while (energy > 0 && !sources.isEmpty()) {
            int drain = (energy + sources.size() - 1) / sources.size();
            for (Iterator<IEnergyStorage> it = sources.iterator(); it.hasNext(); ) {
                IEnergyStorage s = it.next();
                if (drain > energy) drain = energy;
                if (s.getStored() <= drain) { energy -= s.getStored(); s.setStored(0); it.remove(); }
                else { energy -= drain; s.addEnergy(-drain); }
            }
        }
    }

    public int getWeightOf(Entity user) {
        int weight = 0;
        for (Entity ce : user.getSelfAndPassengers().toList()) {
            if (ce instanceof ItemEntity item) { ItemStack is = item.getItem(); weight += 100 * is.getCount() / is.getMaxStackSize(); }
            else if (ce instanceof Animal || ce instanceof AbstractMinecart || ce instanceof Boat) weight += 100;
            else if (ce instanceof Player p) {
                weight += 1000;
                for (ItemStack is : p.getInventory().items) if (!is.isEmpty()) weight += 100 * is.getCount() / is.getMaxStackSize();
            }
            else if (ce instanceof Ghast) weight += 2500;
            else if (ce instanceof EnderDragon) weight += 10000;
            else if (ce instanceof PathfinderMob) weight += 500;
            // teleporterUseInventoryWeight (default on): worn armor, and the held item of non-players
            if (ce instanceof LivingEntity living) {
                for (var slot : net.minecraft.world.entity.EquipmentSlot.values()) {
                    if (slot.getType() == net.minecraft.world.entity.EquipmentSlot.Type.HAND && (ce instanceof Player || slot == net.minecraft.world.entity.EquipmentSlot.OFFHAND)) continue;
                    ItemStack is = living.getItemBySlot(slot);
                    if (!is.isEmpty()) weight += 100 * is.getCount() / is.getMaxStackSize();
                }
            }
        }
        return weight;
    }

    public void setTarget(BlockPos pos) { targetSet = true; target = pos.immutable(); setChanged(); sendClientUpdate(); }

    private boolean lastClientActive;
    @Override
    protected void updateEntityClient() {
        boolean active = getActive();
        if (active) spawnBlueParticles(2, worldPosition);
        if (active == lastClientActive) return;
        lastClientActive = active;
        if (audioSource == null) audioSource = AudioSource.create(this, Ic2Sounds.TELEPORTER_CHARGED.get());
        if (audioSource != null) { if (active) audioSource.play(); else audioSource.stop(); }
    }

    private void spawnBlueParticles(int n, BlockPos p) {
        var r = level.random;
        for (int i = 0; i < n; i++) {
            level.addParticle(BLUE, p.getX() + r.nextFloat(), p.getY() + 1 + r.nextFloat(), p.getZ() + r.nextFloat(), 0, 0, 0);
            level.addParticle(BLUE, p.getX() + r.nextFloat(), p.getY() + 2 + r.nextFloat(), p.getZ() + r.nextFloat(), 0, 0, 0);
        }
    }

    @Override
    public void onNetworkEvent(int event) {
        if (event != 0) return;
        level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, Ic2Sounds.TELEPORTER_USE.get(), SoundSource.BLOCKS, 1, 1, false);
        level.playLocalSound(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, Ic2Sounds.TELEPORTER_USE.get(), SoundSource.BLOCKS, 1, 1, false);
        spawnBlueParticles(20, worldPosition);
        spawnBlueParticles(20, target);
    }

    @Override public void onUnloaded() { if (audioSource != null) { audioSource.remove(); audioSource = null; } super.onUnloaded(); }
}
