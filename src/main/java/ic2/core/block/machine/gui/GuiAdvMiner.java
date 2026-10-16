package ic2.core.block.machine.gui;
import ic2.core.*;
import ic2.core.block.machine.container.ContainerAdvMiner;
import ic2.core.network.ContainerEventPayload;
import ic2.core.util.GuiTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
public class GuiAdvMiner extends GuiIC2<ContainerAdvMiner>{
    public GuiAdvMiner(ContainerAdvMiner c,Inventory i,Component t){super(c,i,t,203);}
    public Component getName(){return Component.translatable("block.ic2.adv_miner");}
    public ResourceLocation getResourceLocation(){return IC2.id("textures/gui/guiadvminer.png");}
    protected void drawBackground(GuiGraphics g,float pt,int x,int y){int h=(int)(14*menu.base.getChargeLevel());if(h>0)drawTexturedModalRect(g,leftPos+9,topPos+67-h,176,14-h,14,h);}
    protected void drawForeground(GuiGraphics g,int x,int y){g.drawString(font,Component.translatable("ic2.AdvMiner.gui.info.minelevel",menu.base.xcounter,menu.base.zcounter,menu.base.minelayer),28,105,0x20eb3e,false);var label=Component.translatable("ic2.AdvMiner.gui.mode."+(menu.base.blacklist?"blacklist":"whitelist"));float scale=Math.min(1,80f/font.width(label));g.pose().pushPose();g.pose().translate(40,31,0);g.pose().scale(scale,scale,1);g.drawString(font,label,0,0,0x20eb3e,false);g.pose().popPose();GuiTooltipHelper.drawAreaTooltip(g,font,x,y,Component.translatable("ic2.AdvMiner.gui.switch.reset").getString(),134,102,167,114);GuiTooltipHelper.drawAreaTooltip(g,font,x,y,Component.translatable("ic2.AdvMiner.gui.switch.mode").getString(),124,28,139,40);GuiTooltipHelper.drawAreaTooltip(g,font,x,y,Component.translatable("ic2.AdvMiner.gui.switch.silktouch",menu.base.silktouch).getString(),129,45,149,59);}
    @Override public boolean mouseClicked(double mx,double my,int b){int x=(int)mx-leftPos,y=(int)my-topPos,n=x>=134&&x<=167&&y>=102&&y<=114?0:x>=124&&x<=139&&y>=28&&y<=40?1:x>=129&&x<=149&&y>=45&&y<=59?2:-1;if(n>=0&&b==0){PacketDistributor.sendToServer(new ContainerEventPayload(menu.containerId,""+n));return true;}return super.mouseClicked(mx,my,b);}
}
