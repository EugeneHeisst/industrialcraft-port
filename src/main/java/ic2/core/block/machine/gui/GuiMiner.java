package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerMiner;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiMiner extends GuiIC2<ContainerMiner> {
    public GuiMiner(ContainerMiner c, Inventory i, Component t) { super(c, i, t); }
    @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
        int len = Math.round(menu.base.getChargeLevel() * 14);
        if (len > 0) drawTexturedModalRect(g, xoffset + 151, yoffset + 41 + 14 - len, 176, 14 - len, 14, len);
    }
    @Override public Component getName() { return Component.translatable("ic2.Miner.gui.name"); }
    @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guiminer.png"); }
}
