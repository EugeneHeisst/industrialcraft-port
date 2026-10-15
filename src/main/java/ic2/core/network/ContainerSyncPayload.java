package ic2.core.network;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ContainerSyncPayload(int containerId, double[] values) implements CustomPacketPayload {
    public static final Type<ContainerSyncPayload> TYPE = new Type<>(IC2.id("container_sync"));
    public static final StreamCodec<ByteBuf, ContainerSyncPayload> CODEC = StreamCodec.of((buf, p) -> {
        ByteBufCodecs.VAR_INT.encode(buf, p.containerId);
        ByteBufCodecs.VAR_INT.encode(buf, p.values.length);
        for (double v : p.values) buf.writeDouble(v);
    }, buf -> {
        int id = ByteBufCodecs.VAR_INT.decode(buf);
        double[] values = new double[ByteBufCodecs.VAR_INT.decode(buf)];
        for (int i = 0; i < values.length; i++) values[i] = buf.readDouble();
        return new ContainerSyncPayload(id, values);
    });

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ContainerSyncPayload p, IPayloadContext ctx) {
        if (ctx.player().containerMenu instanceof ContainerBase<?> menu && menu.containerId == p.containerId) menu.receiveSync(p.values);
    }
}
