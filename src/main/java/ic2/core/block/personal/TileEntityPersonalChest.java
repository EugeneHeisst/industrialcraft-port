package ic2.core.block.personal;

import com.mojang.authlib.GameProfile;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/** 54-slot safe only its owner (and operators) can open or wrench; nothing can pipe items in or out of it. */
public class TileEntityPersonalChest extends TileEntityInventory implements IPersonalBlock, IHasGui {
    private int ticksSinceSync;
    private int numUsingPlayers;
    public float lidAngle, prevLidAngle;
    private final PersonalOwner owner = new PersonalOwner();
    public final InvSlot contentSlot = new InvSlot(this, "content", InvSlot.Access.NONE, 54);

    public TileEntityPersonalChest(BlockPos pos, BlockState state) { super(Ic2BlockEntities.PERSONAL_CHEST.get(), pos, state); }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); owner.read(nbt); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); owner.write(nbt); }
    @Override protected void writeClientData(CompoundTag nbt, HolderLookup.Provider r) { owner.write(nbt); }
    @Override protected void readClientData(CompoundTag nbt, HolderLookup.Provider r) { owner.read(nbt); }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (++ticksSinceSync % 80 == 0) syncNumUsingPlayers();
    }

    @Override
    protected void updateEntityClient() {
        prevLidAngle = lidAngle;
        if (numUsingPlayers > 0 && lidAngle == 0) playSound(true);
        if (numUsingPlayers == 0 && lidAngle > 0 || numUsingPlayers > 0 && lidAngle < 1) {
            float old = lidAngle;
            lidAngle += numUsingPlayers > 0 ? 0.1f : -0.1f;
            if (lidAngle > 1) lidAngle = 1;
            if (lidAngle < 0.5f && old >= 0.5f) playSound(false);
            if (lidAngle < 0) lidAngle = 0;
        }
    }

    private void playSound(boolean open) {
        level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE,
            SoundSource.BLOCKS, 0.5f, level.random.nextFloat() * 0.1f + 0.9f, false);
    }

    public void openChest() { numUsingPlayers++; syncNumUsingPlayers(); }
    public void closeChest() { numUsingPlayers--; syncNumUsingPlayers(); }

    @Override
    public boolean triggerEvent(int event, int data) {
        if (event != 1) return super.triggerEvent(event, data);
        numUsingPlayers = data;
        return true;
    }

    private void syncNumUsingPlayers() { if (level != null && !level.isClientSide) level.blockEvent(worldPosition, getBlockState().getBlock(), 1, numUsingPlayers); }

    @Override
    public boolean wrenchCanRemove(Player player) {
        if (!permitsAccess(player.getGameProfile())) { player.displayClientMessage(owner.ownerMessage(), false); return false; }
        if (!contentSlot.isEmpty()) { player.displayClientMessage(net.minecraft.network.chat.Component.literal("Can't wrench non-empty safe"), false); return false; }
        return true;
    }

    @Override public boolean permitsAccess(GameProfile profile) { return owner.permits(level, profile, () -> { setChanged(); sendClientUpdate(); }); }
    @Override public GameProfile getOwner() { return owner.owner; }
    public net.minecraft.network.chat.Component ownerMessage() { return owner.ownerMessage(); }

    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerPersonalChest(id, inventory, this); }

    /** Full access to the contents for a trade-o-mat or energy-o-mat of the same owner. */
    public IItemHandler ownerAccess() {
        return new IItemHandler() {
            @Override public int getSlots() { return contentSlot.size(); }
            @Override public ItemStack getStackInSlot(int slot) { return contentSlot.get(slot); }
            @Override public int getSlotLimit(int slot) { return 64; }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return true; }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (stack.isEmpty()) return ItemStack.EMPTY;
                ItemStack cur = contentSlot.get(slot);
                if (!cur.isEmpty() && !ItemStack.isSameItemSameComponents(cur, stack)) return stack;
                int n = Math.min(stack.getCount(), Math.min(64, stack.getMaxStackSize()) - cur.getCount());
                if (n <= 0) return stack;
                if (!simulate) { contentSlot.put(slot, stack.copyWithCount(cur.getCount() + n)); setChanged(); }
                return stack.copyWithCount(stack.getCount() - n);
            }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                ItemStack cur = contentSlot.get(slot);
                if (cur.isEmpty() || amount <= 0) return ItemStack.EMPTY;
                int n = Math.min(amount, cur.getCount());
                ItemStack ret = cur.copyWithCount(n);
                if (!simulate) { contentSlot.put(slot, cur.getCount() == n ? ItemStack.EMPTY : cur.copyWithCount(cur.getCount() - n)); setChanged(); }
                return ret;
            }
        };
    }
}
