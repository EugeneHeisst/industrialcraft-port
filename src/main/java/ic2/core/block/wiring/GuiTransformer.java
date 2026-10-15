package ic2.core.block.wiring;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.network.ContainerEventPayload;
import ic2.core.ref.Ic2Items;
import ic2.core.util.GuiTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class GuiTransformer extends GuiIC2<ContainerTransformer> {
    private static final ResourceLocation background = IC2.id("textures/gui/guitransfomer.png");

    public GuiTransformer(ContainerTransformer container, Inventory inventory, Component title) { super(container, inventory, title, 219); }

    private void send(int event) { PacketDistributor.sendToServer(new ContainerEventPayload(menu.containerId, Integer.toString(event))); }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < 3; i++) {
            int event = i;
            addRenderableWidget(Button.builder(Component.translatable("ic2.Transformer.gui.switch.mode" + (i + 1)), b -> send(event)).bounds(leftPos + 7, topPos + 65 + i * 20, 144, 20).build());
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int x = (int)mx - leftPos, y = (int)my - topPos;
        if (x >= 150 && y >= 32 && x <= 167 && y <= 49) send(3);
        return super.mouseClicked(mx, my, button);
    }

    @Override
    protected void drawForeground(GuiGraphics g, int mouseX, int mouseY) {
        var base = menu.base;
        g.drawString(font, Component.translatable("ic2.Transformer.gui.Output"), 6, 30, 4210752, false);
        g.drawString(font, Component.translatable("ic2.Transformer.gui.Input"), 6, 43, 4210752, false);
        g.drawString(font, base.getoutputflow() + " " + Component.translatable("ic2.generic.text.EUt").getString(), 52, 30, 2157374, false);
        g.drawString(font, base.getinputflow() + " " + Component.translatable("ic2.generic.text.EUt").getString(), 52, 45, 2157374, false);
        GuiTooltipHelper.drawTooltipArea(g, font, mouseX, mouseY, Component.translatable("ic2.Transformer.gui.refresh").getString(), 150, 32, 167, 49, 0, -50);
        g.renderItem(new ItemStack(Ic2Items.WRENCH.get()), 152, 67 + base.getMode() * 20);
    }

    @Override public Component getName() { return Component.literal(menu.base.getTyp() + " - " + Component.translatable("ic2.blockTransformer").getString()); }
    @Override public ResourceLocation getResourceLocation() { return background; }
}
