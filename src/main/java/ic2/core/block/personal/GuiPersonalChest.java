package ic2.core.block.personal;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiPersonalChest extends GuiIC2<ContainerPersonalChest> {
    private static final ResourceLocation background = IC2.id("textures/gui/guipersonalchest.png");

    public GuiPersonalChest(ContainerPersonalChest container, Inventory inventory, Component title) { super(container, inventory, title, 222); }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, getName(), 8, 6, 4210752, false);
        g.drawString(font, Component.translatable("container.inventory"), 8, imageHeight - 96 + 2, 4210752, false);
    }

    @Override public Component getName() { return Component.translatable("ic2.blockPersonalChest"); }
    @Override public ResourceLocation getResourceLocation() { return background; }
}
