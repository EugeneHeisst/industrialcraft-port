package ic2.core.block.machine.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerMagnetizer;
import ic2.core.block.misc.BlockPoleFence;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Blocks;
import ic2.core.upgrade.IUpgradableBlock;
import ic2.core.upgrade.UpgradableProperty;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Charges the iron fence column in front of it (20 blocks up and down, more with augmentation): 1 EU per magnet level. */
public class TileEntityMagnetizer extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock {
    public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
    private int ticker = java.util.concurrent.ThreadLocalRandom.current().nextInt(16);

    public TileEntityMagnetizer(BlockPos pos, BlockState state) { super(Ic2BlockEntities.MAGNETIZER.get(), pos, state, 100, 1); }

    @Override public void onLoaded() { super.onLoaded(); if (!level.isClientSide) setOverclockRates(); }
    @Override public void setChanged() { super.setChanged(); if (level != null && !level.isClientSide) setOverclockRates(); }

    public void setOverclockRates() {
        upgradeSlot.onChanged();
        setTier(TileEntityStandardMachine.applyModifier(1, upgradeSlot.extraTier, 1));
        maxEnergy = TileEntityStandardMachine.applyModifier(100, upgradeSlot.extraEnergyStorage, upgradeSlot.energyStorageMultiplier);
    }

    private boolean redstone() { return upgradeSlot.getRedstoneInput(level.getBestNeighborSignal(worldPosition)) > 0; }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (ticker++ % 16 != 0 || redstone()) return;
        Direction dir = getFacing();
        int x = worldPosition.getX() + dir.getStepX(), z = worldPosition.getZ() + dir.getStepZ();
        double multiplier = (1 + upgradeSlot.extraEnergyDemand) * upgradeSlot.energyDemandMultiplier;
        int distance = 20 + upgradeSlot.augmentation;
        for (int y = worldPosition.getY(); y > level.getMinBuildHeight() && y >= worldPosition.getY() - distance && energy >= multiplier; y--)
            if (!charge(new BlockPos(x, y, z), multiplier)) break;
        for (int y = worldPosition.getY(); y < level.getMaxBuildHeight() && y <= worldPosition.getY() + distance && energy >= multiplier; y++)
            if (!charge(new BlockPos(x, y, z), multiplier)) break;
    }

    private boolean charge(BlockPos p, double multiplier) {
        BlockState s = level.getBlockState(p);
        if (!s.is(Ic2Blocks.IRON_FENCE.get())) return false;
        int need = 15 - s.getValue(BlockPoleFence.MAGNET);
        if (need > 0) {
            if (need * multiplier > energy) need = (int)Math.floor(energy / multiplier);
            level.setBlock(p, s.setValue(BlockPoleFence.MAGNET, s.getValue(BlockPoleFence.MAGNET) + need), 3);
            energy -= need * multiplier;
        }
        return true;
    }

    @Override public boolean acceptsEnergyFrom(BlockEntity emitter, Direction direction) { return direction != getFacing(); }
    @Override public double getEnergy() { return energy; }
    @Override public boolean useEnergy(double amount) { if (energy >= amount) { energy -= amount; return true; } return false; }
    @Override public Set<UpgradableProperty> getUpgradableProperties() { return EnumSet.of(UpgradableProperty.Augmentable, UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerMagnetizer(id, inv, this); }
}
