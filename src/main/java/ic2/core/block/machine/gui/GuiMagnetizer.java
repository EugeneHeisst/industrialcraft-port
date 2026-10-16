package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerElectrolyzer;
import ic2.core.block.machine.container.ContainerMagnetizer;
import ic2.core.block.misc.BlockPoleFence;
import ic2.core.util.GuiTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiMagnetizer extends GuiIC2<ContainerMagnetizer> {
    public GuiMagnetizer(ContainerMagnetizer c, Inventory i, Component t) { super(c, i, t); }
    @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
        boolean shoes = BlockPoleFence.hasMetalShoes(menu.player);
        g.drawString(font, Component.translatable(shoes ? "ic2.Magnetizer.gui.hasMetalShoes" : "ic2.Magnetizer.gui.noMetalShoes"), 18, 66, shoes ? 4259648 : 16728128, false);
    }
    @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
        int charge = (int)(14 * menu.base.getChargeLevel());
        if (charge > 0) drawTexturedModalRect(g, xoffset + 7, yoffset + 42 - charge, 176, 14 - charge, 14, charge);
    }
    @Override public Component getName() { return Component.translatable("ic2.blockMagnetizer"); }
    @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guimagnetizer.png"); }

    public static class Electrolyzer extends GuiIC2<ContainerElectrolyzer> {
        public Electrolyzer(ContainerElectrolyzer c, Inventory i, Component t) { super(c, i, t); }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.generic.text.Buffer").getString() + " " + menu.base.energy + Component.translatable("ic2.generic.text.EU").getString(), 75, 34, 106, 50);
        }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
            if (menu.base.energy > 0) drawTexturedModalRect(g, xoffset + 79, yoffset + 34, 176, 14, menu.base.gaugeEnergyScaled(24) + 1, 16);
        }
        @Override public Component getName() { return Component.translatable("ic2.Electrolyzer.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guielectrolyzer.png"); }
    }
}
