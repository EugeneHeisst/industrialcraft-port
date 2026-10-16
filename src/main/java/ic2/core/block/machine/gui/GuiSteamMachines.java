package ic2.core.block.machine.gui;
import ic2.core.*;
import ic2.core.block.machine.container.*;
import ic2.core.network.ContainerEventPayload;
import ic2.core.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
public class GuiSteamMachines extends GuiIC2<ContainerFluidMachines<?>>{
    private final boolean boiler;
    public GuiSteamMachines(ContainerFluidMachines<?> c,Inventory i,Component t){super(c,i,t,c instanceof ContainerSteamMachines.Boiler?220:166);boiler=c instanceof ContainerSteamMachines.Boiler;}
    public Component getName(){return Component.translatable(boiler?"block.ic2.steam_generator":"block.ic2.steam_kinetic_generator");}
    public ResourceLocation getResourceLocation(){return IC2.id("textures/gui/"+(boiler?"guisteamgenerator":"guisteamkineticgenerator")+".png");}
    protected void drawBackground(GuiGraphics g,float pt,int x,int y){
        if(menu instanceof ContainerSteamMachines.Boiler b){
            int heat=Math.min(76,(int)(b.base.systemheat*76/500)),scale=Math.min(58,b.base.calcification*58/100000);drawTexturedModalRect(g,leftPos+13,topPos+146-heat,177,1,7,heat);drawTexturedModalRect(g,leftPos+155,topPos+119-scale,187,1,7,scale);
            int h=menu.fluids[0].getAmount()*47/10000;DrawUtil.drawFluid(g,menu.fluids[0],leftPos+10,topPos+202-h,75,h);
        }else{int h=menu.fluids[1].getAmount()*21/1000;DrawUtil.drawFluid(g,menu.fluids[1],leftPos+75,topPos+47-h,26,h);}
    }
    protected void drawForeground(GuiGraphics g,int x,int y){
        if(menu instanceof ContainerSteamMachines.Boiler b){
            g.drawString(font,b.base.pressurevalve+" bar",25,38,0x20ebee,false);g.drawString(font,b.base.outputmb+" mB/t",70,29,0x20ebee,false);
            var f=menu.fluids[1];if(!f.isEmpty()){var label=f.getHoverName();g.pose().pushPose();g.pose().translate(70,49,0);g.pose().scale(0.65f,0.65f,1);g.drawString(font,label,0,0,0x20ebee,false);g.pose().popPose();}
            g.drawString(font,b.base.heatinput+" hU/t",40,136,0x20ebee,false);g.drawString(font,b.base.inputmb+" mB/t",92,175,0x20ebee,false);
            GuiTooltipHelper.drawAreaTooltip(g,font,x,y,String.format(java.util.Locale.ROOT,"%.1f C",b.base.systemheat),13,70,20,146);
            GuiTooltipHelper.drawAreaTooltip(g,font,x,y,b.base.calcification+" / 100000",155,61,162,119);
            GuiTooltipHelper.drawAreaTooltip(g,font,x,y,menu.fluids[0].getAmount()+" / 10000 mB",10,155,85,202);
        }else if(menu instanceof ContainerSteamMachines.Turbine t){g.drawString(font,t.base.kUoutput+" kU/t",55,57,0x20ebee,false);g.drawString(font,menu.fluids[0].getAmount()+" / 21000 mB",40,75,0x20ebee,false);GuiTooltipHelper.drawAreaTooltip(g,font,x,y,menu.fluids[1].getAmount()+" / 1000 mB",75,26,101,47);}
    }
    public boolean mouseClicked(double mx,double my,int button){
        if(!boiler||button!=0)return super.mouseClicked(mx,my,button);
        int x=(int)mx-leftPos,y=(int)my-topPos;Integer event=null;
        for(int i=0;i<4;i++)if(x>=92+i*10&&x<=100+i*10){if(y>=162&&y<=170)event=new int[]{1000,100,10,1}[i];if(y>=186&&y<=194)event=new int[]{-1000,-100,-10,-1}[i];}
        for(int i=0;i<3;i++)if(x>=23+i*10&&x<=31+i*10){if(y>=25&&y<=33)event=new int[]{2100,2010,2001}[i];if(y>=49&&y<=57)event=new int[]{-2100,-2010,-2001}[i];}
        if(event!=null){PacketDistributor.sendToServer(new ContainerEventPayload(menu.containerId,event.toString()));return true;}return super.mouseClicked(mx,my,button);
    }
}
