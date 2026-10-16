package ic2.core.client;

import ic2.core.IC2;
import ic2.core.item.tool.ItemScanner;
import ic2.core.ref.Ic2Items;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Scan result of the OD/OV scanner: up to ten "count x item" lines above the (read-only) player inventory, like the original. */
public class GuiToolScanner extends Screen {
    private static final ResourceLocation background = IC2.id("textures/gui/guitoolscanner.png");
    private static final int W = 176, H = 231;
    private final ItemScanner.ScanResult result;

    public GuiToolScanner(ItemScanner.ScanResult result, Component title) { super(title); this.result = result; }

    public static void open(ItemScanner.ScanResult result) {
        var player = Minecraft.getInstance().player;
        boolean adv = player != null && (player.getMainHandItem().is(Ic2Items.OV_SCANNER.get()) || !player.getMainHandItem().is(Ic2Items.OD_SCANNER.get()) && player.getOffhandItem().is(Ic2Items.OV_SCANNER.get()));
        Minecraft.getInstance().setScreen(new GuiToolScanner(result, Component.translatable(adv ? "ic2.itemScannerAdv" : "ic2.itemScanner")));
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        int x = (width - W) / 2, y = (height - H) / 2;
        g.drawString(font, title, x + (W - font.width(title)) / 2, y + 6, 4210752, false);
        g.drawString(font, Component.translatable("ic2.itemScanner.found"), x + 10, y + 20, 2157374, false);
        for (int i = 0; i < result.items().size() && i < 10; i++)
            g.drawString(font, result.counts().get(i) + "x" + result.items().get(i).getHoverName().getString(), x + 10, y + 35 + i * 11, 5752026, false);
        var inv = minecraft.player.getInventory();
        for (int i = 0; i < 36; i++) {
            int sx = x + 8 + (i % 9) * 18, sy = i < 9 ? y + H - 24 : y + H - 82 + (i / 9 - 1) * 18;
            g.renderItem(inv.items.get(i), sx, sy);
            g.renderItemDecorations(font, inv.items.get(i), sx, sy);
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        super.renderBackground(g, mx, my, pt);
        g.blit(background, (width - W) / 2, (height - H) / 2, 0, 0, W, H);
    }

    @Override public boolean isPauseScreen() { return false; }
}
