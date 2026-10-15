package ic2.core.network;

import ic2.core.IC2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Tile entity event of the original network manager (machine start/stop sounds and similar). */
public record TileEventPayload(BlockPos pos, int event) implements CustomPacketPayload {
    public static final Type<TileEventPayload> TYPE = new Type<>(IC2.id("tile_event"));
    public static final StreamCodec<ByteBuf, TileEventPayload> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, TileEventPayload::pos, ByteBufCodecs.VAR_INT, TileEventPayload::event, TileEventPayload::new);

    public interface Listener { void onNetworkEvent(int event); }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void send(BlockEntity be, int event) {
        if (be.getLevel() instanceof ServerLevel level) PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(be.getBlockPos()), new TileEventPayload(be.getBlockPos(), event));
    }

    public static void handle(TileEventPayload p, IPayloadContext ctx) {
        var level = ctx.player().level();
        if (level.isLoaded(p.pos) && level.getBlockEntity(p.pos) instanceof Listener l) l.onNetworkEvent(p.event);
    }
}
