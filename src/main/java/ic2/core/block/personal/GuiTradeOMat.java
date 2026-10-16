package ic2.core.block.personal;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.network.ContainerEventPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class GuiTradeOMat extends GuiIC2<ContainerTradeOMat> {
    private static final ResourceLocation open = IC2.id("textures/gui/guitradeomatopen.png"), closed = IC2.id("textures/gui/guitradeomatclosed.png");

    public GuiTradeOMat(ContainerTradeOMat container, Inventory inventory, Component title) { super(container, inventory, title); }

    @Override
    protected void init() {
        super.init();
        if (menu.open && menu.admin)
            addRenderableWidget(Button.builder(Component.literal("∞"), b -> PacketDistributor.sendToServer(new ContainerEventPayload(menu.containerId, "0"))).bounds(leftPos + 152, topPos + 4, 20, 20).build());
    }

    @Override
    protected void drawForeground(GuiGraphics g, int mouseX, int mouseY) {
        var base = menu.base;
        String stock = base.stock < 0 ? "∞" : Integer.toString(base.stock);
        g.drawString(font, Component.translatable("container.inventory"), 8, imageHeight - 96 + 2, 4210752, false);
        drawFit(g, Component.translatable("ic2.container.personalTrader.want"), 12, 23, 36, 4210752);
        if (menu.open) {
            drawFit(g, Component.translatable("ic2.container.personalTrader.offer"), 12, 57, 36, 4210752);
            g.drawString(font, Component.translatable("ic2.container.personalTrader.totalTrades0"), 108, 28, 4210752, false);
            g.drawString(font, Component.translatable("ic2.container.personalTrader.totalTrades1"), 108, 36, 4210752, false);
            g.drawString(font, Integer.toString(base.totalTradeCount), 112, 44, 4210752, false);
            drawFit(g, Component.literal(Component.translatable("ic2.container.personalTrader.stock").getString() + " " + stock), 108, 60, 62, 4210752);
        } else {
            drawFit(g, Component.translatable("ic2.container.personalTrader.offer"), 12, 42, 36, 4210752);
            drawFit(g, Component.translatable("ic2.container.personalTrader.stock"), 12, 60, 36, 4210752);
            g.drawString(font, stock, 50, 60, base.stock != 0 ? 4210752 : 16733525, false);
        }
    }

    @Override public Component getName() { return Component.translatable("ic2.blockPersonalTrader"); }
    @Override public ResourceLocation getResourceLocation() { return menu.open ? open : closed; }
}
