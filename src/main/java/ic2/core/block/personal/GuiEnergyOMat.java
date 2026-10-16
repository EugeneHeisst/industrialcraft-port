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

public class GuiEnergyOMat extends GuiIC2<ContainerEnergyOMat> {
    private static final ResourceLocation open = IC2.id("textures/gui/guienergyomatopen.png"), closed = IC2.id("textures/gui/guienergyomatclosed.png");
    private static final String[] LABELS = {"-100k", "-10k", "-1k", "-100", "+100k", "+10k", "+1k", "+100"};

    public GuiEnergyOMat(ContainerEnergyOMat container, Inventory inventory, Component title) { super(container, inventory, title); }

    @Override
    protected void init() {
        super.init();
        if (!menu.open) return;
        for (int i = 0; i < 8; i++) {
            String event = Integer.toString(i);
            addRenderableWidget(Button.builder(Component.literal(LABELS[i]), b -> PacketDistributor.sendToServer(new ContainerEventPayload(menu.containerId, event)))
                .bounds(leftPos + (i < 4 ? 102 : 134), topPos + 16 + (i % 4) * 10, 32, 10).build());
        }
    }

    @Override
    protected void drawForeground(GuiGraphics g, int mouseX, int mouseY) {
        var base = menu.base;
        g.drawString(font, Component.translatable("container.inventory"), 8, imageHeight - 96 + 2, 4210752, false);
        if (menu.open) {
            g.drawString(font, Component.translatable("ic2.container.personalTrader.offer"), 100, 60, 4210752, false);
            g.drawString(font, base.euOffer + " EU", 100, 68, 4210752, false);
        } else {
            drawFit(g, Component.translatable("ic2.container.personalTrader.want"), 12, 21, 36, 4210752);
            drawFit(g, Component.translatable("ic2.container.personalTrader.offer"), 12, 39, 36, 4210752);
            g.drawString(font, base.euOffer + " EU", 50, 39, 4210752, false);
            drawFit(g, Component.translatable("ic2.container.personalTraderEnergy.paidFor", base.paidFor), 12, 57, 124, 4210752);
        }
    }

    @Override public Component getName() { return Component.translatable("ic2.blockPersonalTraderEnergy"); }
    @Override public ResourceLocation getResourceLocation() { return menu.open ? open : closed; }
}
