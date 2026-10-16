package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerCanner;
import ic2.core.block.machine.container.ContainerFluidBottler;
import ic2.core.block.machine.container.ContainerSolidCanner;
import ic2.core.network.ContainerEventPayload;
import ic2.core.util.DrawUtil;
import ic2.core.util.GuiTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.network.PacketDistributor;

/** GUIs of the canning machine, solid canning machine and bottling plant. */
public final class GuiCanners {
    private GuiCanners() {}

    static String tankTip(FluidStack f) { return f.getHoverName().getString() + ": " + f.getAmount() + Component.translatable("ic2.generic.text.mb").getString(); }

    public static class Canner extends GuiIC2<ContainerCanner> {
        public Canner(ContainerCanner c, Inventory i, Component t) { super(c, i, t, 184); }

        @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
            String tip = Component.translatable("ic2.Canner.gui.switch." + menu.base.getMode().name()).getString();
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, tip, 63, 81, 112, 94);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.Canner.gui.switchTanks").getString(), 77, 64, 100, 77);
            if (!menu.base.inputTank.isEmpty()) GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, tankTip(menu.base.inputTank.getFluid()), 42, 45, 55, 93);
            if (!menu.base.outputTank.isEmpty()) GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, tankTip(menu.base.outputTank.getFluid()), 120, 45, 133, 93);
        }

        @Override public boolean mouseClicked(double mx, double my, int button) {
            int x = (int)mx - leftPos, y = (int)my - topPos;
            if (x >= 63 && y >= 81 && x <= 112 && y <= 94) { PacketDistributor.sendToServer(new ContainerEventPayload(menu.containerId, "0")); return true; }
            if (x >= 77 && y >= 64 && x <= 100 && y <= 77) { PacketDistributor.sendToServer(new ContainerEventPayload(menu.containerId, "1")); return true; }
            return super.mouseClicked(mx, my, button);
        }

        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
            switch (menu.base.getMode()) {
                case BottleSolid -> { drawTexturedModalRect(g, xoffset + 63, yoffset + 81, 196, 20, 50, 14); drawTexturedModalRect(g, xoffset + 59, yoffset + 53, 3, 4, 9, 18); drawTexturedModalRect(g, xoffset + 99, yoffset + 53, 3, 4, 18, 23); }
                case EmptyLiquid -> { drawTexturedModalRect(g, xoffset + 63, yoffset + 81, 196, 35, 50, 14); drawTexturedModalRect(g, xoffset + 71, yoffset + 43, 196, 0, 26, 18); drawTexturedModalRect(g, xoffset + 59, yoffset + 53, 3, 4, 9, 18); }
                case BottleLiquid -> { drawTexturedModalRect(g, xoffset + 63, yoffset + 81, 196, 50, 50, 14); drawTexturedModalRect(g, xoffset + 99, yoffset + 53, 3, 4, 18, 23); drawTexturedModalRect(g, xoffset + 71, yoffset + 43, 196, 0, 26, 18); }
                case EnrichLiquid -> drawTexturedModalRect(g, xoffset + 63, yoffset + 81, 196, 65, 50, 14);
            }
            int charge = Math.round(menu.base.getChargeLevel() * 14);
            if (charge > 0) drawTexturedModalRect(g, xoffset + 9, yoffset + 61 + 14 - charge, 176, 14 - charge, 14, charge);
            int progress = Math.round(menu.base.getProgress() * 23);
            if (progress > 0) drawTexturedModalRect(g, xoffset + 74, yoffset + 22, 233, 0, progress, 14);
            tank(g, menu.base.inputTank, 39, menu.base.gaugeLiquidScaled(47, 0));
            tank(g, menu.base.outputTank, 117, menu.base.gaugeLiquidScaled(47, 1));
        }

        private void tank(GuiGraphics g, FluidTank tank, int x, int h) {
            if (tank.isEmpty()) return;
            drawTexturedModalRect(g, xoffset + x, yoffset + 42, 176, 14, 20, 55);
            DrawUtil.drawFluid(g, tank.getFluid(), xoffset + x + 4, yoffset + 46 + 47 - h, 12, h);
            drawTexturedModalRect(g, xoffset + x + 4, yoffset + 46, 176, 69, 12, 47);
        }

        @Override public Component getName() { return Component.translatable("ic2.Canner.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guicanner.png"); }
    }

    public static class SolidCanner extends GuiIC2<ContainerSolidCanner> {
        public SolidCanner(ContainerSolidCanner c, Inventory i, Component t) { super(c, i, t); }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
            int charge = (int)(14 * menu.base.getChargeLevel()), progress = (int)(24 * menu.base.getProgress());
            if (charge > 0) drawTexturedModalRect(g, xoffset + 8, yoffset + 45 + 14 - charge, 176, 14 - charge, 14, charge);
            if (progress > 0) drawTexturedModalRect(g, xoffset + 88, yoffset + 35, 176, 14, progress + 1, 16);
        }
        @Override public Component getName() { return Component.translatable("ic2.blockSolidCanner"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guisolidcanner.png"); }
    }

    public static class FluidBottler extends GuiIC2<ContainerFluidBottler> {
        public FluidBottler(ContainerFluidBottler c, Inventory i, Component t) { super(c, i, t, 184); }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
            var f = menu.base.getFluidStackfromTank();
            if (!f.isEmpty()) GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, tankTip(f), 81, 37, 94, 85);
        }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
            int charge = (int)(14 * menu.base.getChargeLevel());
            if (charge > 0) drawTexturedModalRect(g, xoffset + 9, yoffset + 48 - charge, 176, 14 - charge, 14, charge);
            int p = Math.round(menu.base.getProgress() * 16);
            if (p > 0) {
                drawTexturedModalRect(g, xoffset + 61, yoffset + 36, 198, 0, p, 13);
                drawTexturedModalRect(g, xoffset + 61, yoffset + 73, 198, 0, p, 13);
                drawTexturedModalRect(g, xoffset + 99, yoffset + 55, 198, 0, p, 13);
            }
            if (menu.base.getTankAmount() > 0) {
                drawTexturedModalRect(g, xoffset + 78, yoffset + 35, 176, 15, 20, 55);
                int h = menu.base.gaugeLiquidScaled(47);
                DrawUtil.drawFluid(g, menu.base.getFluidStackfromTank(), xoffset + 82, yoffset + 38 + 47 - h, 12, h);
                drawTexturedModalRect(g, xoffset + 82, yoffset + 38, 176, 70, 12, 47);
            }
        }
        @Override public Component getName() { return Component.translatable("ic2.FluidBottler.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guibottler.png"); }
    }
}
