package ic2.core.block.heatgenerator;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.util.GuiTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class GuiHeatGenerators {
    private GuiHeatGenerators() {}

    static void heatLine(GuiIC2<?> gui, GuiGraphics g, net.minecraft.client.gui.Font font, int transmit, int max, String tooltipKey, int mx, int my, int width) {
        String s = transmit + " / " + max;
        g.drawString(font, s, (width - font.width(s)) / 2, 70, 5752026, false);
        GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable(tooltipKey).getString(), 48, 65, 128, 79);
    }

    public static class Solid extends GuiIC2<ContainerHeatGenerators.Solid> {
        public Solid(ContainerHeatGenerators.Solid c, Inventory i, Component t) { super(c, i, t); }
        @Override protected int titleY() { return 4; }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) { heatLine(this, g, font, menu.base.transmitHeat, menu.base.maxHeatEmitpeerTick, "ic2.SolidHeatGenerator.gui.tooltipheat", mx, my, imageWidth); }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
            if (menu.base.fuel > 0) { int l = menu.base.gaugeFuelScaled(12); drawTexturedModalRect(g, xoffset + 81, yoffset + 29 + 12 - l, 176, 12 - l, 14, l + 2); }
        }
        @Override public Component getName() { return Component.translatable("ic2.SolidHeatGenerator.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guisolidheatgenerator.png"); }
    }

    public static class RT extends GuiIC2<ContainerHeatGenerators.RT> {
        public RT(ContainerHeatGenerators.RT c, Inventory i, Component t) { super(c, i, t); }
        @Override protected int titleY() { return 4; }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) { heatLine(this, g, font, menu.base.transmitHeat, menu.base.maxHeatEmitpeerTick, "ic2.RTHeatGenerator.gui.tooltipheat", mx, my, imageWidth); }
        @Override public Component getName() { return Component.translatable("ic2.RTHeatGenerator.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guirtheatgenerator.png"); }
    }

    public static class Electric extends GuiIC2<ContainerHeatGenerators.Electric> {
        public Electric(ContainerHeatGenerators.Electric c, Inventory i, Component t) { super(c, i, t); }
        @Override protected int titleY() { return 4; }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
            g.drawString(font, Component.translatable("ic2.ElectricHeatGenerator.gui.hUmax", menu.base.transmitHeat, menu.base.maxHeatEmitpeerTick), 38, 70, 5752026, false);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.ElectricHeatGenerator.gui.tooltipheat").getString(), 48, 65, 128, 79);
            GuiTooltipHelper.drawTooltipArea(g, font, mx, my, Component.translatable("ic2.ElectricHeatGenerator.gui.coils").getString(), 61, 26, 114, 62, -15, 0);
        }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
            int charge = (int)(14 * menu.base.getChargeLevel());
            if (charge > 0) drawTexturedModalRect(g, xoffset + 9, yoffset + 57 - charge, 176, 14 - charge, 14, charge);
        }
        @Override public Component getName() { return Component.translatable("ic2.ElectricHeatGenerator.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guielectricheatgenerator.png"); }
    }
}
