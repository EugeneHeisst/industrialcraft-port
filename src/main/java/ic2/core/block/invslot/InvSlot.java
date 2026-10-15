package ic2.core.block.invslot;

import ic2.core.block.TileEntityInventory;
import ic2.core.util.StackUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class InvSlot {
    public final TileEntityInventory base;
    public final String name;
    private final NonNullList<ItemStack> contents;
    private ItemStack[] contentsBackup;
    protected final Access access;
    public final InvSide preferredSide;
    private int stackSizeLimit = 64;

    public InvSlot(TileEntityInventory base, String name, Access access, int count) { this(base, name, access, count, InvSide.ANY); }

    public InvSlot(TileEntityInventory base, String name, Access access, int count, InvSide preferredSide) {
        this.contents = NonNullList.withSize(count, ItemStack.EMPTY);
        this.base = base;
        this.name = name;
        this.access = access;
        this.preferredSide = preferredSide;
        if (base != null) base.addInvSlot(this);
    }

    /** Detached slot (crafting helpers). */
    public InvSlot(int count) { this(null, null, Access.NONE, count); }

    public void readFromNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        ListTag list = nbt.getList("Contents", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            int index = tag.getByte("Index") & 255;
            if (index < size()) contents.set(index, ItemStack.parseOptional(registries, tag));
        }
    }

    public void writeToNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (int i = 0; i < contents.size(); i++) {
            if (contents.get(i).isEmpty()) continue;
            CompoundTag tag = (CompoundTag)contents.get(i).save(registries, new CompoundTag());
            tag.putByte("Index", (byte)i);
            list.add(tag);
        }
        nbt.put("Contents", list);
    }

    public int size() { return contents.size(); }
    public ItemStack get() { return get(0); }
    public ItemStack get(int index) { return contents.get(index); }
    public void put(ItemStack content) { put(0, content); }
    public void put(int index, ItemStack content) { contents.set(index, content == null ? ItemStack.EMPTY : content); onChanged(); }
    public void clear() { for (int i = 0; i < contents.size(); i++) contents.set(i, ItemStack.EMPTY); }
    public void onChanged() {}
    public boolean accepts(ItemStack stack) { return true; }
    public boolean canInput() { return access == Access.I || access == Access.IO; }
    public boolean canOutput() { return access == Access.O || access == Access.IO; }

    public boolean isEmpty() {
        for (ItemStack s : contents) if (!s.isEmpty()) return false;
        return true;
    }

    public void organize() {
        for (int dstIndex = 0; dstIndex < contents.size() - 1; dstIndex++) {
            ItemStack dst = contents.get(dstIndex);
            if (!dst.isEmpty() && dst.getCount() >= dst.getMaxStackSize()) continue;
            for (int srcIndex = dstIndex + 1; srcIndex < contents.size(); srcIndex++) {
                ItemStack src = contents.get(srcIndex);
                if (src.isEmpty()) continue;
                if (dst.isEmpty()) { contents.set(srcIndex, ItemStack.EMPTY); dst = src; contents.set(dstIndex, src); }
                else if (StackUtil.isStackEqualStrict(dst, src)) {
                    int space = Math.min(getStackSizeLimit(), dst.getMaxStackSize() - dst.getCount());
                    if (src.getCount() > space) { src.shrink(space); dst.grow(space); break; }
                    contents.set(srcIndex, ItemStack.EMPTY);
                    dst.grow(src.getCount());
                }
            }
        }
    }

    public int getStackSizeLimit() { return stackSizeLimit; }
    public void setStackSizeLimit(int limit) { stackSizeLimit = limit; }

    protected void backup() {
        if (contentsBackup == null) contentsBackup = new ItemStack[contents.size()];
        for (int i = 0; i < contents.size(); i++) contentsBackup[i] = contents.get(i).copy();
    }

    protected void restore() { for (int i = 0; i < contents.size(); i++) contents.set(i, contentsBackup[i]); }

    @Override public String toString() { return name + "[" + contents.size() + "]: " + contents; }

    public enum Access { NONE, I, O, IO }

    public enum InvSide {
        ANY, TOP, BOTTOM, SIDE, NOTSIDE;
        public boolean matches(Direction side) {
            return this == ANY || side == Direction.DOWN && this == BOTTOM || side == Direction.UP && this == TOP
                || side != null && side.getAxis() == Direction.Axis.Y && this == NOTSIDE || side != null && side.getAxis() != Direction.Axis.Y && this == SIDE;
        }
    }
}
