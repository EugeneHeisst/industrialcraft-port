package ic2.core.block.generator.gui;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.generator.container.ContainerGenerator;
import ic2.core.util.GuiTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiGenerator extends GuiIC2<ContainerGenerator> {
    private static final ResourceLocation background = IC2.id("textures/gui/guigenerator.png");
    public GuiGenerator(ContainerGenerator container, Inventory inventory, Component title) { super(container, inventory, title); }

    @Override
    protected void drawForeground(GuiGraphics g, int mouseX, int mouseY) {
        GuiTooltipHelper.drawAreaTooltip(g, font, mouseX, mouseY, Component.translatable("ic2.generic.text.bufferEU", String.valueOf(menu.base.storage)).getString(), 90, 35, 121, 51);
    }

    @Override
    protected void drawBackground(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        if (menu.base.fuel > 0) { int l = menu.base.gaugeFuelScaled(12); drawTexturedModalRect(g, xoffset + 66, yoffset + 36 + 12 - l, 176, 12 - l, 14, l + 2); }
        drawTexturedModalRect(g, xoffset + 94, yoffset + 35, 176, 14, menu.base.gaugeStorageScaled(24), 17);
    }

    @Override public Component getName() { return Component.translatable("ic2.Generator.gui.name"); }
    @Override public ResourceLocation getResourceLocation() { return background; }
}
