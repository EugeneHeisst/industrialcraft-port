package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerPump;
import ic2.core.util.DrawUtil;
import ic2.core.util.GuiTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiPump extends GuiIC2<ContainerPump> {
    public GuiPump(ContainerPump c, Inventory i, Component t) { super(c, i, t); }
    @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
        var fluid = menu.base.getFluidStackfromTank();
        if (!fluid.isEmpty()) GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, fluid.getHoverName().getString() + ": " + fluid.getAmount() + Component.translatable("ic2.generic.text.mb").getString(), 73, 19, 86, 67);
    }
    @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
        int charge = (int)(14 * menu.base.getChargeLevel());
        int progress = (int)(24 * menu.base.guiProgress);
        if (charge > 0) drawTexturedModalRect(g, xoffset + 7, yoffset + 42 - charge, 176, 14 - charge, 14, charge);
        if (progress > 0) drawTexturedModalRect(g, xoffset + 36, yoffset + 34, 176, 14, progress + 1, 16);
        if (menu.base.getTankAmount() > 0) {
            drawTexturedModalRect(g, xoffset + 70, yoffset + 16, 176, 30, 20, 55);
            int h = menu.base.gaugeLiquidScaled(47);
            DrawUtil.drawFluid(g, menu.base.getFluidStackfromTank(), xoffset + 74, yoffset + 20 + 47 - h, 12, h);
            drawTexturedModalRect(g, xoffset + 74, yoffset + 20, 176, 85, 12, 47);
        }
    }
    @Override public Component getName() { return Component.translatable("ic2.Pump.gui.name"); }
    @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guipump.png"); }
}
