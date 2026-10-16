package ic2.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ic2.core.block.Retexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Draws a full cube whose sides show the obscurator textures, the other sides the block's own. */
public final class RetextureRender {
    private RetextureRender() {}

    // top-left corner, right and down axes of each side as seen from outside (vanilla texture orientation)
    private static final float[][][] FACES = new float[6][][];
    static {
        FACES[Direction.DOWN.ordinal()] = new float[][]{{0, 0, 1}, {1, 0, 0}, {0, 0, -1}};
        FACES[Direction.UP.ordinal()] = new float[][]{{0, 1, 0}, {1, 0, 0}, {0, 0, 1}};
        FACES[Direction.NORTH.ordinal()] = new float[][]{{1, 1, 0}, {-1, 0, 0}, {0, -1, 0}};
        FACES[Direction.SOUTH.ordinal()] = new float[][]{{0, 1, 1}, {1, 0, 0}, {0, -1, 0}};
        FACES[Direction.WEST.ordinal()] = new float[][]{{0, 1, 0}, {0, 0, 1}, {0, -1, 0}};
        FACES[Direction.EAST.ordinal()] = new float[][]{{1, 1, 1}, {0, 0, -1}, {0, -1, 0}};
    }

    public static void renderCube(Retexture rt, BlockState own, BlockAndTintGetter level, BlockPos pos, PoseStack pose, MultiBufferSource buffers) {
        var mc = Minecraft.getInstance();
        VertexConsumer vc = buffers.getBuffer(Sheets.cutoutBlockSheet());
        for (Direction d : Direction.values()) {
            BlockPos np = pos.relative(d);
            if (!Block.shouldRenderFace(own, level, pos, d, np)) continue;
            BlockState src = rt.state(d);
            Direction side = rt.refSide(d);
            if (src == null) { src = own; side = d; }
            var model = mc.getBlockRenderer().getBlockModel(src);
            var quads = new java.util.ArrayList<>(model.getQuads(src, side, RandomSource.create(42), ModelData.EMPTY, null));
            if (quads.isEmpty()) for (BakedQuad q : model.getQuads(src, null, RandomSource.create(42), ModelData.EMPTY, null)) if (q.getDirection() == side) quads.add(q);
            int light = LevelRenderer.getLightColor(level, np);
            for (BakedQuad q : quads) {
                int color = q.isTinted() ? mc.getBlockColors().getColor(src, level, pos, q.getTintIndex()) : -1;
                face(vc, pose, d, q.getSprite(), color, light);
            }
        }
    }

    private static void face(VertexConsumer vc, PoseStack pose, Direction d, net.minecraft.client.renderer.texture.TextureAtlasSprite sprite, int color, int light) {
        float[][] f = FACES[d.ordinal()];
        float r = (color >> 16 & 255) / 255f, g = (color >> 8 & 255) / 255f, b = (color & 255) / 255f;
        float[][] uv = {{0, 0}, {0, 1}, {1, 1}, {1, 0}};
        var last = pose.last();
        for (float[] c : uv) {
            float x = f[0][0] + f[1][0] * c[0] + f[2][0] * c[1], y = f[0][1] + f[1][1] * c[0] + f[2][1] * c[1], z = f[0][2] + f[1][2] * c[0] + f[2][2] * c[1];
            vc.addVertex(last, x, y, z).setColor(r, g, b, 1).setUv(sprite.getU0() + (sprite.getU1() - sprite.getU0()) * c[0], sprite.getV0() + (sprite.getV1() - sprite.getV0()) * c[1])
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(last, d.getStepX(), d.getStepY(), d.getStepZ());
        }
    }
}
