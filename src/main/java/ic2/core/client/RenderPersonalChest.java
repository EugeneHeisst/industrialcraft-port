package ic2.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ic2.core.IC2;
import ic2.core.block.personal.TileEntityPersonalChest;
import ic2.core.ref.Ic2Items;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** The safe of ModelPersonalChest: five walls and a door swinging open while someone uses it; also drawn for the item. */
@EventBusSubscriber(modid = IC2.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RenderPersonalChest implements BlockEntityRenderer<TileEntityPersonalChest> {
    private static final ResourceLocation TEXTURE = IC2.id("textures/models/newsafe.png");
    private static ModelPart model;

    public RenderPersonalChest(BlockEntityRendererProvider.Context ctx) {}

    private static ModelPart model() {
        if (model != null) return model;
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        float half = (float)Math.PI / 2;
        root.addOrReplaceChild("wallR", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0, 0, 0, 14, 16, 1), PartPose.offsetAndRotation(1, 0, 15, 0, half, 0));
        root.addOrReplaceChild("wallL", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0, 0, 0, 14, 16, 1), PartPose.offsetAndRotation(15, 0, 1, 0, -half, 0));
        root.addOrReplaceChild("wallB", CubeListBuilder.create().texOffs(1, 1).mirror().addBox(1, 1, 0, 12, 14, 1), PartPose.offsetAndRotation(15, 0, 15, 0, (float)Math.PI, 0));
        root.addOrReplaceChild("wallU", CubeListBuilder.create().texOffs(1, 17).mirror().addBox(1, 0, 0, 12, 14, 1), PartPose.offsetAndRotation(1, 0, 15, -half, 0, 0));
        root.addOrReplaceChild("wallD", CubeListBuilder.create().texOffs(1, 17).mirror().addBox(1, 0, 0, 12, 14, 1), PartPose.offsetAndRotation(15, 15, 1, -half, (float)Math.PI, 0));
        root.addOrReplaceChild("door", CubeListBuilder.create().texOffs(30, 0).mirror().addBox(0, 0, 0, 12, 14, 1), PartPose.offset(2, 1, 2));
        return model = LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    static void render(Direction facing, float lidAngle, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(0, 1, 1);
        pose.scale(1, -1, -1);
        pose.translate(0.5, 0.5, 0.5);
        int angle = switch (facing) { case NORTH -> 180; case WEST -> 90; case EAST -> -90; default -> 0; };
        pose.mulPose(Axis.YP.rotationDegrees(angle));
        pose.translate(-0.5, -0.5, -0.5);
        float open = 1 - lidAngle;
        open = 1 - open * open * open;
        ModelPart m = model();
        m.getChild("door").yRot = open * (float)Math.PI / 2;
        m.render(pose, buffers.getBuffer(RenderType.entityCutout(TEXTURE)), light, overlay);
        pose.popPose();
    }

    @Override
    public void render(TileEntityPersonalChest te, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        render(te.getFacing(), te.prevLidAngle + (te.lidAngle - te.prevLidAngle) * partialTick, pose, buffers, light, overlay);
    }

    @SubscribeEvent
    static void extensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new BlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()) {
                    @Override public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buffers, int light, int overlay) { render(Direction.SOUTH, 0, pose, buffers, light, overlay); }
                };
                return renderer;
            }
        }, Ic2Items.REGISTRY.getEntries().stream().filter(i -> i.getId().getPath().equals("personal_chest")).findFirst().orElseThrow().get());
    }
}
