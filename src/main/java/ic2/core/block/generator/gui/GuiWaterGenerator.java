package ic2.core.block.generator.gui;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.generator.container.ContainerWaterGenerator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiWaterGenerator extends GuiIC2<ContainerWaterGenerator> {
    private static final ResourceLocation background = IC2.id("textures/gui/guiwatergenerator.png");
    public GuiWaterGenerator(ContainerWaterGenerator container, Inventory inventory, Component title) { super(container, inventory, title); }

    @Override
    protected void drawBackground(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        if (menu.base.fuel > 0) { int l = menu.base.gaugeFuelScaled(14); drawTexturedModalRect(g, xoffset + 80, yoffset + 36 + 14 - l, 176, 14 - l, 14, l); }
    }

    @Override public Component getName() { return Component.translatable("ic2.WaterGenerator.gui.name"); }
    @Override public ResourceLocation getResourceLocation() { return background; }
}
