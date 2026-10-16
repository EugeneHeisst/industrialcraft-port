package ic2.core.block.machine.gui;
import ic2.api.item.ILatheItem;
import ic2.core.*;
import ic2.core.block.machine.container.ContainerLathe;
import ic2.core.network.ContainerEventPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
public class GuiLathe extends GuiIC2<ContainerLathe>{
    public GuiLathe(ContainerLathe c,Inventory i,Component t){super(c,i,t);}
    public Component getName(){return Component.translatable("block.ic2.lathe");}
    public ResourceLocation getResourceLocation(){return IC2.id("textures/gui/guilathe.png");}
    protected void init(){super.init();for(int n=0;n<5;n++){final int position=n;addRenderableWidget(Button.builder(Component.literal("▼"),b->PacketDistributor.sendToServer(new ContainerEventPayload(menu.containerId,""+position))).bounds(leftPos+40+n*24,topPos+15,24,20).build());}}
    protected void drawForeground(GuiGraphics g,int x,int y){var blank=menu.base.latheSlot.get();if(blank.getItem() instanceof ILatheItem item){int[] state=item.getCurrentState(blank);for(int n=0;n<Math.min(5,state.length);n++)g.drawString(font,state[n]+"/"+item.getWidth(blank),40+n*24,37,0x404040,false);}}
    protected void drawBackground(GuiGraphics g,float pt,int x,int y){var blank=menu.base.latheSlot.get();if(blank.getItem() instanceof ILatheItem item){int[] state=item.getCurrentState(blank);for(int n=0;n<Math.min(5,state.length);n++){int h=Math.round(32f*state[n]/item.getWidth(blank)),offset=(32-h)/2;g.blit(item.getTexture(blank),leftPos+40+n*24,topPos+45+offset,n*24,offset,24,h,256,256);}}}
}
