package ic2.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ic2.core.block.kineticgenerator.TileEntityKineticGenerators;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/** The four rotor blades (KineticGeneratorRotor of the original) spinning in front of wind and water kinetic generators. */
public class KineticGeneratorRenderer implements BlockEntityRenderer<TileEntityKineticGenerators.Rotor> {
    private static final Map<Integer, ModelPart> models = new HashMap<>();

    public KineticGeneratorRenderer(BlockEntityRendererProvider.Context ctx) {}

    private static ModelPart model(int radius) {
        return models.computeIfAbsent(radius, r -> {
            MeshDefinition mesh = new MeshDefinition();
            var root = mesh.getRoot();
            float[][] rot = {{0, -0.5f, 0}, {3.1f, 0.5f, 0}, {4.7f, 0, 0.5f}, {1.5f, 0, -0.5f}};
            for (int i = 0; i < 4; i++)
                root.addOrReplaceChild("rotor" + i, CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0, 0, -4, 1, r * 8, 8), PartPose.offsetAndRotation(-8, 0, 0, rot[i][0], rot[i][1], rot[i][2]));
            return LayerDefinition.create(mesh, 32, 256).bakeRoot();
        });
    }

    @Override
    public void render(TileEntityKineticGenerators.Rotor te, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        int diameter = te.getRotorDiameter();
        if (diameter == 0) return;
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        Direction facing = te.getFacing();
        int dir = switch (facing) { case WEST -> 0; case NORTH -> 1; case EAST -> 2; default -> 3; };
        pose.mulPose(Axis.YP.rotationDegrees(dir * -90));
        float modifier = te instanceof TileEntityKineticGenerators.Water ? 0.1f : 360f / 500f;
        pose.mulPose(Axis.XP.rotationDegrees(te.getAngle(modifier)));
        pose.translate(-0.2, 0, 0);
        model(diameter).render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(te.getRotorRenderTexture())), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    @Override public boolean shouldRenderOffScreen(TileEntityKineticGenerators.Rotor te) { return true; }
    @Override public AABB getRenderBoundingBox(TileEntityKineticGenerators.Rotor te) { return new AABB(te.getBlockPos()).inflate(te.getRotorDiameter() / 2.0 + 1); }
}
