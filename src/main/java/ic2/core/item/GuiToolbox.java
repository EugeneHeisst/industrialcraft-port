package ic2.core.item;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiToolbox extends GuiIC2<ItemToolbox.Container> {
    public GuiToolbox(ItemToolbox.Container c, Inventory i, Component t) { super(c, i, t); }
    @Override protected void renderLabels(GuiGraphics g, int mx, int my) { g.drawString(font, getName(), 65, 11, 0, false); }
    @Override public Component getName() { return Component.translatable("ic2.itemToolbox"); }
    @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guitoolbox.png"); }
}
