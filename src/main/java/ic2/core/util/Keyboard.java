package ic2.core.util;

import ic2.core.IC2;
import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Per-player IC2 key state (ALT, boost, mode switch, ...) sent by the client whenever it changes. */
public final class Keyboard {
    public enum Key { alt, boost, forward, modeSwitch, jump, sideInventory, hubMode }
    private static final Map<Player, Set<Key>> playerKeys = new WeakHashMap<>();
    private Keyboard() {}

    public static boolean isAltKeyDown(Player p) { return get(p, Key.alt); }
    public static boolean isBoostKeyDown(Player p) { return get(p, Key.boost); }
    public static boolean isForwardKeyDown(Player p) { return get(p, Key.forward); }
    public static boolean isJumpKeyDown(Player p) { return get(p, Key.jump); }
    public static boolean isModeSwitchKeyDown(Player p) { return get(p, Key.modeSwitch); }
    public static boolean isSideinventoryKeyDown(Player p) { return get(p, Key.sideInventory); }
    public static boolean isHudModeKeyDown(Player p) { return get(p, Key.hubMode); }
    public static boolean isSneakKeyDown(Player p) { return p.isShiftKeyDown(); }

    private static synchronized boolean get(Player p, Key key) { var keys = playerKeys.get(p); return keys != null && keys.contains(key); }
    public static synchronized void processKeyUpdate(Player p, int state) {
        Set<Key> keys = EnumSet.noneOf(Key.class);
        for (Key k : Key.values()) if ((state & 1 << k.ordinal()) != 0) keys.add(k);
        playerKeys.put(p, keys);
    }

    public record KeyStatePayload(int state) implements CustomPacketPayload {
        public static final Type<KeyStatePayload> TYPE = new Type<>(IC2.id("keys"));
        public static final StreamCodec<ByteBuf, KeyStatePayload> CODEC = ByteBufCodecs.VAR_INT.map(KeyStatePayload::new, KeyStatePayload::state);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
        public static void handle(KeyStatePayload p, IPayloadContext ctx) { processKeyUpdate(ctx.player(), p.state); }
    }
}
