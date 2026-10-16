package ic2.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ic2.core.IC2;
import ic2.core.item.tool.EntityMiningLaser;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/** Crossed-quad beam like the arrow renderer (RenderCrossed of the original) with the laser texture. */
public class RenderMiningLaser extends EntityRenderer<EntityMiningLaser> {
    private static final ResourceLocation TEXTURE = IC2.id("textures/models/laser.png");
    public RenderMiningLaser(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override
    public void render(EntityMiningLaser e, float yaw, float pt, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(pt, e.yRotO, e.getYRot()) - 90));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(pt, e.xRotO, e.getXRot())));
        pose.mulPose(Axis.XP.rotationDegrees(45));
        pose.scale(0.05625f, 0.05625f, 0.05625f);
        pose.translate(-4, 0, 0);
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        int full = 15728880;
        for (int j = 0; j < 4; j++) {
            pose.mulPose(Axis.XP.rotationDegrees(90));
            Matrix4f m = pose.last().pose();
            v(vc, pose, m, -8, -2, 0, 0, 0, full);
            v(vc, pose, m, 8, -2, 0, 0.5f, 0, full);
            v(vc, pose, m, 8, 2, 0, 0.5f, 0.15625f, full);
            v(vc, pose, m, -8, 2, 0, 0, 0.15625f, full);
        }
        pose.popPose();
        super.render(e, yaw, pt, pose, buffers, light);
    }

    private static void v(VertexConsumer vc, PoseStack pose, Matrix4f m, float x, float y, float z, float u, float v, int light) {
        vc.addVertex(m, x, y, z).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose.last(), 0, 1, 0);
    }

    @Override public ResourceLocation getTextureLocation(EntityMiningLaser e) { return TEXTURE; }
}
