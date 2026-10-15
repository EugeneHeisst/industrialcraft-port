package ic2.core.util;

import ic2.core.upgrade.IUpgradableBlock;
import ic2.core.upgrade.IUpgradeItem;
import ic2.core.upgrade.UpgradeRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Tooltips drawn by IC2 GUIs: opaque black box with a light border, white shadowed text. */
public final class GuiTooltipHelper {
    private static final int BACKGROUND = 0xFF000000, BORDER = 0xBFBABABA;
    private GuiTooltipHelper() {}

    public static void drawUpgradeslotTooltip(GuiGraphics g, Font font, int x, int y, int minX, int minY, int maxX, int maxY, IUpgradableBlock te, int yoffset, int xoffset) {
        if (x < minX || x > maxX || y < minY || y > maxY) return;
        String title = Component.translatable("ic2.generic.text.upgrade").getString();
        int width = font.width(title);
        List<ItemStack> upgrades = getCompatibleUpgrades(te);
        for (ItemStack stack : upgrades) width = Math.max(width, font.width(stack.getHoverName()));
        drawTooltip(g, font, x, y, yoffset, xoffset, title, true, width);
        yoffset += 15;
        for (ItemStack stack : upgrades) { drawTooltip(g, font, x, y, yoffset, xoffset, stack.getHoverName().getString(), false, width); yoffset += 14; }
    }

    private static List<ItemStack> getCompatibleUpgrades(IUpgradableBlock block) {
        List<ItemStack> ret = new ArrayList<>();
        for (ItemStack stack : UpgradeRegistry.getUpgrades()) if (((IUpgradeItem)stack.getItem()).isSuitableFor(stack, block.getUpgradableProperties())) ret.add(stack);
        return ret;
    }

    public static void drawAreaTooltip(GuiGraphics g, Font font, int x, int y, String tooltip, int minX, int minY, int maxX, int maxY) {
        if (x >= minX && x <= maxX && y >= minY && y <= maxY) drawTooltip(g, font, x, y, 0, 0, tooltip, true, 0);
    }

    public static void drawTooltipArea(GuiGraphics g, Font font, int x, int y, String tooltip, int minX, int minY, int maxX, int maxY, int yoffset, int xoffset) {
        if (x >= minX && x <= maxX && y >= minY && y <= maxY) drawTooltip(g, font, x, y, yoffset, xoffset, tooltip, true, 0);
    }

    public static void drawTooltip(GuiGraphics g, Font font, int x, int y, int yoffset, int xoffset, String tooltip, boolean drawborder, int width) {
        if (width == 0) x -= font.width(tooltip) / 2;
        y -= 12;
        x += xoffset;
        y += yoffset;
        if (width == 0) width = font.width(tooltip);
        width += 8;
        int height = 8;
        g.pose().pushPose();
        g.pose().translate(0, 0, 300);
        g.fill(x - 3, y - 4, x + width + 3, y - 3, BACKGROUND);
        g.fill(x - 3, y + height + 3, x + width + 3, y + height + 4, BACKGROUND);
        g.fill(x - 3, y - 3, x + width + 3, y + height + 3, BACKGROUND);
        g.fill(x - 4, y - 3, x - 3, y + height + 3, BACKGROUND);
        g.fill(x + width + 3, y - 3, x + width + 4, y + height + 3, BACKGROUND);
        if (drawborder) {
            g.fill(x - 3, y - 2, x - 2, y + height + 2, BORDER);
            g.fill(x + width + 2, y - 2, x + width + 3, y + height + 2, BORDER);
            g.fill(x - 3, y - 3, x + width + 3, y - 2, BORDER);
            g.fill(x - 3, y + height + 2, x + width + 3, y + height + 3, BORDER);
        }
        g.drawString(font, tooltip, x + 4, y, 0xFFFFFFFE, true);
        g.pose().popPose();
    }
}
