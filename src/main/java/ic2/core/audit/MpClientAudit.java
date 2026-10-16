package ic2.core.audit;

import ic2.core.IC2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** -Dic2.audit=mpclient: joins the dev server, holds jump (jetpack thrust) and reports how the connection ends. */
@EventBusSubscriber(modid = IC2.MODID, value = Dist.CLIENT)
public final class MpClientAudit {
    private static final boolean ENABLED = "mpclient".equals(System.getProperty("ic2.audit"));
    private static int ticks;
    private MpClientAudit() {}

    @SubscribeEvent
    static void tick(ClientTickEvent.Post event) {
        if (!ENABLED) return;
        Minecraft mc = Minecraft.getInstance();
        ticks++;
        if (mc.player != null && mc.getConnection() != null) mc.options.keyJump.setDown(true);
        if (mc.screen instanceof DisconnectedScreen ds) {
            IC2.LOG.info("[mp-audit] client disconnected: {}", ds.getTitle().getString() + " / " + (ds.children().isEmpty() ? "" : ds.getNarrationMessage().getString()));
            mc.stop();
        }
        if (ticks > 6000) mc.stop();
    }
}
