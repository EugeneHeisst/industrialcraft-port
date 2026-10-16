package ic2.core.block.wiring;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.network.ContainerEventPayload;
import ic2.core.util.GuiTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

public class GuiChargepad extends GuiIC2<ContainerChargepad> {
    private static final ItemStack REDSTONE = new ItemStack(Items.REDSTONE);
    public GuiChargepad(ContainerChargepad c, Inventory i, Component t) { super(c, i, t, 162); }
    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.empty(), b -> PacketDistributor.sendToServer(new ContainerEventPayload(menu.containerId, "redstone"))).bounds(leftPos + 152, topPos + 4, 20, 20).build());
    }
    @Override public void render(GuiGraphics g, int mx, int my, float pt) { super.render(g, mx, my, pt); g.renderItem(REDSTONE, leftPos + 154, topPos + 6); }
    @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
        var base = menu.base;
        g.drawString(font, Component.translatable("ic2.EUStorage.gui.info.level"), 79, 25, 4210752, false);
        g.drawString(font, " " + (int)Math.min(base.energy, base.maxStorage), 110, 35, 4210752, false);
        g.drawString(font, "/" + base.maxStorage, 110, 45, 4210752, false);
        GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, base.getRedstoneMode().getString(), 153, 3, 172, 22);
    }
    @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
        if (menu.base.energy > 0) drawTexturedModalRect(g, xoffset + 79, yoffset + 34, 176, 14, (int)(24 * menu.base.getChargeLevel()) + 1, 16);
    }
    @Override public Component getName() { return Component.translatable(switch (menu.base.tier) { case 1 -> "ic2.blockChargepadBatBox"; case 2 -> "ic2.blockChargepadCESU"; case 3 -> "ic2.blockChargepadMFE"; default -> "ic2.blockChargepadMFSU"; }); }
    @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guichargepadblock.png"); }
}
