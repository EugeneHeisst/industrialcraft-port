package ic2.core.util;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public final class StackUtil {
    private StackUtil() {}
    public static boolean isStackEqual(ItemStack a, ItemStack b) { return !a.isEmpty() && !b.isEmpty() && a.is(b.getItem()); }
    public static boolean isStackEqualStrict(ItemStack a, ItemStack b) { return ItemStack.isSameItemSameComponents(a, b); }
    public static ItemStack copyWithSize(ItemStack stack, int size) { return stack.copyWithCount(size); }

    private static IItemHandler neighbor(BlockEntity be, Direction dir) {
        return be.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, be.getBlockPos().relative(dir), dir.getOpposite());
    }

    /** Moves up to amount items out of the block entity into the neighbor on dir; returns the moved count. */
    public static int eject(BlockEntity be, Direction dir, int amount) {
        if (!(be instanceof WorldlyContainer src)) return 0;
        IItemHandler dst = neighbor(be, dir);
        if (dst == null) return 0;
        int moved = 0;
        for (int slot : src.getSlotsForFace(dir)) {
            ItemStack stack = src.getItem(slot);
            if (stack.isEmpty() || !src.canTakeItemThroughFace(slot, stack, dir)) continue;
            ItemStack rest = ItemHandlerHelper.insertItemStacked(dst, stack.copyWithCount(Math.min(amount - moved, stack.getCount())), false);
            int n = Math.min(amount - moved, stack.getCount()) - rest.getCount();
            if (n > 0) { src.removeItem(slot, n); moved += n; }
            if (moved >= amount) break;
        }
        return moved;
    }

    /** Pulls up to amount items from the neighbor on dir into the block entity; returns the moved count. */
    public static int pull(BlockEntity be, Direction dir, int amount) {
        if (!(be instanceof WorldlyContainer dst)) return 0;
        IItemHandler src = neighbor(be, dir);
        if (src == null) return 0;
        int moved = 0;
        for (int i = 0; i < src.getSlots() && moved < amount; i++) {
            ItemStack stack = src.extractItem(i, amount - moved, true);
            if (stack.isEmpty()) continue;
            int accepted = insert(dst, stack, dir, true);
            if (accepted <= 0) continue;
            ItemStack taken = src.extractItem(i, accepted, false);
            moved += insert(dst, taken, dir, false);
        }
        return moved;
    }

    private static int insert(WorldlyContainer dst, ItemStack stack, Direction side, boolean simulate) {
        int left = stack.getCount();
        for (int slot : dst.getSlotsForFace(side)) {
            if (left <= 0) break;
            if (!dst.canPlaceItemThroughFace(slot, stack, side)) continue;
            ItemStack cur = dst.getItem(slot);
            int limit = Math.min(dst.getMaxStackSize(), stack.getMaxStackSize());
            if (cur.isEmpty()) {
                int n = Math.min(left, limit);
                if (!simulate) dst.setItem(slot, stack.copyWithCount(n));
                left -= n;
            } else if (isStackEqualStrict(cur, stack) && cur.getCount() < limit) {
                int n = Math.min(left, limit - cur.getCount());
                if (!simulate) { cur.grow(n); dst.setChanged(); }
                left -= n;
            }
        }
        return stack.getCount() - left;
    }
}
