package ic2.core.block.personal;

import com.mojang.authlib.GameProfile;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotCharge;
import ic2.core.block.invslot.InvSlotConsumableLinked;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.upgrade.IUpgradableBlock;
import ic2.core.upgrade.UpgradableProperty;
import ic2.core.util.StackUtil;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Sells energy: each paid demand stack (moved into the adjacent inventories) buys euOffer EU, which it takes from the cables
 * on its other sides and hands out through its front or the charge slot.
 */
public class TileEntityEnergyOMat extends TileEntityInventory implements IPersonalBlock, IHasGui, IEnergySink, IEnergySource, IUpgradableBlock {
    public int euOffer = 1000;
    private final PersonalOwner owner = new PersonalOwner();
    private boolean addedToEnergyNet;
    public int paidFor;
    public double euBuffer;
    private int euBufferMax = 10000;
    private int tier = 1;
    public final InvSlot demandSlot = new InvSlot(this, "demand", InvSlot.Access.NONE, 1);
    public final InvSlotConsumableLinked inputSlot = new InvSlotConsumableLinked(this, "input", 1, demandSlot);
    public final InvSlotCharge chargeSlot = new InvSlotCharge(this, 1);
    public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 1);

    public TileEntityEnergyOMat(BlockPos pos, BlockState state) { super(Ic2BlockEntities.ENERGY_O_MAT.get(), pos, state); }

    @Override
    public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) {
        super.readFromNBT(nbt, r);
        owner.read(nbt);
        euOffer = nbt.getInt("euOffer");
        paidFor = nbt.getInt("paidFor");
        euBuffer = nbt.getDouble("euBuffer");
        upgradeSlot.onChanged();
    }

    @Override
    public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) {
        super.writeToNBT(nbt, r);
        owner.write(nbt);
        nbt.putInt("euOffer", euOffer);
        nbt.putInt("paidFor", paidFor);
        nbt.putDouble("euBuffer", euBuffer);
    }

    @Override protected void writeClientData(CompoundTag nbt, HolderLookup.Provider r) { owner.write(nbt); }
    @Override protected void readClientData(CompoundTag nbt, HolderLookup.Provider r) { owner.read(nbt); }

    @Override public boolean wrenchCanRemove(Player player) { return permitsAccess(player.getGameProfile()); }
    @Override public boolean wrenchCanSetFacing(Player player, Direction side) { return getFacing() != side && permitsAccess(player.getGameProfile()); }

    @Override public void onLoaded() { super.onLoaded(); EnergyNet.instance.addTile(this); addedToEnergyNet = true; }
    @Override public void onUnloaded() {
        if (addedToEnergyNet && level != null && !level.isClientSide) { EnergyNet.instance.removeTile(this); addedToEnergyNet = false; }
        super.onUnloaded();
    }

    @Override
    public void setFacing(Direction facing) {
        if (addedToEnergyNet) EnergyNet.instance.removeTile(this);
        super.setFacing(facing);
        if (addedToEnergyNet) EnergyNet.instance.addTile(this);
    }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean invChanged = false;
        euBufferMax = TileEntityStandardMachine.applyModifier(10000, upgradeSlot.extraEnergyStorage, upgradeSlot.energyStorageMultiplier);
        tier = 1 + upgradeSlot.extraTier;
        chargeSlot.setTier(tier);
        ItemStack tradedIn = inputSlot.consumeLinked(true);
        if (!tradedIn.isEmpty() && StackUtil.distribute(this, tradedIn, true) == tradedIn.getCount()) {
            StackUtil.distribute(this, inputSlot.consumeLinked(false), false);
            paidFor += euOffer;
            invChanged = true;
        }
        if (euBuffer >= 1) {
            double sent = chargeSlot.charge(euBuffer);
            if (sent > 0) { euBuffer -= sent; invChanged = true; }
        }
        if (invChanged) setChanged();
    }

    @Override public boolean permitsAccess(GameProfile profile) { return owner.permits(level, profile, () -> { setChanged(); sendClientUpdate(); }); }
    @Override public GameProfile getOwner() { return owner.owner; }

    @Override public boolean acceptsEnergyFrom(BlockEntity emitter, Direction direction) { return direction != getFacing(); }
    @Override public boolean emitsEnergyTo(BlockEntity receiver, Direction direction) { return direction == getFacing(); }
    @Override public double getOfferedEnergy() { return Math.min(euBuffer, EnergyNet.instance.getPowerFromTier(getSourceTier())); }
    @Override public void drawEnergy(double amount) { euBuffer -= amount; }
    @Override public double getDemandedEnergy() { return Math.min(paidFor, euBufferMax - euBuffer); }

    @Override
    public double injectEnergy(Direction from, double amount, double voltage) {
        double toAdd = Math.min(Math.min(amount, paidFor), euBufferMax - euBuffer);
        paidFor = (int)(paidFor - toAdd);
        euBuffer += toAdd;
        return amount - toAdd;
    }

    @Override public int getSourceTier() { return tier; }
    @Override public int getSinkTier() { return Integer.MAX_VALUE; }
    public int getEuBufferMax() { return euBufferMax; }

    @Override public void writeGuiData(Player player, RegistryFriendlyByteBuf buf) { buf.writeBoolean(permitsAccess(player.getGameProfile())); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerEnergyOMat(id, inventory, this, permitsAccess(inventory.player.getGameProfile())); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory, RegistryFriendlyByteBuf buf) { return new ContainerEnergyOMat(id, inventory, this, buf.readBoolean()); }

    /** Owner buttons 0-7: -100k, -10k, -1k, -100, +100k, +10k, +1k, +100 EU per trade, never below 100. */
    public void onNetworkEvent(Player player, int event) {
        if (!permitsAccess(player.getGameProfile())) return;
        int[] steps = {-100000, -10000, -1000, -100, 100000, 10000, 1000, 100};
        if (event < 0 || event >= steps.length) return;
        euOffer = Math.max(100, euOffer + steps[event]);
        setChanged();
    }

    @Override public double getEnergy() { return euBuffer; }
    @Override public boolean useEnergy(double amount) { return amount <= euBuffer; }
    @Override public Set<UpgradableProperty> getUpgradableProperties() { return EnumSet.of(UpgradableProperty.EnergyStorage, UpgradableProperty.Transformer); }
}
