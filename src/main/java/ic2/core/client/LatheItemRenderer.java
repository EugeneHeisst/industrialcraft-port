package ic2.core.client;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.item.ILatheItem;
import ic2.core.IC2;
import ic2.core.ref.Ic2Items;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.*;
import java.util.*;
/** Five individually cut sections, shared by GUI, hand and dropped-item rendering. */
@EventBusSubscriber(modid=IC2.MODID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public class LatheItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final Map<String,ModelPart> models=new HashMap<>();
    private LatheItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    @SubscribeEvent static void extensions(RegisterClientExtensionsEvent event){event.registerItem(new IClientItemExtensions(){private LatheItemRenderer renderer;@Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new LatheItemRenderer();return renderer;}},Ic2Items.IRON_TURNING_BLANK.get(),Ic2Items.WOOD_TURNING_BLANK.get());}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(!(stack.getItem() instanceof ILatheItem item))return;int[] state=item.getCurrentState(stack);if(state.length!=5)return;
        var model=models.computeIfAbsent(item.getWidth(stack)+":"+Arrays.toString(state),key->{var mesh=new MeshDefinition();for(int n=0;n<5;n++){float size=11f*state[n]/item.getWidth(stack);mesh.getRoot().addOrReplaceChild("section"+n,CubeListBuilder.create().texOffs(n*24,0).addBox(-6.5f+n*2.6f,-size/2,-1,2.6f,size,2),PartPose.ZERO);}return LayerDefinition.create(mesh,256,256).bakeRoot();});
        pose.pushPose();pose.translate(.5,.5,.5);model.render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(item.getTexture(stack))),light,overlay);pose.popPose();
    }
}
