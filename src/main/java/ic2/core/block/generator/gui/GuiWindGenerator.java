package ic2.core.block.generator.gui;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.generator.container.ContainerWindGenerator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiWindGenerator extends GuiIC2<ContainerWindGenerator> {
    private static final ResourceLocation background = IC2.id("textures/gui/guiwindgenerator.png");
    public GuiWindGenerator(ContainerWindGenerator container, Inventory inventory, Component title) { super(container, inventory, title); }

    @Override
    protected void drawBackground(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int l = menu.base.gaugeFuelScaled(14);
        drawTexturedModalRect(g, xoffset + 80, yoffset + 45 + 14 - l, 176, 14 - l, 14, l);
        int o = menu.base.getOverheatScaled(14);
        if (o > 0) drawTexturedModalRect(g, xoffset + 80, yoffset + 45 + 14 - o, 176, 28 - o, 14, o);
    }

    @Override public Component getName() { return Component.translatable("ic2.WindGenerator.gui.name"); }
    @Override public ResourceLocation getResourceLocation() { return background; }
}
