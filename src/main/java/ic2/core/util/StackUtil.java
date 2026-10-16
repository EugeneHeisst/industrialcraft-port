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

    /**
     * Inventories next to the block entity, personal blocks first. A personal block is only reachable from a personal block
     * of the same owner (trade/energy-o-mat stock in the owner's safe), like the original getAdjacentInventories.
     */
    public static java.util.List<IItemHandler> getAdjacentInventories(BlockEntity source) {
        java.util.List<IItemHandler> personal = new java.util.ArrayList<>(), other = new java.util.ArrayList<>();
        for (Direction dir : Direction.values()) {
            BlockEntity target = ic2.core.util.Util.loadedBlockEntity(source.getLevel(), source.getBlockPos().relative(dir));
            if (target instanceof ic2.core.block.personal.IPersonalBlock pt) {
                if (!(source instanceof ic2.core.block.personal.IPersonalBlock ps) || !pt.permitsAccess(ps.getOwner())) continue;
                IItemHandler h = target instanceof ic2.core.block.personal.TileEntityPersonalChest chest ? chest.ownerAccess() : neighbor(source, dir);
                if (h != null) personal.add(h);
            } else {
                IItemHandler h = neighbor(source, dir);
                if (h != null) other.add(h);
            }
        }
        personal.addAll(other);
        return personal;
    }

    /** Puts the stack into the adjacent inventories; returns the transferred count. The stack itself is not changed. */
    public static int distribute(BlockEntity source, ItemStack stack, boolean simulate) {
        ItemStack rest = stack.copy();
        for (IItemHandler inv : getAdjacentInventories(source)) {
            if (rest.isEmpty()) break;
            rest = ItemHandlerHelper.insertItemStacked(inv, rest, simulate);
        }
        return stack.getCount() - rest.getCount();
    }

    /** Takes up to the stack's size of exactly that item from the adjacent inventories; returns what was (or would be) taken. */
    public static ItemStack fetch(BlockEntity source, ItemStack stack, boolean simulate) {
        int needed = stack.getCount(), got = 0;
        for (IItemHandler inv : getAdjacentInventories(source)) {
            for (int i = 0; i < inv.getSlots() && got < needed; i++) {
                if (!isStackEqualStrict(inv.getStackInSlot(i), stack)) continue;
                got += inv.extractItem(i, needed - got, simulate).getCount();
            }
            if (got >= needed) break;
        }
        return got == 0 ? ItemStack.EMPTY : stack.copyWithCount(got);
    }
}
