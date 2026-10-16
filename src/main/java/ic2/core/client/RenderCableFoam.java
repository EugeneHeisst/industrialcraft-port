package ic2.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.misc.BlockFoamWall;
import ic2.core.block.wiring.TileEntityCable;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** Draws the soft foam or the coloured hardened wall cube around a foamed cable (the cable model stays inside). */
public class RenderCableFoam implements BlockEntityRenderer<TileEntityCable> {
    public RenderCableFoam(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(TileEntityCable te, float pt, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (te.foamed == 0 || te.getLevel() == null) return;
        var state = te.foamed == 1 ? Ic2Blocks.CONSTRUCTION_FOAM.get().defaultBlockState() : Ic2Blocks.CONSTRUCTION_FOAM_WALL.get().defaultBlockState().setValue(BlockFoamWall.COLOR, te.foamColor & 15);
        int l = te.foamed == 2 ? LevelRenderer.getLightColor(te.getLevel(), te.getBlockPos().above()) : light;
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, pose, buffers, Math.max(l, light), overlay);
    }
}
