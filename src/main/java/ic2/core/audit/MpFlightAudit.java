package ic2.core.audit;

import ic2.core.IC2;
import ic2.core.ref.Ic2Items;
import ic2.core.util.Util;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * -Dic2.audit=mpflight on the dedicated dev server (allow-flight=false): the joining player climbs slowly on a charged
 * electric jetpack in hover mode for 220 ticks (the kick comes after 80 floating ticks), then the jetpack's in-air reset is
 * switched off and the server must kick for flying. Run with ./mp-flight-audit.sh.
 */
@EventBusSubscriber(modid = IC2.MODID)
public final class MpFlightAudit {
    private static final boolean ENABLED = "mpflight".equals(System.getProperty("ic2.audit"));
    private static UUID flyer;
    private static int t = -1, maxInAir;
    private static double startY;
    private static boolean flightOk, kicked;
    private MpFlightAudit() {}

    @SubscribeEvent
    static void join(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ENABLED || !(event.getEntity() instanceof ServerPlayer sp)) return;
        flyer = sp.getUUID();
        t = 0;
        sp.setGameMode(GameType.SURVIVAL);
        var jetpack = Ic2Items.ELECTRIC_JETPACK.get().charged();
        CustomData.update(DataComponents.CUSTOM_DATA, jetpack, tag -> tag.putBoolean("hoverMode", true));
        sp.setItemSlot(EquipmentSlot.CHEST, jetpack);
        sp.teleportTo(sp.serverLevel(), sp.getX(), 100, sp.getZ(), 0, 0);
        startY = 100;
        IC2.LOG.info("[mp-audit] {} joined, allow-flight={}, electric jetpack in hover mode", sp.getName().getString(), sp.server.isFlightAllowed());
    }

    @SubscribeEvent
    static void leave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!ENABLED || !event.getEntity().getUUID().equals(flyer)) return;
        kicked = true;
        IC2.LOG.info("[mp-audit] player left at tick {} (reset disabled: {})", t, Util.auditDisableAirReset);
    }

    @SubscribeEvent
    static void tick(ServerTickEvent.Post event) {
        if (!ENABLED || t < 0) return;
        t++;
        ServerPlayer sp = flyer == null ? null : event.getServer().getPlayerList().getPlayer(flyer);
        if (sp != null && !Util.auditDisableAirReset) maxInAir = Math.max(maxInAir, Util.playerInAirTime(sp));
        if (t == 240) {
            flightOk = sp != null && !kicked && sp.getY() > startY + 5;
            IC2.LOG.info("[mp-audit] {} jetpack flight on an allow-flight=false server: connected={} climbed {} blocks, max floating ticks {}", flightOk ? "PASS" : "FAIL", sp != null, sp == null ? 0 : sp.getY() - startY, maxInAir);
            Util.auditDisableAirReset = true;
        }
        if (t > 240 && kicked || t == 480) {
            IC2.LOG.info("[mp-audit] {} control: without the reset the server kicks for flying (kicked={} after {} ticks)", kicked ? "PASS" : "FAIL", kicked, t - 240);
            IC2.LOG.info("[mp-audit] DONE {}", flightOk && kicked ? "PASS" : "FAIL");
            t = -1;
            event.getServer().halt(false);
        }
    }
}
