package ic2.core.block.machine.container;

import ic2.core.ContainerBase;
import ic2.core.block.machine.tileentity.TileEntityPatternStorage;
import ic2.core.block.machine.tileentity.TileEntityReplicator;
import ic2.core.block.machine.tileentity.TileEntityScanner;
import ic2.core.slot.SlotInvSlot;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Containers of the scanner, replicator and pattern storage. Patterns are synced as item ids. */
public final class ContainerUu {
    private ContainerUu() {}

    static void syncPattern(ContainerBase<?> c, Supplier<ItemStack> get, Consumer<ItemStack> set) {
        c.sync(() -> get.get().isEmpty() ? -1 : BuiltInRegistries.ITEM.getId(get.get().getItem()), v -> set.accept(v < 0 ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.byId((int)v))));
    }

    public static class Scanner extends ContainerElectricMachine<TileEntityScanner> {
        public Scanner(int id, Inventory inv, TileEntityScanner base) {
            super(id, inv, base, 166, 8, 43);
            addSlot(new SlotInvSlot(base.inputSlot, 0, 55, 35));
            addSlot(new SlotInvSlot(base.diskSlot, 0, 152, 65));
            sync(() -> base.state.ordinal(), v -> base.state = TileEntityScanner.State.values()[(int)v]);
            sync(() -> base.progress, v -> base.progress = (int)v);
            sync(() -> base.patternUu, v -> base.patternUu = v);
            sync(() -> base.patternEu, v -> base.patternEu = v);
        }
        @Override public void onContainerEvent(String event) { base.onNetworkEvent(Integer.parseInt(event)); }
    }

    public static class Replicator extends ContainerElectricMachine<TileEntityReplicator> {
        public Replicator(int id, Inventory inv, TileEntityReplicator base) {
            super(id, inv, base, 184, 152, 83);
            addSlot(new SlotInvSlot(base.outputSlot, 0, 90, 59));
            addSlot(new SlotInvSlot(base.fluidSlot, 0, 8, 27));
            addSlot(new SlotInvSlot(base.cellSlot, 0, 8, 72));
            for (int i = 0; i < 4; i++) addSlot(new SlotInvSlot(base.upgradeSlot, i, 152, 8 + i * 18));
            FluidSync.tank(this, base.getFluidTank());
            sync(() -> base.uuProcessed, v -> base.uuProcessed = v);
            syncPattern(this, () -> base.pattern, s -> base.pattern = s);
            sync(() -> base.mode.ordinal(), v -> base.mode = TileEntityReplicator.Mode.values()[(int)v]);
            sync(() -> base.index, v -> base.index = (int)v);
            sync(() -> base.maxIndex, v -> base.maxIndex = (int)v);
            sync(() -> base.patternUu, v -> base.patternUu = v);
            sync(() -> base.patternEu, v -> base.patternEu = v);
        }
        @Override public void onContainerEvent(String event) { base.onNetworkEvent(Integer.parseInt(event)); }
    }

    public static class PatternStorage extends ContainerBase<TileEntityPatternStorage> {
        public PatternStorage(int id, Inventory inv, TileEntityPatternStorage base) {
            super(id, inv, base);
            addPlayerInventorySlots(inv, 166);
            addSlot(new SlotInvSlot(base.diskSlot, 0, 18, 20));
            sync(() -> base.index, v -> base.index = (int)v);
            sync(() -> base.maxIndex, v -> base.maxIndex = (int)v);
            syncPattern(this, () -> base.pattern, s -> base.pattern = s);
            sync(() -> base.patternUu, v -> base.patternUu = v);
            sync(() -> base.patternEu, v -> base.patternEu = v);
        }
        @Override public void onContainerEvent(String event) { base.onNetworkEvent(Integer.parseInt(event)); }
    }
}
