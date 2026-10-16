package ic2.core;

import ic2.core.network.ContainerSyncPayload;
import ic2.core.ref.Ic2Menus;
import ic2.core.slot.SlotInvSlot;
import ic2.core.slot.SlotInvSlotReadOnly;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Menu of an IC2 block entity. The same subclass is built on both sides; values registered with {@link #sync} are copied
 * from the server block entity to the client one (the original's networked container fields).
 */
public abstract class ContainerBase<T> extends AbstractContainerMenu {
    public final T base;
    protected final Player player;
    private final List<DoubleSupplier> getters = new ArrayList<>();
    private final List<DoubleConsumer> setters = new ArrayList<>();
    private double[] lastSent;

    protected ContainerBase(int containerId, Inventory inventory, T base) {
        super(Ic2Menus.TILE.get(), containerId);
        this.base = base;
        this.player = inventory.player;
    }

    protected void addPlayerInventorySlots(Inventory inventory, int height) { addPlayerInventorySlots(inventory, 178, height); }

    protected void addPlayerInventorySlots(Inventory inventory, int width, int height) {
        int xStart = (width - 162) / 2;
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, xStart + col * 18, height - 82 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, xStart + col * 18, height - 24));
    }

    public void sync(DoubleSupplier getter, DoubleConsumer setter) { getters.add(getter); setters.add(setter); }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (!(player instanceof ServerPlayer sp) || getters.isEmpty()) return;
        double[] values = new double[getters.size()];
        for (int i = 0; i < values.length; i++) values[i] = getters.get(i).getAsDouble();
        if (lastSent == null || !java.util.Arrays.equals(lastSent, values)) {
            lastSent = values;
            PacketDistributor.sendToPlayer(sp, new ContainerSyncPayload(containerId, values));
        }
    }

    public void receiveSync(double[] values) { for (int i = 0; i < values.length && i < setters.size(); i++) setters.get(i).accept(values[i]); }

    /** Client → server button press of the original GUIs. */
    public void onContainerEvent(String event) {}

    @Override
    public boolean stillValid(Player player) { return !(base instanceof Container c) || c.stillValid(player); }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (base instanceof IHasGui gui) gui.onGuiClosed(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int sourceSlotIndex) {
        Slot sourceSlot = slots.get(sourceSlotIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = sourceSlot.getItem();
        int oldSize = source.getCount();
        if (sourceSlot.container == player.getInventory()) {
            for (int run = 0; run < 4 && source.getCount() > 0; run++)
                for (Slot target : slots)
                    if (target.container != player.getInventory() && isValidTargetSlot(target, source, run % 2 == 1, run < 2)) { transfer(source, target); if (source.isEmpty()) break; }
        } else {
            for (int run = 0; run < 2 && source.getCount() > 0; run++) {
                ListIterator<Slot> it = slots.listIterator(slots.size());
                while (it.hasPrevious()) {
                    Slot target = it.previous();
                    if (target.container == player.getInventory() && isValidTargetSlot(target, source, run == 1, false)) { transfer(source, target); if (source.isEmpty()) break; }
                }
            }
        }
        if (source.getCount() != oldSize) {
            if (source.isEmpty()) sourceSlot.set(ItemStack.EMPTY);
            else sourceSlot.onTake(player, source);
            sourceSlot.setChanged();
        }
        return ItemStack.EMPTY;
    }

    private static boolean isValidTargetSlot(Slot slot, ItemStack stack, boolean allowEmpty, boolean requireInputOnly) {
        if (slot instanceof SlotInvSlotReadOnly) return false;
        if (!slot.mayPlace(stack)) return false;
        if (!allowEmpty && !slot.hasItem()) return false;
        return !requireInputOnly || slot instanceof SlotInvSlot s && s.invSlot.canInput();
    }

    private void transfer(ItemStack stack, Slot dst) {
        int amount = Math.min(dst.getMaxStackSize(stack), stack.isStackable() ? stack.getMaxStackSize() : 1);
        ItemStack dstStack = dst.getItem();
        if (!dstStack.isEmpty() && !StackUtil.isStackEqualStrict(stack, dstStack)) return;
        if (!dstStack.isEmpty()) amount -= dstStack.getCount();
        amount = Math.min(amount, stack.getCount());
        if (amount <= 0) return;
        if (dstStack.isEmpty()) dst.set(stack.copyWithCount(amount));
        else dstStack.grow(amount);
        stack.shrink(amount);
        dst.setChanged();
    }
}
