package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.machine.container.*;
import ic2.core.network.ContainerEventPayload;
import ic2.core.ref.Ic2Items;
import ic2.core.util.DrawUtil;
import ic2.core.util.GuiTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** GUIs of the induction furnace, metal former, ore washing plant, thermal centrifuge, block cutter and blast furnace. */
public final class GuiMachines {
    private GuiMachines() {}

    public static class Induction extends GuiIC2<ContainerInduction> {
        public Induction(ContainerInduction c, Inventory i, Component t) { super(c, i, t); }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
            g.drawString(font, Component.translatable("ic2.generic.text.heat"), 10, 36, 4210752, false);
            g.drawString(font, menu.base.getHeat(), 10, 44, 4210752, false);
        }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
            int charge = Math.round(menu.base.getChargeLevel() * 14);
            drawTexturedModalRect(g, xoffset + 56, yoffset + 36 + 14 - charge, 176, 14 - charge, 14, charge);
            drawTexturedModalRect(g, xoffset + 79, yoffset + 34, 176, 14, menu.base.gaugeProgressScaled(24) + 1, 16);
        }
        @Override public Component getName() { return Component.translatable("ic2.Induction.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guiinduction.png"); }
    }

    public static class MetalFormer extends GuiIC2<ContainerMetalFormer> {
        public MetalFormer(ContainerMetalFormer c, Inventory i, Component t) { super(c, i, t); }
        @Override protected void init() {
            super.init();
            addRenderableWidget(Button.builder(Component.empty(), b -> PacketDistributor.sendToServer(new ContainerEventPayload(menu.containerId, "0"))).bounds(leftPos + 65, topPos + 53, 20, 20).build());
        }
        @Override public void render(GuiGraphics g, int mx, int my, float pt) {
            super.render(g, mx, my, pt);
            ItemStack icon = switch (menu.base.getMode()) { case 0 -> new ItemStack(Ic2Items.get("copper_cable")); case 1 -> new ItemStack(Ic2Items.FORGE_HAMMER.get()); default -> new ItemStack(Ic2Items.CUTTER.get()); };
            g.renderItem(icon, leftPos + 67, topPos + 55);
            String tip = Component.translatable(switch (menu.base.getMode()) { case 0 -> "ic2.MetalFormer.gui.switch.Extruding"; case 1 -> "ic2.MetalFormer.gui.switch.Rolling"; default -> "ic2.MetalFormer.gui.switch.Cutting"; }).getString();
            g.pose().pushPose(); g.pose().translate(leftPos, topPos, 0);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx - leftPos, my - topPos, tip, 65, 50, 85, 70);
            g.pose().popPose();
        }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
            int charge = (int)(14 * menu.base.getChargeLevel()), progress = (int)(48 * menu.base.getProgress());
            if (charge > 0) drawTexturedModalRect(g, xoffset + 17, yoffset + 50 - charge, 176, 14 - charge, 14, charge);
            if (progress > 0) drawTexturedModalRect(g, xoffset + 51, yoffset + 37, 177, 14, progress, 13);
        }
        @Override public Component getName() { return Component.translatable("ic2.MetalFormer.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guimetalformer.png"); }
    }

    public static class OreWashing extends GuiIC2<ContainerOreWashing> {
        public OreWashing(ContainerOreWashing c, Inventory i, Component t) { super(c, i, t); }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
            var fluid = menu.base.getFluidStackfromTank();
            if (!fluid.isEmpty()) GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, fluid.getHoverName().getString() + ": " + fluid.getAmount() + Component.translatable("ic2.generic.text.mb").getString(), 63, 23, 76, 71);
        }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
            int charge = (int)(14 * menu.base.getChargeLevel()), progress = (int)(20 * menu.base.getProgress());
            if (charge > 0) drawTexturedModalRect(g, xoffset + 9, yoffset + 57 - charge, 176, 14 - charge, 14, charge);
            if (progress > 0) drawTexturedModalRect(g, xoffset + 102, yoffset + 38, 176, 117, progress + 1, 20);
            if (menu.base.getTankAmount() > 0) {
                drawTexturedModalRect(g, xoffset + 60, yoffset + 20, 176, 15, 20, 55);
                int h = menu.base.gaugeLiquidScaled(47);
                DrawUtil.drawFluid(g, menu.base.getFluidStackfromTank(), xoffset + 64, yoffset + 24 + 47 - h, 12, h);
                drawTexturedModalRect(g, xoffset + 64, yoffset + 24, 176, 70, 12, 47);
            }
        }
        @Override public Component getName() { return Component.translatable("ic2.OreWashingPlan.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guiorewashingplant.png"); }
    }

    public static class Centrifuge extends GuiIC2<ContainerCentrifuge> {
        public Centrifuge(ContainerCentrifuge c, Inventory i, Component t) { super(c, i, t); }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.generic.text.heat").getString() + " " + menu.base.heat + "/" + menu.base.workheat, 65, 61, 91, 76);
        }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
            if (menu.base.getActive()) drawTexturedModalRect(g, xoffset + 93, yoffset + 62, 177, 36, 14, 14);
            drawTexturedModalRect(g, xoffset + 67, yoffset + 67, 176, 30, menu.base.gaugeHeatScaled(22), 6);
            int charge = (int)(14 * menu.base.getChargeLevel()), progress = (int)(30 * menu.base.getProgress());
            if (charge > 0) drawTexturedModalRect(g, xoffset + 11, yoffset + 42 + 14 - charge, 176, 14 - charge, 14, charge);
            if (progress > 0) drawTexturedModalRect(g, xoffset + 84, yoffset + 54 - progress, 177, 50, 5, progress);
        }
        @Override public Component getName() { return Component.translatable("ic2.Centrifuge.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guitermalcentrifuge.png"); }
    }

    public static class BlockCutter extends GuiIC2<ContainerBlockCutter> {
        public BlockCutter(ContainerBlockCutter c, Inventory i, Component t) { super(c, i, t); }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
            GuiTooltipHelper.drawTooltipArea(g, font, mx, my, Component.translatable("ic2.BlockCutter.gui.bladeslot").getString(), 70, 33, 85, 51, -15, 0);
            if (menu.base.isbladetoweak()) GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.BlockCutter.gui.bladetoweak").getString(), 63, 54, 93, 80);
        }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
            if (menu.base.isbladetoweak()) drawTexturedModalRect(g, xoffset + 63, yoffset + 54, 176, 34, 30, 26);
            int charge = (int)(14 * menu.base.getChargeLevel()), progress = (int)(46 * menu.base.getProgress());
            if (charge > 0) drawTexturedModalRect(g, xoffset + 26, yoffset + 36 + 14 - charge, 176, 14 - charge, 14, charge);
            if (progress > 0) drawTexturedModalRect(g, xoffset + 55, yoffset + 33, 176, 14, progress + 1, 19);
        }
        @Override public Component getName() { return Component.translatable("ic2.BlockCutter.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guiblockcutter.png"); }
    }

    public static class BlastFurnace extends GuiIC2<ContainerBlastFurnace> {
        public BlastFurnace(ContainerBlastFurnace c, Inventory i, Component t) { super(c, i, t); }
        @Override protected void drawForeground(GuiGraphics g, int mx, int my) {
            var b = menu.base;
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.BlastFurnace.gui.heat").getString() + " " + b.getheatpercent() + " %", 67, 65, 109, 80);
            GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.BlastFurnace.gui.progress").getString() + " " + b.getprogresspercent() + " %", 75, 35, 101, 60);
            GuiTooltipHelper.drawTooltipArea(g, font, mx, my, Component.translatable("ic2.BlastFurnace.gui.toolair").getString(), 25, 55, 60, 72, -15, 0);
            if (b.outOfAir) GuiTooltipHelper.drawAreaTooltip(g, font, mx, my, Component.translatable("ic2.BlastFurnace.gui.airmiss").getString(), 116, 25, 136, 47);
        }
        @Override protected void drawBackground(GuiGraphics g, float pt, int mx, int my) {
            var b = menu.base;
            drawTexturedModalRect(g, xoffset + 70, yoffset + 69, 176, 0, b.gaugeHeatScaled(22), 8);
            if (b.isHot()) drawTexturedModalRect(g, xoffset + 95, yoffset + 66, 176, 8, 14, 14);
            if (b.progress > 0) drawTexturedModalRect(g, xoffset + 75, yoffset + 34, 176, 23, 27, 27);
            if (b.outOfAir) drawTexturedModalRect(g, xoffset + 116, yoffset + 25, 176, 79, 22, 22);
            int p = b.gaugeprogress(27);
            drawTexturedModalRect(g, xoffset + 75, yoffset + 34 + 27 - p, 176, 51, 27, p);
        }
        @Override public Component getName() { return Component.translatable("ic2.BlastFurnace.gui.name"); }
        @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guiblastfurnace.png"); }
    }
}
