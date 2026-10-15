package ic2.core.block;

import ic2.core.block.invslot.InvSlot;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Inventory made of named {@link InvSlot}s; sided access follows each slot's access and preferred side like the original. */
public abstract class TileEntityInventory extends TileEntityBlock implements WorldlyContainer {
    public final List<InvSlot> invSlots = new ArrayList<>();

    protected TileEntityInventory(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    @Override
    public void readFromNBT(CompoundTag nbt, HolderLookup.Provider registries) {
        super.readFromNBT(nbt, registries);
        CompoundTag slots = nbt.getCompound("InvSlots");
        for (InvSlot slot : invSlots) slot.readFromNbt(slots.getCompound(slot.name), registries);
    }

    @Override
    public void writeToNBT(CompoundTag nbt, HolderLookup.Provider registries) {
        super.writeToNBT(nbt, registries);
        CompoundTag slots = new CompoundTag();
        for (InvSlot slot : invSlots) { CompoundTag tag = new CompoundTag(); slot.writeToNbt(tag, registries); slots.put(slot.name, tag); }
        nbt.put("InvSlots", slots);
    }

    public void addInvSlot(InvSlot slot) { invSlots.add(slot); }

    @Override public int getContainerSize() { int ret = 0; for (InvSlot s : invSlots) ret += s.size(); return ret; }
    @Override public boolean isEmpty() { for (InvSlot s : invSlots) if (!s.isEmpty()) return false; return true; }

    @Override
    public ItemStack getItem(int index) {
        for (InvSlot s : invSlots) { if (index < s.size()) return s.get(index); index -= s.size(); }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int amount) {
        ItemStack stack = getItem(index);
        if (stack.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        if (amount >= stack.getCount()) { setItem(index, ItemStack.EMPTY); return stack; }
        ItemStack ret = stack.split(amount);
        InvSlot slot = getInvSlot(index);
        if (slot != null) slot.onChanged();
        setChanged();
        return ret;
    }

    @Override public ItemStack removeItemNoUpdate(int index) { ItemStack ret = getItem(index); if (!ret.isEmpty()) setItem(index, ItemStack.EMPTY); return ret; }

    @Override
    public void setItem(int index, ItemStack stack) {
        for (InvSlot s : invSlots) { if (index < s.size()) { s.put(index, stack); break; } index -= s.size(); }
        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        for (InvSlot s : invSlots) s.onChanged();
    }

    @Override
    public int getMaxStackSize() { int max = 0; for (InvSlot s : invSlots) max = Math.max(max, s.getStackSizeLimit()); return max; }

    @Override public boolean stillValid(Player player) { return !isRemoved() && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) <= 64; }
    @Override public void clearContent() { for (InvSlot s : invSlots) s.clear(); }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        InvSlot slot = getInvSlot(index);
        return slot != null && slot.canInput() && slot.accepts(stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        int[] ret = new int[getContainerSize()];
        for (int i = 0; i < ret.length; i++) ret[i] = i;
        return ret;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction side) {
        InvSlot target = getInvSlot(index);
        if (target == null || !target.canInput() || !target.accepts(stack)) return false;
        if (target.preferredSide != InvSlot.InvSide.ANY && target.preferredSide.matches(side)) return true;
        for (InvSlot s : invSlots)
            if (s != target && s.preferredSide != InvSlot.InvSide.ANY && s.preferredSide.matches(side) && s.canInput() && s.accepts(stack)) return false;
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction side) {
        InvSlot target = getInvSlot(index);
        if (target == null || !target.canOutput()) return false;
        boolean correctSide = target.preferredSide.matches(side);
        if (target.preferredSide != InvSlot.InvSide.ANY && correctSide) return true;
        for (InvSlot s : invSlots)
            if (s != target && (s.preferredSide != InvSlot.InvSide.ANY || !correctSide) && s.preferredSide.matches(side) && s.canOutput()) return false;
        return true;
    }

    public InvSlot getInvSlot(int index) {
        for (InvSlot s : invSlots) { if (index < s.size()) return s; index -= s.size(); }
        return null;
    }

    /** Container index of the slot's first stack. */
    public int getBaseIndex(InvSlot slot) {
        int ret = 0;
        for (InvSlot s : invSlots) { if (s == slot) return ret; ret += s.size(); }
        return -1;
    }
}
