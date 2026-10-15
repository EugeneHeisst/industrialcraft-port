package ic2.core.block.generator.gui;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.generator.container.ContainerSolarGenerator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiSolarGenerator extends GuiIC2<ContainerSolarGenerator> {
    private static final ResourceLocation background = IC2.id("textures/gui/guisolargenerator.png");
    public GuiSolarGenerator(ContainerSolarGenerator container, Inventory inventory, Component title) { super(container, inventory, title); }

    @Override
    protected void drawBackground(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        if (menu.base.sunIsVisible) drawTexturedModalRect(g, xoffset + 80, yoffset + 45, 176, 0, 14, 14);
    }

    @Override public Component getName() { return Component.translatable("ic2.SolarGenerator.gui.name"); }
    @Override public ResourceLocation getResourceLocation() { return background; }
}
