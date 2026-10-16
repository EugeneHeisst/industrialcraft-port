package ic2.core.block.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.upgrade.IUpgradableBlock;
import ic2.core.upgrade.IUpgradeItem;
import ic2.core.upgrade.UpgradableProperty;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Pressure vessel ports: each finds the reactor (or a chamber) within one block. */
public final class TileEntityReactorPort {
    private TileEntityReactorPort() {}

    static TileEntityNuclearReactor findReactor(net.minecraft.world.level.Level level, BlockPos pos) {
        if (level == null) return null;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (!level.isLoaded(p)) continue;
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof TileEntityNuclearReactor r) return r;
            if (be instanceof IReactorChamber c && c.getReactor() instanceof TileEntityNuclearReactor r) return r;
        }
        return null;
    }

    /** Access hatch: exposes the reactor inventory (item handler capability) to pipes and hoppers. */
    public static class AccessHatch extends ic2.core.block.TileEntityBlock {
        public AccessHatch(BlockPos pos, BlockState state) { super(ic2.core.ref.Ic2BlockEntities.REACTOR_ACCESS_HATCH.get(), pos, state); }
        public TileEntityNuclearReactor getReactor() { return findReactor(level, worldPosition); }
        @Override public float getWrenchDropRate() { return 0.8f; }
    }

    /** Redstone port: a redstone signal on it switches the reactor on. */
    public static class RedstonePort extends ic2.core.block.TileEntityBlock {
        private boolean powered;
        private int ticker;
        public RedstonePort(BlockPos pos, BlockState state) { super(ic2.core.ref.Ic2BlockEntities.REACTOR_REDSTONE_PORT.get(), pos, state); }
        @Override protected void updateEntityServer() {
            if (++ticker < 20) return;
            ticker = 0;
            boolean p = level.hasNeighborSignal(worldPosition);
            if (p != powered) { powered = p; var r = findReactor(level, worldPosition); if (r != null) r.setRedstoneSignal(p); }
        }
        @Override public float getWrenchDropRate() { return 0.8f; }
    }

    /** Fluid port: passes coolant in / hot coolant out; a fluid ejector upgrade pushes hot coolant out. */
    public static class FluidPort extends TileEntityInventory implements IHasGui, IUpgradableBlock {
        public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 1);
        public FluidPort(BlockPos pos, BlockState state) { super(ic2.core.ref.Ic2BlockEntities.REACTOR_FLUID_PORT.get(), pos, state); }
        public TileEntityNuclearReactor getReactor() { return findReactor(level, worldPosition); }
        @Override protected void updateEntityServer() {
            super.updateEntityServer();
            for (int i = 0; i < upgradeSlot.size(); i++) { var s = upgradeSlot.get(i); if (s.getItem() instanceof IUpgradeItem u && u.onTick(s, this)) super.setChanged(); }
        }
        @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerReactorFluidPort(id, inv, this); }
        @Override public Set<UpgradableProperty> getUpgradableProperties() { return EnumSet.of(UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing); }
        @Override public double getEnergy() { return 40; }
        @Override public boolean useEnergy(double amount) { return true; }
        @Override public float getWrenchDropRate() { return 0.8f; }
    }
}
