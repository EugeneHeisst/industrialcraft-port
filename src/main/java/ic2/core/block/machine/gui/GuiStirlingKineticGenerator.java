package ic2.core.block.machine.gui;
import ic2.core.*;
import ic2.core.block.machine.container.ContainerStirlingKineticGenerator;
import ic2.core.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
public class GuiStirlingKineticGenerator extends GuiIC2<ContainerStirlingKineticGenerator>{
    public GuiStirlingKineticGenerator(ContainerStirlingKineticGenerator c,Inventory i,Component t){super(c,i,t,204);}
    public Component getName(){return Component.translatable("block.ic2.stirling_kinetic_generator");}
    public ResourceLocation getResourceLocation(){return IC2.id("textures/gui/guistirlingkineticgenerator.png");}
    protected void drawBackground(GuiGraphics g,float pt,int x,int y){for(int n=0;n<2;n++){int h=menu.fluids[n].getAmount()*44/2000;DrawUtil.drawFluid(g,menu.fluids[n],leftPos+(n==0?19:145),topPos+91-h,12,h);}}
    protected void drawForeground(GuiGraphics g,int x,int y){g.drawString(font,menu.base.kUBuffer+" / 2000 kU",45,36,0x404040,false);g.drawString(font,menu.base.heatbuffer+" / 1000 hU",45,49,0x404040,false);for(int n=0;n<2;n++){var f=menu.fluids[n];int px=n==0?16:142;GuiTooltipHelper.drawAreaTooltip(g,font,x,y,(f.isEmpty()?"":f.getHoverName().getString()+": ")+f.getAmount()+" / 2000 mB",px,45,px+17,92);}}
}
