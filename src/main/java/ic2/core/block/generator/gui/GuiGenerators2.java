package ic2.core.block.generator.gui;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.generator.container.ContainerGenerators2;
import ic2.core.util.DrawUtil;
import ic2.core.util.GuiTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class GuiGenerators2 {
    private GuiGenerators2() {}

    public static class RTG extends GuiIC2<ContainerGenerators2.RTG> {
        public RTG(ContainerGenerators2.RTG c, Inventory i, Component t) { super(c, i, t); }
        @Override protected int titleY() { return 4; }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) { GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.generic.text.bufferEU", String.valueOf(menu.base.storage)).getString(), 117, 38, 150, 48); }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) { drawTexturedModalRect(g, xoffset + 119, yoffset + 40, 179, 3, menu.base.gaugeStorageScaled(31), 8); }
        @Override public Component getName() { return Component.translatable("ic2.RTGenerator.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guirtgenerator.png"); }
    }

    public static class Semifluid extends GuiIC2<ContainerGenerators2.Semifluid> {
        public Semifluid(ContainerGenerators2.Semifluid c, Inventory i, Component t) { super(c, i, t); }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
            var fluid = menu.base.getFluidStackfromTank();
            if (!fluid.isEmpty()) GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, fluid.getHoverName().getString() + ": " + fluid.getAmount() + "mB", 73, 23, 83, 71);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.generic.text.bufferEU", String.valueOf(menu.base.storage)).getString(), 108, 25, 139, 41);
        }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
            if (menu.base.storage > 0) drawTexturedModalRect(g, xoffset + 111, yoffset + 25, 176, 0, menu.base.gaugeStorageScaled(25), 17);
            if (menu.base.getTankAmount() > 0) {
                drawTexturedModalRect(g, xoffset + 70, yoffset + 20, 176, 17, 20, 55);
                int h = menu.base.gaugeLiquidScaled(47);
                DrawUtil.drawFluid(g, menu.base.getFluidStackfromTank(), xoffset + 74, yoffset + 24 + 47 - h, 12, h);
                drawTexturedModalRect(g, xoffset + 74, yoffset + 24, 176, 72, 12, 47);
            }
        }
        @Override public Component getName() { return Component.translatable("ic2.blockSemifluidGenerator"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guifluidgenerator.png"); }
    }

    public static class Stirling extends GuiIC2<ContainerGenerators2.Stirling> {
        public Stirling(ContainerGenerators2.Stirling c, Inventory i, Component t) { super(c, i, t); }
        @Override protected int titleY() { return 4; }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
            String s = Component.translatable("ic2.generic.text.hu").getString() + menu.base.receivedheat + " / " + menu.base.production + Component.translatable("ic2.generic.text.EU").getString();
            g.drawString(font, s, (imageWidth - font.width(s)) / 2, 52, 5752026, false);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.generic.text.bufferEU", String.valueOf(menu.base.EUstorage)).getString(), 59, 33, 116, 46);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.StirlingGenerator.gui.productiontooltip").getString(), 40, 48, 137, 62);
        }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) { drawTexturedModalRect(g, xoffset + 62, yoffset + 36, 179, 18, menu.base.gaugeEUStorageScaled(58), 8); }
        @Override public Component getName() { return Component.translatable("ic2.StirlingGenerator.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guistirlinggenerator.png"); }
    }

    public static class Kinetic extends GuiIC2<ContainerGenerators2.Kinetic> {
        public Kinetic(ContainerGenerators2.Kinetic c, Inventory i, Component t) { super(c, i, t); }
        @Override protected int titleY() { return 4; }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
            g.drawString(font, Component.translatable("ic2.KineticGenerator.gui.Output", String.valueOf(Math.round(menu.base.getproduction() * 10) / 10f)), 42, 52, 2157374, false);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.generic.text.bufferEU", String.valueOf((int)menu.base.EUstorage)).getString(), 59, 33, 116, 46);
        }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) { drawTexturedModalRect(g, xoffset + 62, yoffset + 36, 179, 18, menu.base.gaugeEUStorageScaled(58), 8); }
        @Override public Component getName() { return Component.translatable("ic2.KineticGenerator.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guikineticgenerator.png"); }
    }
}
