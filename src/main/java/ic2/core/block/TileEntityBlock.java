package ic2.core.block;

import ic2.api.tile.IWrenchable;
import ic2.core.TickHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base of all IC2 block entities. Facing and the active flag live in the block state (FACING/ACTIVE of
 * {@link BlockTileEntity}), so models switch textures like the original's facing/active icon lookup.
 */
public abstract class TileEntityBlock extends BlockEntity implements IWrenchable {
    private boolean loaded;

    protected TileEntityBlock(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) TickHandler.addSingleTickCallback(level, l -> {
            if (!isRemoved() && l.isLoaded(worldPosition) && l.getBlockEntity(worldPosition) == this) onLoaded();
        });
        else if (level != null) onLoadedClient();
    }

    @Override public void setRemoved() { onUnloaded(); super.setRemoved(); }
    @Override public void onChunkUnloaded() { onUnloaded(); super.onChunkUnloaded(); }

    /** Server: called on the tick after the block entity got placed or loaded. */
    public void onLoaded() { loaded = true; }
    protected void onLoadedClient() {}
    public void onUnloaded() { loaded = false; }
    public boolean isLoaded() { return loaded; }

    public final void tick() {
        if (level.isClientSide) updateEntityClient();
        else if (loaded) updateEntityServer();
    }
    protected void updateEntityClient() {}
    protected void updateEntityServer() {}

    public boolean getActive() {
        BlockState state = getBlockState();
        return state.hasProperty(BlockTileEntity.ACTIVE) && state.getValue(BlockTileEntity.ACTIVE);
    }

    public void setActive(boolean active) {
        BlockState state = getBlockState();
        if (level != null && state.hasProperty(BlockTileEntity.ACTIVE) && state.getValue(BlockTileEntity.ACTIVE) != active)
            level.setBlock(worldPosition, state.setValue(BlockTileEntity.ACTIVE, active), 3);
    }

    @Override
    public Direction getFacing() {
        BlockState state = getBlockState();
        return state.hasProperty(BlockTileEntity.FACING) ? state.getValue(BlockTileEntity.FACING) : Direction.NORTH;
    }

    @Override
    public void setFacing(Direction facing) {
        BlockState state = getBlockState();
        if (level != null && state.hasProperty(BlockTileEntity.FACING) && state.getValue(BlockTileEntity.FACING) != facing)
            level.setBlock(worldPosition, state.setValue(BlockTileEntity.FACING, facing), 3);
    }

    @Override public boolean wrenchCanSetFacing(Player player, Direction side) { return false; }
    @Override public boolean wrenchCanRemove(Player player) { return true; }
    @Override public float getWrenchDropRate() { return 1; }
    @Override public ItemStack getWrenchDrop(Player player) { return new ItemStack(getBlockState().getBlock()); }

    /** Called before the block is broken (not when only the state changes). */
    public void onBlockBreak() {}
    public void onNeighborUpdate(BlockPos from) {}

    // persistence: subclasses override readFromNBT/writeToNBT like the original
    public void readFromNBT(CompoundTag nbt, HolderLookup.Provider registries) {}
    public void writeToNBT(CompoundTag nbt, HolderLookup.Provider registries) {}
    @Override protected final void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) { super.loadAdditional(nbt, registries); readFromNBT(nbt, registries); }
    @Override protected final void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) { super.saveAdditional(nbt, registries); writeToNBT(nbt, registries); }

    // client sync of render data
    protected void writeClientData(CompoundTag nbt, HolderLookup.Provider registries) {}
    protected void readClientData(CompoundTag nbt, HolderLookup.Provider registries) {}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { var tag = new CompoundTag(); writeClientData(tag, registries); return tag; }
    @Override public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) { readClientData(tag, registries); }
    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) { readClientData(pkt.getTag(), registries); }
    protected void sendClientUpdate() { if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2); }
}
