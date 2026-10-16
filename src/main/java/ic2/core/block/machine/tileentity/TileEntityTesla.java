package ic2.core.block.machine.tileentity;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.core.block.TileEntityBlock;
import ic2.core.item.armor.ItemArmorUtility;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.Ic2DamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

/** Tesla coil: while redstone powered, every 32 ticks it shocks one living entity within 4 blocks for stored EU / 400 damage. */
public class TileEntityTesla extends TileEntityBlock implements IEnergySink {
    public double energy;
    private boolean addedToEnet;
    private int ticker = java.util.concurrent.ThreadLocalRandom.current().nextInt(32);

    public TileEntityTesla(BlockPos pos, BlockState state) { super(Ic2BlockEntities.TESLA_COIL.get(), pos, state); }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); energy = nbt.getDouble("energy"); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putDouble("energy", energy); }
    @Override public void onLoaded() { super.onLoaded(); if (!level.isClientSide) { EnergyNet.instance.addTile(this); addedToEnet = true; } }
    @Override public void onUnloaded() { if (addedToEnet) { EnergyNet.instance.removeTile(this); addedToEnet = false; } super.onUnloaded(); }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (level.hasNeighborSignal(worldPosition) && energy >= 1) {
            energy -= 1;
            if (ticker++ % 32 == 0) {
                int damage = (int)energy / 400;
                if (damage > 0 && shock(damage)) energy -= damage * 400;
            }
        }
    }

    protected boolean shock(int damage) {
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(worldPosition).inflate(4))) {
            if (ItemArmorUtility.Hazmat.hasCompleteHazmat(e) || !e.hurt(Ic2DamageSources.get(level, Ic2DamageSources.ELECTRICITY), damage)) continue;
            if (level instanceof ServerLevel sl) {
                var rnd = sl.random;
                for (int i = 0; i < damage; i++) sl.sendParticles(new DustParticleOptions(new Vector3f(0.1f, 0.1f, 1f), 1f), e.getX() + rnd.nextFloat() - 0.5, e.getY() + rnd.nextFloat() * 2 - 1, e.getZ() + rnd.nextFloat() - 0.5, 1, 0, 0, 0, 0);
            }
            return true;
        }
        return false;
    }

    @Override public double getDemandedEnergy() { return 10000 - energy; }
    @Override public int getSinkTier() { return 2; }
    @Override public double injectEnergy(Direction from, double amount, double voltage) { energy += amount; return 0; }
    @Override public boolean acceptsEnergyFrom(BlockEntity emitter, Direction direction) { return true; }
}
