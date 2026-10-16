package ic2.core;

import ic2.core.upgrade.IUpgradableBlock;
import ic2.core.util.GuiTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** GUI of IC2 containers: full texture background, centered title in 0x404040 and the upgrade info button. */
public abstract class GuiIC2<T extends ContainerBase<?>> extends AbstractContainerScreen<T> {
    private static final ResourceLocation INFO_BUTTON = IC2.id("textures/gui/infobutton.png");
    protected int xoffset, yoffset;

    protected GuiIC2(T container, Inventory inventory, Component title) { this(container, inventory, title, 176, 166); }
    protected GuiIC2(T container, Inventory inventory, Component title, int ySize) { this(container, inventory, title, 176, ySize); }
    protected GuiIC2(T container, Inventory inventory, Component title, int xSize, int ySize) {
        super(container, inventory, title);
        imageWidth = xSize;
        imageHeight = ySize;
    }

    public abstract Component getName();
    public abstract ResourceLocation getResourceLocation();

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        Component name = getName();
        int w = font.width(name), max = imageWidth - 34;
        if (w <= max) g.drawString(font, name, (imageWidth - w) / 2, titleY(), 4210752, false);
        else { // long translations are scaled down to fit between the info button and the right edge
            float sc = (float)max / w;
            g.pose().pushPose();
            g.pose().translate((imageWidth - max) / 2f, titleY() + (1 - sc) * 4, 0);
            g.pose().scale(sc, sc, 1);
            g.drawString(font, name, 0, 0, 4210752, false);
            g.pose().popPose();
        }
        if (menu.base instanceof IUpgradableBlock block) GuiTooltipHelper.drawUpgradeslotTooltip(g, font, mouseX - leftPos, mouseY - topPos, 0, 0, 12, 12, block, 25, 0);
        drawForeground(g, mouseX - leftPos, mouseY - topPos);
    }

    protected void drawForeground(GuiGraphics g, int mouseX, int mouseY) {}

    /** Label scaled down when a translation is wider than the room the original GUI left for it. */
    protected void drawFit(GuiGraphics g, Component text, int x, int y, int maxWidth, int color) {
        int w = font.width(text);
        if (w <= maxWidth) { g.drawString(font, text, x, y, color, false); return; }
        float sc = (float)maxWidth / w;
        g.pose().pushPose();
        g.pose().translate(x, y + (1 - sc) * 4, 0);
        g.pose().scale(sc, sc, 1);
        g.drawString(font, text, 0, 0, color, false);
        g.pose().popPose();
    }
    protected int titleY() { return 6; }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        xoffset = leftPos;
        yoffset = topPos;
        g.blit(getResourceLocation(), xoffset, yoffset, 0, 0, imageWidth, imageHeight);
        if (menu.base instanceof IUpgradableBlock) g.blit(INFO_BUTTON, xoffset + 3, yoffset + 3, 0, 0, 10, 10);
        drawBackground(g, partialTick, mouseX, mouseY);
    }

    protected void drawBackground(GuiGraphics g, float partialTick, int mouseX, int mouseY) {}

    /** drawTexturedModalRect of the original, relative to the GUI texture. */
    protected void drawTexturedModalRect(GuiGraphics g, int x, int y, int u, int v, int w, int h) { g.blit(getResourceLocation(), x, y, u, v, w, h); }
}
