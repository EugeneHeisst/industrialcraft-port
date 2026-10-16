package ic2.core.block.reactor;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.util.DrawUtil;
import ic2.core.util.GuiTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;

public class GuiNuclearReactor extends GuiIC2<ContainerNuclearReactor> {
    private static final ResourceLocation BG = IC2.id("textures/gui/guinuclearreaktor.png"), BG_FLUID = IC2.id("textures/gui/guinuclearreaktorfluid.png");
    public GuiNuclearReactor(ContainerNuclearReactor c, Inventory i, Component t) { super(c, i, t, 212, 243); }

    @Override
    protected void drawForeground(GuiGraphics g, int mx, int my) {
        var b = menu.base;
        int temp = b.maxHeat == 0 ? 0 : 100 * b.heat / b.maxHeat;
        if (b.isFluidCooled()) {
            g.drawString(font, Component.translatable("ic2.NuclearReactor.gui.info.hU", b.emitHeat, temp).getString() + "%", 8, 140, 5752026, false);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.NuclearReactor.gui.mode.fluid").getString(), 5, 160, 22, 177);
            tip(g, b.inputTank.getFluid(), mx, my, 10, 54, 21, 100);
            tip(g, b.outputTank.getFluid(), mx, my, 190, 54, 202, 100);
        } else {
            g.drawString(font, Component.translatable("ic2.NuclearReactor.gui.info.EU", Math.round(b.output * 5), temp).getString() + "%", 8, 140, 5752026, false);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.NuclearReactor.gui.mode.electric").getString(), 5, 160, 22, 177);
        }
    }

    private void tip(GuiGraphics g, FluidStack f, int mx, int my, int x1, int y1, int x2, int y2) {
        if (!f.isEmpty()) GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, f.getHoverName().getString() + ": " + f.getAmount() + Component.translatable("ic2.generic.text.mb").getString(), x1, y1, x2, y2);
    }

    @Override
    protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
        var b = menu.base;
        for (int y = 0; y < 6; y++) for (int x = menu.size; x < 9; x++) drawTexturedModalRect(g, xoffset + 26 + x * 18, yoffset + 25 + y * 18, 213, 1, 16, 16);
        if (!b.isFluidCooled()) return;
        int i2 = b.gaugeHeatScaled(160);
        for (int row = 0; row < 7; row++) drawTexturedModalRect(g, xoffset + 26 + 160 - i2, yoffset + 23 + row * 18, 0, 243, i2, 2);
        if (b.inputTank.getFluidAmount() > 0) {
            int h = b.gaugeLiquidScaled(47, 0);
            DrawUtil.drawFluid(g, b.inputTank.getFluid(), xoffset + 10, yoffset + 101 - h, 12, h);
            g.blit(BG, xoffset + 11, yoffset + 59, 218, 123, 9, 37);
        }
        if (b.outputTank.getFluidAmount() > 0) {
            int h = b.gaugeLiquidScaled(47, 1);
            DrawUtil.drawFluid(g, b.outputTank.getFluid(), xoffset + 190, yoffset + 101 - h, 12, h);
            g.blit(BG, xoffset + 192, yoffset + 59, 218, 81, 9, 37);
        }
    }

    @Override public Component getName() { return Component.translatable("ic2.NuclearReactor.gui.name"); }
    @Override public ResourceLocation getResourceLocation() { return menu.base.isFluidCooled() ? BG_FLUID : BG; }

    public static class FluidPort extends GuiIC2<ContainerReactorFluidPort> {
        public FluidPort(ContainerReactorFluidPort c, Inventory i, Component t) { super(c, i, t); }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) { GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.ReactorFluidPort.gui.info").getString(), 80, 43, 95, 58); }
        @Override public Component getName() { return Component.translatable("ic2.ReactorFluidPort.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guireactorfluidport.png"); }
    }
}
