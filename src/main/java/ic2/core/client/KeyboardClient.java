package ic2.core.client;

import com.mojang.blaze3d.platform.InputConstants;
import ic2.core.IC2;
import ic2.core.util.Keyboard;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** IC2 key bindings with the original defaults: ALT=left alt, Boost=left ctrl, Mode Switch=M, Side Inventory=C, Hud Expand=X. */
public final class KeyboardClient {
    private static final String CATEGORY = "IC2";
    public static final KeyMapping altKey = new KeyMapping("ALT Key", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, CATEGORY);
    public static final KeyMapping boostKey = new KeyMapping("Boost Key", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_CONTROL, CATEGORY);
    public static final KeyMapping modeSwitchKey = new KeyMapping("Mode Switch Key", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, CATEGORY);
    public static final KeyMapping sideinventoryKey = new KeyMapping("Side Inventory Key", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY);
    public static final KeyMapping expandinfo = new KeyMapping("Hub Expand Key", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY);
    private static int lastKeyState;
    private KeyboardClient() {}

    @EventBusSubscriber(modid = IC2.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent static void register(RegisterKeyMappingsEvent event) {
            event.register(altKey); event.register(boostKey); event.register(modeSwitchKey); event.register(sideinventoryKey); event.register(expandinfo);
        }
    }

    @EventBusSubscriber(modid = IC2.MODID, value = Dist.CLIENT)
    public static final class Ticker {
        @SubscribeEvent static void tick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.getConnection() == null) return;
            int state = 0;
            if (mc.screen == null) {
                if (altKey.isDown()) state |= 1 << Keyboard.Key.alt.ordinal();
                if (boostKey.isDown()) state |= 1 << Keyboard.Key.boost.ordinal();
                if (mc.options.keyUp.isDown()) state |= 1 << Keyboard.Key.forward.ordinal();
                if (modeSwitchKey.isDown()) state |= 1 << Keyboard.Key.modeSwitch.ordinal();
                if (mc.options.keyJump.isDown()) state |= 1 << Keyboard.Key.jump.ordinal();
                if (sideinventoryKey.isDown()) state |= 1 << Keyboard.Key.sideInventory.ordinal();
                if (expandinfo.isDown()) state |= 1 << Keyboard.Key.hubMode.ordinal();
            }
            if (state != lastKeyState) {
                PacketDistributor.sendToServer(new Keyboard.KeyStatePayload(state));
                Keyboard.processKeyUpdate(mc.player, state);
                lastKeyState = state;
            }
        }
    }
}
