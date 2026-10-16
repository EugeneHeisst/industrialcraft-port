package ic2.core.block.kineticgenerator;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.util.GuiTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class GuiKineticGenerators {
    private GuiKineticGenerators() {}

    public static class Wind extends GuiIC2<ContainerKineticGenerators.Wind> {
        public Wind(ContainerKineticGenerators.Wind c, Inventory i, Component t) { super(c, i, t); }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
            var b = menu.base;
            if (!b.checkrotor()) { g.drawString(font, Component.translatable("ic2.WindKineticGenerator.gui.rotormiss"), 27, 52, 2157374, false); return; }
            if (!b.rotorspace()) { g.drawString(font, Component.translatable("ic2.WindKineticGenerator.gui.rotorspace"), 20, 52, 2157374, false); return; }
            if (!b.guiisminWindStrength()) {
                g.drawString(font, Component.translatable("ic2.WindKineticGenerator.gui.windweak1"), 27, 52, 2157374, false);
                g.drawString(font, Component.translatable("ic2.WindKineticGenerator.gui.windweak2"), 24, 69, 2157374, false);
                return;
            }
            g.drawString(font, Component.translatable("ic2.WindKineticGenerator.gui.output", b.getKuOutput()), 55, 52, 2157374, false);
            g.drawString(font, b.getRotorhealth() + " %", 46, 70, 2157374, false);
            if (b.guiisoverload()) {
                GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.WindKineticGenerator.error.overload").getString(), 44, 20, 79, 45);
                GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.WindKineticGenerator.error.overload2").getString(), 102, 20, 131, 45);
            }
        }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
            if (menu.base.guiisoverload() && menu.base.checkrotor()) { drawTexturedModalRect(g, xoffset + 44, yoffset + 20, 176, 0, 30, 26); drawTexturedModalRect(g, xoffset + 102, yoffset + 20, 176, 0, 30, 26); }
        }
        @Override public Component getName() { return Component.translatable("ic2.WindKineticGenerator.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guiwindkineticgenerator.png"); }
    }

    public static class Water extends GuiIC2<ContainerKineticGenerators.Water> {
        public Water(ContainerKineticGenerators.Water c, Inventory i, Component t) { super(c, i, t); }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
            var b = menu.base;
            if (b.type == -1) {
                g.drawString(font, Component.translatable("ic2.WaterKineticGenerator.gui.wrongbiome1"), 38, 52, 2157374, false);
                g.drawString(font, Component.translatable("ic2.WaterKineticGenerator.gui.wrongbiome2"), 45, 69, 2157374, false);
            } else if (!b.rotorSlot.isEmpty()) {
                if (menu.spaceBlocked != 0) g.drawString(font, Component.translatable("ic2.WaterKineticGenerator.gui.rotorspace"), 20, 52, 2157374, false);
                else {
                    g.drawString(font, Component.translatable("ic2.WaterKineticGenerator.gui.output", b.getKuOutput()), 55, 52, 2157374, false);
                    g.drawString(font, b.getRotorhealth() + " %", 46, 70, 2157374, false);
                }
            } else g.drawString(font, Component.translatable("ic2.WaterKineticGenerator.gui.rotormiss"), 27, 52, 2157374, false);
        }
        @Override public Component getName() { return Component.translatable("ic2.WaterKineticGenerator.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guiwaterkineticgenerator.png"); }
    }

    public static class Electric extends GuiIC2<ContainerKineticGenerators.Electric> {
        public Electric(ContainerKineticGenerators.Electric c, Inventory i, Component t) { super(c, i, t); }
        @Override protected int titleY() { return 4; }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
            g.drawString(font, Component.translatable("ic2.ElectricKineticGenerator.gui.kUmax", menu.base.getMaxKU(), menu.base.getMaxKUForGUI()), 33, 70, 5752026, false);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.ElectricKineticGenerator.gui.tooltipkin").getString(), 48, 65, 128, 79);
            GuiTooltipHelper.drawTooltipArea(g, font, mx, my, Component.translatable("ic2.ElectricKineticGenerator.gui.motors").getString(), 61, 26, 114, 62, -15, 0);
        }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
            int c = (int)(14 * menu.base.getChargeLevel());
            if (c > 0) drawTexturedModalRect(g, xoffset + 9, yoffset + 57 - c, 176, 14 - c, 14, c);
        }
        @Override public Component getName() { return Component.translatable("ic2.ElectricKineticGenerator.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guielectrickineticgenerator.png"); }
    }
}
