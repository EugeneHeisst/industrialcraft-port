package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerStandardMachine;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** GuiMacerator, GuiExtractor, GuiCompressor, GuiElecFurnace and GuiRecycler of the original: same layout, own texture and name. */
public class GuiStandardMachine extends GuiIC2<ContainerStandardMachine<?>> {
    private final ResourceLocation texture;
    private final String nameKey;

    public GuiStandardMachine(ContainerStandardMachine<?> container, Inventory inventory, Component title, String texture, String nameKey) {
        super(container, inventory, title);
        this.texture = IC2.id("textures/gui/" + texture + ".png");
        this.nameKey = nameKey;
    }

    @Override
    protected void drawBackground(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        TileEntityStandardMachine base = menu.base;
        int chargeLevel = (int)(14 * base.getChargeLevel());
        int progress = (int)(24 * base.getProgress());
        if (chargeLevel > 0) drawTexturedModalRect(g, xoffset + 56, yoffset + 36 + 14 - chargeLevel, 176, 14 - chargeLevel, 14, chargeLevel);
        if (progress > 0) drawTexturedModalRect(g, xoffset + 79, yoffset + 34, 176, 14, progress + 1, 16);
    }

    @Override public Component getName() { return Component.translatable(nameKey); }
    @Override public ResourceLocation getResourceLocation() { return texture; }
}
