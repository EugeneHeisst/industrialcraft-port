package ic2.core.block.misc;

import ic2.core.block.Retexture;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Foam wall block entity: only keeps the obscurator retexture of its sides. */
public class TileEntityWall extends BlockEntity {
    public final Retexture retexture = new Retexture();

    public TileEntityWall(BlockPos pos, BlockState state) { super(Ic2BlockEntities.FOAM_WALL.get(), pos, state); }

    @Override protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider r) { super.loadAdditional(nbt, r); retexture.read(nbt, level); }
    @Override protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider r) { super.saveAdditional(nbt, r); retexture.write(nbt); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r) { CompoundTag t = new CompoundTag(); retexture.write(t); return t; }
    @Override public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider r) { retexture.read(tag, level); }
    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider r) { retexture.read(pkt.getTag(), level); }
}
