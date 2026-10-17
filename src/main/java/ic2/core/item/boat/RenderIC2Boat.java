package ic2.core.item.boat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ic2.core.IC2;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** The 1.7.10 boat model (bottom and four sides) with the IC2 boat textures. */
public class RenderIC2Boat extends EntityRenderer<EntityIC2Boat> {
    private static ModelPart model;

    public RenderIC2Boat(EntityRendererProvider.Context ctx) { super(ctx); shadowRadius = 0.5f; }

    private static ModelPart model() {
        if (model != null) return model;
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        float pi = (float)Math.PI;
        root.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 8).addBox(-12, -8, -3, 24, 16, 4), PartPose.offsetAndRotation(0, 4, 0, pi / 2, 0, 0));
        root.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 0).addBox(-10, -7, -1, 20, 6, 2), PartPose.offsetAndRotation(-11, 4, 0, 0, pi * 3 / 2, 0));
        root.addOrReplaceChild("front", CubeListBuilder.create().texOffs(0, 0).addBox(-10, -7, -1, 20, 6, 2), PartPose.offsetAndRotation(11, 4, 0, 0, pi / 2, 0));
        root.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 0).addBox(-10, -7, -1, 20, 6, 2), PartPose.offsetAndRotation(0, 4, -9, 0, pi, 0));
        root.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 0).addBox(-10, -7, -1, 20, 6, 2), PartPose.offset(0, 4, 9));
        return model = LayerDefinition.create(mesh, 64, 32).bakeRoot();
    }

    @Override
    public void render(EntityIC2Boat boat, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.translate(0, 0.3125, 0);
        pose.mulPose(Axis.YP.rotationDegrees(180 - yaw));
        float hurt = boat.getHurtTime() - partialTick, damage = Math.max(0, boat.getDamage() - partialTick);
        if (hurt > 0) pose.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurt) * hurt * damage / 10 * boat.getHurtDir()));
        pose.scale(-1, -1, 1);
        // Both boat models are built along X; modern Boat steering and passengers face along Z.
        // Match BoatRenderer's model rotation so the hull faces the direction its rider steers.
        pose.mulPose(Axis.YP.rotationDegrees(90));
        model().render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(boat))), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(boat, yaw, partialTick, pose, buffers, light);
    }

    @Override public ResourceLocation getTextureLocation(EntityIC2Boat boat) { return IC2.id("textures/models/" + boat.kind.texture + ".png"); }
}
