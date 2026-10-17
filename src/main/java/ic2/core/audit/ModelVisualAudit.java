package ic2.core.audit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ic2.core.IC2;
import ic2.core.block.misc.BlockScaffold;
import ic2.core.item.boat.EntityIC2Boat;
import ic2.core.item.boat.RenderIC2Boat;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Entities;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Vector3f;

/** Native renderer vertices, stitched scaffold faces, and the machine faces visible in the GUI. */
public final class ModelVisualAudit {
    private static int passed, failed;
    private ModelVisualAudit() {}

    private static void check(String name, boolean ok, Object detail) {
        if (ok) passed++; else failed++;
        IC2.LOG.info("[model-visual-audit] {} {}: {}", ok ? "PASS" : "FAIL", name, detail);
    }

    public static void run(Minecraft mc) {
        passed = failed = 0;
        boats(mc);
        scaffold(mc, Ic2Blocks.SCAFFOLD.get().defaultBlockState().setValue(BlockScaffold.STRENGTH, 2), "scaffold");
        scaffold(mc, Ic2Blocks.SCAFFOLD.get().defaultBlockState().setValue(BlockScaffold.STRENGTH, 5), "scaffold_reinforced");
        scaffold(mc, Ic2Blocks.IRON_SCAFFOLD.get().defaultBlockState().setValue(BlockScaffold.STRENGTH, 5), "iron_scaffold");
        scaffold(mc, Ic2Blocks.IRON_SCAFFOLD.get().defaultBlockState().setValue(BlockScaffold.STRENGTH, 12), "iron_scaffold_reinforced");
        for (String name : List.of("macerator", "extractor", "compressor", "electric_furnace", "generator")) machine(mc, name);
        IC2.LOG.info("[model-visual-audit] DONE {}/{} passed, {} failed", passed, passed + failed, failed);
    }

    private static void boats(Minecraft mc) {
        List<Supplier<EntityType<EntityIC2Boat>>> types = List.of(Ic2Entities.CARBON_BOAT, Ic2Entities.RUBBER_BOAT, Ic2Entities.ELECTRIC_BOAT);
        for (var type : types) for (int yaw : new int[]{0, 90, 180, 270}) {
            String name = BuiltInRegistries.ENTITY_TYPE.getKey(type.get()).getPath() + " yaw=" + yaw;
            try {
                EntityIC2Boat boat = type.get().create(mc.level);
                boat.setYRot(yaw);
                var renderer = mc.getEntityRenderDispatcher().getRenderer(boat);
                HullVertices vertices = new HullVertices(Vec3.directionFromRotation(0, yaw));
                renderer.render(boat, yaw, 0, new PoseStack(), ignored -> vertices, 0xF000F0);
                // At all cardinal yaws, the hull's longer axis must follow native Boat's movement/rider yaw.
                // The old renderer emits the same vertices with the longer axis perpendicular to it.
                check(name + " hull follows rider/steering axis", renderer instanceof RenderIC2Boat && vertices.count >= 120
                    && vertices.forwardSpan() > vertices.sideSpan() + 0.1,
                    "vertices=" + vertices.count + " forward=" + vertices.forwardSpan() + " side=" + vertices.sideSpan());
            } catch (RuntimeException e) { check(name, false, e); }
        }
    }

    private static void scaffold(Minecraft mc, BlockState state, String variant) {
        BakedModel model = mc.getBlockRenderer().getBlockModel(state);
        for (Direction face : Direction.values()) {
            String name = variant + " " + face.getName();
            try {
                List<BakedQuad> quads = model.getQuads(state, face, RandomSource.create(0), ModelData.EMPTY, RenderType.cutout());
                String expected = "ic2:block/" + variant + "/" + face.getName();
                boolean ok = quads.size() == 1 && quads.getFirst().getDirection() == face;
                for (var quad : quads) {
                    var sprite = quad.getSprite().contents();
                    ok &= sprite.width() == 16 && sprite.height() == 16 && sprite.name().toString().equals(expected);
                }
                check(name + " has its own 16x16 stitched face", ok, quads.stream().map(q -> {
                    var sprite = q.getSprite().contents();
                    return sprite.name() + " " + sprite.width() + "x" + sprite.height();
                }).toList());
            } catch (RuntimeException e) { check(name, false, e); }
        }
    }

    private static void machine(Minecraft mc, String name) {
        try {
            var item = BuiltInRegistries.ITEM.get(IC2.id(name));
            BakedModel model = mc.getItemRenderer().getModel(new ItemStack(item), mc.level, mc.player, 0);
            var front = model.getQuads(null, Direction.SOUTH, RandomSource.create(0), ModelData.EMPTY, null);
            var back = model.getQuads(null, Direction.NORTH, RandomSource.create(0), ModelData.EMPTY, null);
            PoseStack pose = new PoseStack();
            model.getTransforms().getTransform(ItemDisplayContext.GUI).apply(false, pose);
            float frontZ = front.isEmpty() ? Float.NaN : transformedNormalZ(front.getFirst(), pose);
            float backZ = back.isEmpty() ? Float.NaN : transformedNormalZ(back.getFirst(), pose);
            check(name + " GUI shows the machine front", front.size() == 1 && back.size() == 1
                && front.getFirst().getSprite().contents().name().equals(IC2.id("block/" + name + "_3"))
                && frontZ > 0 && backZ < 0,
                "frontZ=" + frontZ + " backZ=" + backZ + " front=" + front.stream().map(q -> q.getSprite().contents().name()).toList());
        } catch (RuntimeException e) { check(name + " GUI shows the machine front", false, e); }
    }

    private static float transformedNormalZ(BakedQuad quad, PoseStack pose) {
        var normal = quad.getDirection().getNormal();
        return pose.last().transformNormal(normal.getX(), normal.getY(), normal.getZ(), new Vector3f()).z();
    }

    private static final class HullVertices implements VertexConsumer {
        private final Vec3 forward;
        private int count;
        private double minForward = Double.POSITIVE_INFINITY, maxForward = Double.NEGATIVE_INFINITY;
        private double minSide = Double.POSITIVE_INFINITY, maxSide = Double.NEGATIVE_INFINITY;
        private HullVertices(Vec3 forward) { this.forward = forward; }
        @Override public VertexConsumer addVertex(float x, float y, float z) {
            double f = x * forward.x + z * forward.z, s = x * forward.z - z * forward.x;
            minForward = Math.min(minForward, f); maxForward = Math.max(maxForward, f);
            minSide = Math.min(minSide, s); maxSide = Math.max(maxSide, s);
            count++;
            return this;
        }
        private double forwardSpan() { return maxForward - minForward; }
        private double sideSpan() { return maxSide - minSide; }
        @Override public VertexConsumer setColor(int red, int green, int blue, int alpha) { return this; }
        @Override public VertexConsumer setUv(float u, float v) { return this; }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
    }
}
