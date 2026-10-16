package ic2.core.block.generator.gui;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.generator.container.ContainerGeoGenerator;
import ic2.core.util.DrawUtil;
import ic2.core.util.GuiTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiGeoGenerator extends GuiIC2<ContainerGeoGenerator> {
    private static final ResourceLocation background = IC2.id("textures/gui/guifluidgenerator.png");
    public GuiGeoGenerator(ContainerGeoGenerator container, Inventory inventory, Component title) { super(container, inventory, title); }

    @Override
    protected void drawForeground(GuiGraphics g, int mouseX, int mouseY) {
        var fluid = menu.base.getFluidStackfromTank();
        if (!fluid.isEmpty()) GuiTooltipHelper.drawAreaTooltip(g, font, mouseX, mouseY, fluid.getHoverName().getString() + ": " + fluid.getAmount() + "mB", 73, 23, 83, 71);
        GuiTooltipHelper.drawAreaTooltip(g, font, mouseX, mouseY, Component.translatable("ic2.generic.text.bufferEU", String.valueOf(menu.base.storage)).getString(), 108, 25, 139, 41);
    }

    @Override
    protected void drawBackground(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        if (menu.base.storage > 0) drawTexturedModalRect(g, xoffset + 111, yoffset + 25, 176, 0, menu.base.gaugeStorageScaled(25), 17);
        if (menu.base.getTankAmount() > 0) {
            drawTexturedModalRect(g, xoffset + 70, yoffset + 20, 176, 17, 20, 55);
            int liquidHeight = menu.base.gaugeLiquidScaled(47);
            DrawUtil.drawFluid(g, menu.base.getFluidStackfromTank(), xoffset + 74, yoffset + 24 + 47 - liquidHeight, 12, liquidHeight);
            drawTexturedModalRect(g, xoffset + 74, yoffset + 24, 176, 72, 12, 47);
        }
    }

    @Override public Component getName() { return Component.translatable("ic2.GeoGenerator.gui.name"); }
    @Override public ResourceLocation getResourceLocation() { return background; }
}
