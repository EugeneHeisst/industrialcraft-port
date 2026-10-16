package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerUu;
import ic2.core.block.machine.tileentity.TileEntityReplicator;
import ic2.core.block.machine.tileentity.TileEntityScanner;
import ic2.core.network.ContainerEventPayload;
import ic2.core.util.DrawUtil;
import ic2.core.util.GuiTooltipHelper;
import ic2.core.util.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/** GUIs of the scanner, replicator and pattern storage. */
public final class GuiUu {
    private GuiUu() {}
    static String tr(String key) { return Component.translatable(key).getString(); }

    public static class Scanner extends GuiIC2<ContainerUu.Scanner> {
        public Scanner(ContainerUu.Scanner c, Inventory i, Component t) { super(c, i, t); }
        @Override protected void renderLabels(GuiGraphics g, int mx, int my) {
            var te = menu.base;
            String name = tr("ic2.Scanner.gui.name");
            g.drawString(font, name, (imageWidth / 2 - font.width(name)) / 2, 6, 4210752, false);
            String res = tr("ic2.Scanner.gui.info5") + ":";
            float sc = Math.min(1f, 66f / font.width(res));
            g.pose().pushPose(); g.pose().translate(105, 6 + (1 - sc) * 4, 0); g.pose().scale(sc, sc, 1);
            g.drawString(font, res, 0, 0, 4210752, false);
            g.pose().popPose();
            int x = mx - leftPos, y = my - topPos;
            switch (te.state) {
                case IDLE -> g.drawString(font, tr("ic2.Scanner.gui.idle"), 10, 69, 15461152, false);
                case NO_STORAGE -> g.drawString(font, tr("ic2.Scanner.gui.info2"), 10, 69, 15461152, false);
                case SCANNING -> { g.drawString(font, tr("ic2.Scanner.gui.info1"), 10, 69, 2157374, false); g.drawString(font, te.getPercentageDone() + "%", 125, 69, 2157374, false); }
                case NO_ENERGY -> g.drawString(font, tr("ic2.Scanner.gui.info3"), 10, 69, 14094352, false);
                case ALREADY_RECORDED -> g.drawString(font, tr("ic2.Scanner.gui.info8"), 10, 69, 14094352, false);
                case FAILED -> { g.drawString(font, tr("ic2.Scanner.gui.info4"), 10, 69, 2157374, false); g.drawString(font, tr("ic2.Scanner.gui.info6"), 110, 30, 14094352, false); }
                case COMPLETED, TRANSFER_ERROR -> {
                    if (te.state == TileEntityScanner.State.COMPLETED) g.drawString(font, tr("ic2.Scanner.gui.info4"), 10, 69, 2157374, false);
                    else g.drawString(font, tr("ic2.Scanner.gui.info7"), 10, 69, 14094352, false);
                    g.drawString(font, Util.toSiString(te.patternUu, 4) + "B UUM", 105, 25, 16777215, false);
                    g.drawString(font, Util.toSiString(te.patternEu, 4) + "EU", 105, 36, 16777215, false);
                }
            }
            if (te.state == TileEntityScanner.State.COMPLETED || te.state == TileEntityScanner.State.TRANSFER_ERROR || te.state == TileEntityScanner.State.FAILED) {
                GuiTooltipHelper.drawAreaTooltip(g, font, x, y, tr("ic2.Scanner.gui.button.delete"), 102, 49, 114, 61);
                if (te.state != TileEntityScanner.State.FAILED) GuiTooltipHelper.drawAreaTooltip(g, font, x, y, tr("ic2.Scanner.gui.button.save"), 143, 49, 167, 61);
            }
        }
        @Override public boolean mouseClicked(double mx, double my, int b) {
            int x = (int)mx - leftPos, y = (int)my - topPos;
            if (menu.base.isDone()) {
                if (x >= 102 && y >= 49 && x <= 111 && y <= 61) PacketDistributor.sendToServer(new ContainerEventPayload(menu.containerId, "0"));
                if (x >= 143 && y >= 49 && x <= 167 && y <= 61) PacketDistributor.sendToServer(new ContainerEventPayload(menu.containerId, "1"));
            }
            return super.mouseClicked(mx, my, b);
        }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
            var te = menu.base;
            if (te.isDone()) { drawTexturedModalRect(g, xoffset + 102, yoffset + 49, 176, 57, 12, 12); drawTexturedModalRect(g, xoffset + 143, yoffset + 49, 176, 69, 24, 12); }
            int charge = (int)(14 * te.getChargeLevel()), loop = te.getSubPercentageDoneScaled(66);
            if (charge > 0) drawTexturedModalRect(g, xoffset + 9, yoffset + 38 - charge, 176, 14 - charge, 14, charge);
            if (loop > 0) drawTexturedModalRect(g, xoffset + 30, yoffset + 20, 176, 14, loop, 43);
        }
        @Override public Component getName() { return Component.translatable("ic2.Scanner.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guiscanner.png"); }
    }

    public static class Replicator extends GuiIC2<ContainerUu.Replicator> {
        public Replicator(ContainerUu.Replicator c, Inventory i, Component t) { super(c, i, t, 184); }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
            var te = menu.base;
            int progressUu = te.patternUu != 0 ? Math.min((int)Math.round(100 * te.uuProcessed / te.patternUu), 100) : 0;
            switch (te.mode) {
                case STOPPED -> g.drawString(font, tr("ic2.Replicator.gui.info.Waiting"), 54, 41, 15461152, false);
                case SINGLE, CONTINUOUS -> {
                    g.drawString(font, "UU:" + progressUu + "%", 51, 41, 2157374, false);
                    g.drawString(font, "EU:0%", 92, 41, 2157374, false);
                    g.drawString(font, te.mode == TileEntityReplicator.Mode.SINGLE ? ">" : ">>", te.mode == TileEntityReplicator.Mode.SINGLE ? 134 : 130, 41, 2157374, false);
                }
            }
            var f = te.getFluidStackfromTank();
            if (!f.isEmpty()) GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, f.getHoverName().getString() + ": " + f.getAmount() + "mB", 30, 33, 43, 81);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, tr("ic2.Replicator.gui.info.Stop"), 74, 81, 91, 98);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, tr("ic2.Replicator.gui.info.single"), 92, 81, 108, 98);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, tr("ic2.Replicator.gui.info.repeat"), 109, 81, 125, 98);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, tr("ic2.Replicator.gui.info.last"), 80, 16, 88, 33);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, tr("ic2.Replicator.gui.info.next"), 109, 16, 117, 33);
            if (!te.pattern.isEmpty())
                GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, te.pattern.getHoverName().getString() + " UU: " + Util.toSiString(te.patternUu, 4) + tr("ic2.generic.text.bucketUnit") + " EU: " + Util.toSiString(te.patternEu, 4) + tr("ic2.generic.text.EU"), 90, 16, 107, 33);
        }
        @Override public boolean mouseClicked(double mx, double my, int b) {
            int x = (int)mx - leftPos, y = (int)my - topPos;
            String e = x >= 80 && y >= 16 && x <= 88 && y <= 33 ? "0" : x >= 109 && y >= 16 && x <= 117 && y <= 33 ? "1" : x >= 75 && y >= 82 && x <= 90 && y <= 97 ? "3"
                : x >= 92 && y >= 82 && x <= 107 && y <= 97 ? "4" : x >= 109 && y >= 82 && x <= 124 && y <= 97 ? "5" : null;
            if (e != null) PacketDistributor.sendToServer(new ContainerEventPayload(menu.containerId, e));
            return super.mouseClicked(mx, my, b);
        }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
            var te = menu.base;
            int charge = (int)(14 * te.getChargeLevel());
            if (charge > 0) drawTexturedModalRect(g, xoffset + 132, yoffset + 97 - charge, 176, 14 - charge, 14, charge);
            if (te.getTankAmount() > 0) {
                drawTexturedModalRect(g, xoffset + 27, yoffset + 30, 176, 15, 20, 55);
                int h = te.gaugeLiquidScaled(47);
                DrawUtil.drawFluid(g, te.getFluidStackfromTank(), xoffset + 31, yoffset + 34 + 47 - h, 12, h);
                drawTexturedModalRect(g, xoffset + 31, yoffset + 34, 176, 70, 12, 47);
            }
            if (!te.pattern.isEmpty()) g.renderItem(te.pattern, xoffset + 91, yoffset + 17);
        }
        @Override public Component getName() { return Component.translatable("ic2.Replicator.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guireplicator.png"); }
    }

    public static class PatternStorage extends GuiIC2<ContainerUu.PatternStorage> {
        public PatternStorage(ContainerUu.PatternStorage c, Inventory i, Component t) { super(c, i, t); }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
            var te = menu.base;
            String idx = Math.min(te.index + 1, te.maxIndex) + " / " + te.maxIndex;
            g.drawString(font, idx, (imageWidth - font.width(idx)) / 2, 30, 4210752, false);
            g.drawString(font, tr("ic2.generic.text.Name") + " ", 10, 48, 16777215, false);
            g.drawString(font, tr("ic2.generic.text.UUMatte") + " ", 10, 59, 16777215, false);
            g.drawString(font, tr("ic2.generic.text.Energy") + " ", 10, 70, 16777215, false);
            if (!te.pattern.isEmpty()) {
                g.drawString(font, Util.toSiString(te.patternUu, 4) + tr("ic2.generic.text.bucketUnit"), 80, 59, 16777215, false);
                g.drawString(font, Util.toSiString(te.patternEu, 4) + tr("ic2.generic.text.EU"), 80, 70, 16777215, false);
                g.drawString(font, te.pattern.getHoverName(), 40, 48, 16777215, false);
            }
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, tr("ic2.PatternStorage.gui.info.last"), 7, 19, 15, 36);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, tr("ic2.PatternStorage.gui.info.next"), 36, 19, 44, 36);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, tr("ic2.PatternStorage.gui.info.export"), 10, 37, 25, 44);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, tr("ic2.PatternStorage.gui.info.import"), 26, 37, 41, 44);
        }
        @Override public boolean mouseClicked(double mx, double my, int b) {
            int x = (int)mx - leftPos, y = (int)my - topPos;
            String e = x >= 7 && y >= 19 && x <= 15 && y <= 36 ? "0" : x >= 36 && y >= 19 && x <= 44 && y <= 36 ? "1" : x >= 10 && y >= 37 && x <= 25 && y <= 44 ? "2" : x >= 26 && y >= 37 && x <= 41 && y <= 44 ? "3" : null;
            if (e != null) PacketDistributor.sendToServer(new ContainerEventPayload(menu.containerId, e));
            return super.mouseClicked(mx, my, b);
        }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) { if (!menu.base.pattern.isEmpty()) g.renderItem(menu.base.pattern, xoffset + 152, yoffset + 29); }
        @Override public Component getName() { return Component.translatable("ic2.PatternStorage.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guipatternstorage.png"); }
    }
}
