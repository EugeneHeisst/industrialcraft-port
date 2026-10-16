package ic2.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.misc.BlockFoamWall;
import ic2.core.block.misc.TileEntityWall;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** Foam wall with obscurator textures (its block model is hidden while retextured). */
public class RenderFoamWall implements BlockEntityRenderer<TileEntityWall> {
    public RenderFoamWall(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(TileEntityWall te, float pt, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var state = te.getBlockState();
        if (te.getLevel() == null || !state.hasProperty(BlockFoamWall.RETEXTURED) || !state.getValue(BlockFoamWall.RETEXTURED)) return;
        RetextureRender.renderCube(te.retexture, state.setValue(BlockFoamWall.RETEXTURED, false), te.getLevel(), te.getBlockPos(), pose, buffers);
    }
}
