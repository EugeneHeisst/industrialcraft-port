package ic2.core.block.machine.gui;
import ic2.core.*;
import ic2.core.block.machine.container.ContainerFluidMachines;
import ic2.core.network.ContainerEventPayload;
import ic2.core.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
public class GuiFluidMachines extends GuiIC2<ContainerFluidMachines<?>>{
    private final String kind,texture;
    public GuiFluidMachines(ContainerFluidMachines<?> c,Inventory i,Component t,String kind,String texture){super(c,i,t,184);this.kind=kind;this.texture=texture;}
    public Component getName(){return Component.translatable("block.ic2."+kind);}
    public ResourceLocation getResourceLocation(){return IC2.id("textures/gui/"+texture+".png");}
    private void tank(GuiGraphics g,int n,int x,int bottom,int width,int height){int h=menu.fluids[n].getAmount()*height/menu.base.getTankCapacity(n);DrawUtil.drawFluid(g,menu.fluids[n],leftPos+x,topPos+bottom-h,width,h);}
    protected void drawBackground(GuiGraphics g,float pt,int x,int y){
        if(menu instanceof ContainerFluidMachines.Distributor)tank(g,0,29,85,55,58);
        if(menu instanceof ContainerFluidMachines.Regulator r){tank(g,0,82,85,12,47);int h=(int)(14*r.base.energy/r.base.maxEnergy);if(h>0)drawTexturedModalRect(g,leftPos+9,topPos+52-h,176,14-h,14,h);}
        if(menu instanceof ContainerFluidMachines.Condenser c){tank(g,0,46,60,84,34);tank(g,1,46,89,84,18);drawTexturedModalRect(g,leftPos+46,topPos+62,0,184,c.base.progress*84/10000,9);int h=(int)(14*c.base.energy/c.base.maxEnergy);if(h>0)drawTexturedModalRect(g,leftPos+9,topPos+39-h,176,14-h,14,h);}
        if(menu instanceof ContainerFluidMachines.Solar s){if(s.base.getActive())drawTexturedModalRect(g,leftPos+36,topPos+26,0,184,97,29);tank(g,0,37,61,53,34);tank(g,1,115,98,17,34);}
    }
    protected void drawForeground(GuiGraphics g,int x,int y){
        if(menu instanceof ContainerFluidMachines.Regulator r){g.drawString(font,r.base.outputmb+" mB",102,57,0x20ebee,false);g.drawString(font,Component.translatable(r.base.mode==1?"ic2.generic.text.tick":"ic2.generic.text.sec"),145,57,0x20ebee,false);}
        if(menu instanceof ContainerFluidMachines.Distributor d){var label=Component.translatable(d.base.getActive()?"ic2.FluidDistributor.gui.mode.concentrate":"ic2.FluidDistributor.gui.mode.distribute");g.pose().pushPose();g.pose().translate(95,71,0);g.pose().scale(0.7f,0.7f,1);g.drawString(font,label,0,0,0x57c55a,false);g.pose().popPose();}
        int[][] areas=menu instanceof ContainerFluidMachines.Distributor?new int[][]{{29,27,84,85}}:menu instanceof ContainerFluidMachines.Regulator?new int[][]{{82,38,96,85}}:menu instanceof ContainerFluidMachines.Condenser?new int[][]{{46,26,130,60},{46,71,130,89}}:new int[][]{{37,27,90,61},{115,64,132,98}};
        for(int n=0;n<areas.length;n++){var f=menu.fluids[n];var a=areas[n];GuiTooltipHelper.drawAreaTooltip(g,font,x,y,(f.isEmpty()?"":f.getHoverName().getString()+": ")+f.getAmount()+" / "+menu.base.getTankCapacity(n)+" mB",a[0],a[1],a[2],a[3]);}
    }
    @Override public boolean mouseClicked(double mx,double my,int button){
        int x=(int)mx-leftPos,y=(int)my-topPos;String event=null;
        if(button==0&&menu instanceof ContainerFluidMachines.Distributor&&x>=117&&x<=135&&y>=58&&y<=66)event="toggle";
        if(button==0&&menu instanceof ContainerFluidMachines.Regulator){
            for(int i=0;i<4;i++)if(x>=102+i*10&&x<=110+i*10){if(y>=44&&y<=52)event=""+new int[]{1000,100,10,1}[i];if(y>=68&&y<=76)event=""+new int[]{-1000,-100,-10,-1}[i];}
            if(x>=151&&x<=161){if(y>=44&&y<=52)event="1001";if(y>=68&&y<=76)event="1002";}
        }
        if(event!=null){PacketDistributor.sendToServer(new ContainerEventPayload(menu.containerId,event));return true;}return super.mouseClicked(mx,my,button);
    }
}
