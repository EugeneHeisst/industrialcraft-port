package ic2.core.uu;

import ic2.core.IC2;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Builds the UU graph when the server starts / reloads data and sends the values to clients (scanner slot checks, tooltips). */
@EventBusSubscriber(modid = IC2.MODID)
public final class UuSync {
    private UuSync() {}

    public record Values(List<Integer> ids, List<Double> values) implements CustomPacketPayload {
        public static final Type<Values> TYPE = new Type<>(IC2.id("uu_values"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Values> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), Values::ids, ByteBufCodecs.DOUBLE.apply(ByteBufCodecs.list()), Values::values, Values::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
        public static void handle(Values p, IPayloadContext ctx) {
            var map = new IdentityHashMap<net.minecraft.world.item.Item, Double>();
            for (int i = 0; i < p.ids.size(); i++) map.put(BuiltInRegistries.ITEM.byId(p.ids.get(i)), p.values.get(i));
            ctx.enqueueWork(() -> { if (ctx.player().level().isClientSide) UuGraph.setAll(map); });
        }
    }

    static Values payload() {
        List<Integer> ids = new ArrayList<>(); List<Double> vals = new ArrayList<>();
        UuGraph.all().forEach((item, v) -> { ids.add(BuiltInRegistries.ITEM.getId(item)); vals.add(v); });
        return new Values(ids, vals);
    }

    @SubscribeEvent static void started(ServerStartedEvent e) { UuGraph.build(e.getServer()); }

    @SubscribeEvent
    static void sync(OnDatapackSyncEvent e) {
        if (e.getPlayer() == null) UuGraph.build(e.getPlayerList().getServer());
        Values p = payload();
        if (e.getPlayer() != null) PacketDistributor.sendToPlayer(e.getPlayer(), p);
        else for (ServerPlayer sp : e.getPlayerList().getPlayers()) PacketDistributor.sendToPlayer(sp, p);
    }
}
