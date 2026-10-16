package ic2.core.block.personal;

import com.mojang.authlib.GameProfile;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLinked;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.network.TileEventPayload;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Sounds;
import ic2.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Sells the offer stack for the demand stack: the payment goes into the adjacent inventories, the goods come out of them
 * (the owner's safe first). Operators can make it infinite, then it needs no stock and keeps no payment.
 */
public class TileEntityTradeOMat extends TileEntityInventory implements IPersonalBlock, IHasGui, TileEventPayload.Listener {
    private int ticker = java.util.concurrent.ThreadLocalRandom.current().nextInt(64);
    private final PersonalOwner owner = new PersonalOwner();
    public int totalTradeCount;
    public int stock;
    public boolean infinite;
    public final InvSlot demandSlot = new InvSlot(this, "demand", InvSlot.Access.NONE, 1);
    public final InvSlot offerSlot = new InvSlot(this, "offer", InvSlot.Access.NONE, 1);
    public final InvSlotConsumableLinked inputSlot = new InvSlotConsumableLinked(this, "input", 1, demandSlot);
    public final InvSlotOutput outputSlot = new InvSlotOutput(this, "output", 1);

    public TileEntityTradeOMat(BlockPos pos, BlockState state) { super(Ic2BlockEntities.TRADE_O_MAT.get(), pos, state); }

    @Override
    public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) {
        super.readFromNBT(nbt, r);
        owner.read(nbt);
        totalTradeCount = nbt.getInt("totalTradeCount");
        infinite = nbt.getBoolean("infinite");
    }

    @Override
    public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) {
        super.writeToNBT(nbt, r);
        owner.write(nbt);
        nbt.putInt("totalTradeCount", totalTradeCount);
        if (infinite) nbt.putBoolean("infinite", true);
    }

    @Override protected void writeClientData(CompoundTag nbt, HolderLookup.Provider r) { owner.write(nbt); }
    @Override protected void readClientData(CompoundTag nbt, HolderLookup.Provider r) { owner.read(nbt); }

    @Override public void onLoaded() { super.onLoaded(); updateStock(); }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        trade();
        if (infinite) stock = -1;
        else if (ticker++ % 64 == 0) updateStock();
    }

    private void trade() {
        ItemStack tradedIn = inputSlot.consumeLinked(true);
        if (tradedIn.isEmpty()) return;
        ItemStack offer = offerSlot.get();
        if (offer.isEmpty() || !outputSlot.canAdd(offer)) return;
        if (infinite) {
            inputSlot.consumeLinked(false);
            outputSlot.add(offer.copy());
        } else {
            ItemStack transferredIn = StackUtil.fetch(this, offer, true);
            if (transferredIn.getCount() != offer.getCount()) return;
            if (StackUtil.distribute(this, tradedIn, true) != tradedIn.getCount()) return;
            transferredIn = StackUtil.fetch(this, offer, false);
            if (transferredIn.getCount() != offer.getCount()) {
                IC2.LOG.warn("The Trade-O-Mat at {} {} received an inconsistent result from an adjacent trade supply inventory, the item stack {} will be lost.", level.dimension().location(), worldPosition, transferredIn);
                return;
            }
            StackUtil.distribute(this, inputSlot.consumeLinked(false), false);
            outputSlot.add(transferredIn);
            stock -= offer.getCount();
        }
        totalTradeCount++;
        TileEventPayload.send(this, 0);
        setChanged();
    }

    public void updateStock() {
        ItemStack offer = offerSlot.get();
        stock = offer.isEmpty() ? 0 : StackUtil.fetch(this, offer.copyWithCount(Integer.MAX_VALUE), true).getCount();
    }

    @Override public boolean wrenchCanRemove(Player player) { return permitsAccess(player.getGameProfile()); }
    @Override public boolean permitsAccess(GameProfile profile) { return owner.permits(level, profile, () -> { setChanged(); sendClientUpdate(); }); }
    @Override public GameProfile getOwner() { return owner.owner; }

    /** The owner's view also needs the admin flag for the infinite button. */
    @Override
    public void writeGuiData(Player player, RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(permitsAccess(player.getGameProfile()));
        buf.writeBoolean(PersonalOwner.isAdmin(player));
    }

    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerTradeOMat(id, inventory, this, permitsAccess(inventory.player.getGameProfile()), PersonalOwner.isAdmin(inventory.player)); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory, RegistryFriendlyByteBuf buf) { return new ContainerTradeOMat(id, inventory, this, buf.readBoolean(), buf.readBoolean()); }

    @Override
    public void onNetworkEvent(int event) {
        if (event == 0) level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, Ic2Sounds.O_MAT.get(), SoundSource.BLOCKS, 1, 1, false);
    }

    /** Admin button of the owner view: toggles infinite stock. */
    public void onNetworkEvent(Player player, int event) {
        if (event != 0 || !PersonalOwner.isAdmin(player)) return;
        infinite = !infinite;
        if (!infinite) updateStock();
        setChanged();
    }
}
