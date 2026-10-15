package ic2.core.network;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** GUI button press sent to the open server container. */
public record ContainerEventPayload(int containerId, String event) implements CustomPacketPayload {
    public static final Type<ContainerEventPayload> TYPE = new Type<>(IC2.id("container_event"));
    public static final StreamCodec<ByteBuf, ContainerEventPayload> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ContainerEventPayload::containerId, ByteBufCodecs.stringUtf8(256), ContainerEventPayload::event, ContainerEventPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(ContainerEventPayload p, IPayloadContext ctx) {
        if (ctx.player().containerMenu instanceof ContainerBase<?> menu && menu.containerId == p.containerId && menu.stillValid(ctx.player())) menu.onContainerEvent(p.event);
    }
}
