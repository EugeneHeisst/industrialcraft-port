package ic2.core.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public final class DrawUtil {
    private DrawUtil() {}

    /** Fluid still texture tiled over the area (DrawUtil.drawRepeated of the original). */
    public static void drawFluid(GuiGraphics g, FluidStack fluid, int x, int y, int width, int height) {
        if (fluid.isEmpty() || height <= 0) return;
        var ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ext.getStillTexture(fluid));
        int color = ext.getTintColor(fluid);
        float r = (color >> 16 & 255) / 255f, gr = (color >> 8 & 255) / 255f, b = (color & 255) / 255f;
        for (int dy = 0; dy < height; dy += 16) for (int dx = 0; dx < width; dx += 16) {
            int w = Math.min(16, width - dx), h = Math.min(16, height - dy);
            g.blit(x + dx, y + height - dy - h, 0, w, h, sprite, r, gr, b, 1);
        }
    }
}
