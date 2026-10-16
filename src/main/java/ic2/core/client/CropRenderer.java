package ic2.core.client;

import com.mojang.blaze3d.vertex.*;
import ic2.core.crop.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.TextureAtlas;

/** Addon crops have dynamic textures; builtin plants use baked multipart models. */
public final class CropRenderer implements BlockEntityRenderer<TileEntityCrop> {
    public CropRenderer(BlockEntityRendererProvider.Context ctx){}
    @Override public void render(TileEntityCrop crop,float partial,PoseStack poses,MultiBufferSource source,int light,int overlay){
        if(crop.getCrop()==null || crop.getBlockState().getValue(BlockCrop.PLANT)!=BlockCrop.Plant.EMPTY)return;
        var sprite=Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(crop.getCrop().texture(crop.size));
        var out=source.getBuffer(RenderType.cutout());var pose=poses.last();
        float[][][] planes={{{0.146f,0,0.146f},{0.146f,1,0.146f},{0.854f,1,0.854f},{0.854f,0,0.854f}},{{0.146f,0,0.854f},{0.146f,1,0.854f},{0.854f,1,0.146f},{0.854f,0,0.146f}}};
        for(var plane:planes)for(int side=0;side<2;side++)for(int v=0;v<4;v++){
            int i=side==0?v:3-v;var p=plane[i];
            out.addVertex(pose,p[0],p[1],p[2]).setColor(255,255,255,255).setUv(i<2?sprite.getU0():sprite.getU1(),i==0||i==3?sprite.getV1():sprite.getV0()).setOverlay(overlay).setLight(light).setNormal(pose,0,1,0);
        }
    }
}
