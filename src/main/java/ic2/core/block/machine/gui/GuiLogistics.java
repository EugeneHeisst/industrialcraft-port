package ic2.core.block.machine.gui;

import ic2.core.*;
import ic2.core.block.machine.container.ContainerLogistics;
import ic2.core.network.ContainerEventPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public final class GuiLogistics {
    public static class Buffer extends GuiIC2<ContainerLogistics.Buffer>{
        public Buffer(ContainerLogistics.Buffer c,Inventory i,Component t){super(c,i,t,232);}
        public Component getName(){return Component.translatable("block.ic2.item_buffer");}
        public ResourceLocation getResourceLocation(){return IC2.id("textures/gui/guiitembuffer.png");}
    }
    public static class Sorter extends GuiIC2<ContainerLogistics.Sorter>{
        public Sorter(ContainerLogistics.Sorter c,Inventory i,Component t){super(c,i,t,212,243);}
        public Component getName(){return Component.translatable("block.ic2.sorting_machine");}
        public ResourceLocation getResourceLocation(){return IC2.id("textures/gui/guisortingmachine.png");}
        protected void drawBackground(GuiGraphics g,float pt,int mx,int my){
            for(var d:Direction.values()){
                int y=18+d.ordinal()*20;
                boolean connected=menu.base.getLevel()!=null&&menu.base.getLevel().getBlockEntity(menu.base.getBlockPos().relative(d))!=null;
                drawTexturedModalRect(g,leftPos+60,topPos+y,212,connected?15:33,18,18);
                drawTexturedModalRect(g,leftPos+43,topPos+y,230,menu.base.defaultRoute==d.ordinal()?33:15,18,18);
            }
            int charge=(int)Math.min(14,14*menu.base.energy/menu.base.maxEnergy);if(charge>0)drawTexturedModalRect(g,leftPos+171,topPos+233-charge,212,14-charge,14,charge);
        }
        protected void drawForeground(GuiGraphics g,int x,int y){for(var d:Direction.values())ic2.core.util.GuiTooltipHelper.drawAreaTooltip(g,font,x,y,Component.translatable(menu.base.defaultRoute==d.ordinal()?"ic2.SortingMachine.default":"ic2.SortingMachine.whitelist").getString(),43,18+d.ordinal()*20,61,36+d.ordinal()*20);}
        @Override public boolean mouseClicked(double x,double y,int button){
            if(button==0&&x>=leftPos+43&&x<leftPos+61)for(var d:Direction.values())if(y>=topPos+18+d.ordinal()*20&&y<topPos+36+d.ordinal()*20){PacketDistributor.sendToServer(new ContainerEventPayload(menu.containerId,""+d.ordinal()));return true;}
            return super.mouseClicked(x,y,button);
        }
    }
}
