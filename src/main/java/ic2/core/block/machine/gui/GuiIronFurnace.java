package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerIronFurnace;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiIronFurnace extends GuiIC2<ContainerIronFurnace> {
    private static final ResourceLocation background = IC2.id("textures/gui/guifurnace.png");
    public GuiIronFurnace(ContainerIronFurnace container, Inventory inventory, Component title) { super(container, inventory, title); }

    @Override
    protected void drawBackground(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        if (menu.base.fuel > 0) { int l = menu.base.gaugeFuelScaled(12); drawTexturedModalRect(g, xoffset + 56, yoffset + 36 + 12 - l, 176, 12 - l, 14, l + 2); }
        drawTexturedModalRect(g, xoffset + 79, yoffset + 34, 176, 14, menu.base.gaugeProgressScaled(24) + 1, 16);
    }

    @Override public Component getName() { return Component.translatable("ic2.IronFurnace.gui.name"); }
    @Override public ResourceLocation getResourceLocation() { return background; }
}
