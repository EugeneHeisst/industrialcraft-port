package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerMatter;
import ic2.core.util.DrawUtil;
import ic2.core.util.GuiTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiMatter extends GuiIC2<ContainerMatter> {
    public GuiMatter(ContainerMatter c, Inventory i, Component t) { super(c, i, t); }
    @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
        g.drawString(font, Component.translatable("ic2.Matter.gui.info.progress"), 8, 22, 4210752, false);
        g.drawString(font, menu.base.getProgressAsString(), 18, 31, 4210752, false);
        if (menu.base.scrap > 0) {
            g.drawString(font, Component.translatable("ic2.Matter.gui.info.amplifier"), 8, 46, 4210752, false);
            g.drawString(font, "" + menu.base.scrap, 8, 58, 4210752, false);
        }
        var fluid = menu.base.getFluidStackfromTank();
        if (!fluid.isEmpty()) GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, fluid.getHoverName().getString() + ": " + fluid.getAmount() + Component.translatable("ic2.generic.text.mb").getString(), 99, 25, 112, 73);
    }
    @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
        if (menu.base.getTankAmount() > 0) {
            drawTexturedModalRect(g, xoffset + 96, yoffset + 22, 176, 0, 20, 55);
            int h = menu.base.gaugeLiquidScaled(47);
            DrawUtil.drawFluid(g, menu.base.getFluidStackfromTank(), xoffset + 100, yoffset + 26 + 47 - h, 12, h);
            drawTexturedModalRect(g, xoffset + 100, yoffset + 26, 176, 55, 12, 47);
        }
    }
    @Override public Component getName() { return Component.translatable("ic2.Matter.gui.name"); }
    @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guimatter.png"); }
}
