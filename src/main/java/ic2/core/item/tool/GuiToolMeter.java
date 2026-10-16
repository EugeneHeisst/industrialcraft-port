package ic2.core.item.tool;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.network.ContainerEventPayload;
import ic2.core.util.GuiTooltipHelper;
import ic2.core.util.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class GuiToolMeter extends GuiIC2<ItemToolMeter.Container> {
    public GuiToolMeter(ItemToolMeter.Container c, Inventory i, Component t) { super(c, i, t, 217); }

    private void send(String e) { PacketDistributor.sendToServer(new ContainerEventPayload(menu.containerId, e)); }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int x = (int)mx - leftPos, y = (int)my - topPos;
        if (x >= 112 && y >= 55 && x <= 131 && y <= 74) send("mode0");
        else if (x >= 132 && y >= 55 && x <= 151 && y <= 74) send("mode1");
        else if (x >= 112 && y >= 75 && x <= 131 && y <= 94) send("mode2");
        else if (x >= 132 && y >= 75 && x <= 151 && y <= 94) send("mode3");
        else if (x >= 26 && y >= 111 && x <= 83 && y <= 123) send("reset");
        return super.mouseClicked(mx, my, button);
    }

    @Override
    protected void drawForeground(GuiGraphics g, int mx, int my) {
        var c = menu;
        String unit = Component.translatable(c.mode == ItemToolMeter.Mode.Voltage ? "ic2.generic.text.v" : "ic2.generic.text.EUt").getString();
        int col = 2157374;
        g.drawString(font, Component.translatable("ic2.itemToolMEter.mode"), 115, 43, col, false);
        g.drawString(font, Component.translatable("ic2.itemToolMEter.avg"), 15, 41, col, false);
        g.drawString(font, Util.toSiString(c.resultAvg, 6) + unit, 15, 51, col, false);
        g.drawString(font, Component.translatable("ic2.itemToolMEter.max/min"), 15, 64, col, false);
        g.drawString(font, Util.toSiString(c.resultMax, 6) + unit, 15, 74, col, false);
        g.drawString(font, Util.toSiString(c.resultMin, 6) + unit, 15, 84, col, false);
        g.drawString(font, Component.translatable("ic2.itemToolMEter.cycle", c.resultCount / 20), 15, 100, col, false);
        g.drawString(font, Component.translatable("ic2.itemToolMEter.mode.reset"), 39, 114, col, false);
        g.drawString(font, Component.translatable("ic2.itemToolMEter.mode." + c.mode.name()), 105, 100, col, false);
        String sw = Component.translatable("ic2.itemToolMEter.mode.switch").getString() + " ";
        GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, sw + Component.translatable("ic2.itemToolMEter.mode.EnergyIn").getString(), 112, 55, 131, 74);
        GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, sw + Component.translatable("ic2.itemToolMEter.mode.EnergyOut").getString(), 132, 55, 151, 74);
        GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, sw + Component.translatable("ic2.itemToolMEter.mode.EnergyGain").getString(), 112, 75, 131, 94);
        GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, sw + Component.translatable("ic2.itemToolMEter.mode.Voltage").getString(), 132, 75, 151, 94);
    }

    @Override
    protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
        int v = switch (menu.mode) { case EnergyIn -> 0; case EnergyOut -> 40; case EnergyGain -> 120; case Voltage -> 80; };
        drawTexturedModalRect(g, xoffset + 112, yoffset + 55, 176, v, 40, 40);
    }

    @Override public Component getName() { return Component.translatable("ic2.itemToolMEter"); }
    @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guitooleumeter.png"); }
}
