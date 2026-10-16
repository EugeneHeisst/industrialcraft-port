package ic2.core.block.machine.gui;
import ic2.core.*;
import ic2.core.block.TileEntityHeatSourceInventory;
import ic2.core.block.machine.container.*;
import ic2.core.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
public class GuiThermalFluids extends GuiIC2<ContainerFluidMachines<?>>{
    private final String kind,texture;
    public GuiThermalFluids(ContainerFluidMachines<?> c,Inventory i,Component t,String kind,String texture){super(c,i,t,c instanceof ContainerThermalFluids.Generator?166:c instanceof ContainerThermalFluids.Exchanger?204:184);this.kind=kind;this.texture=texture;}
    public Component getName(){return Component.translatable("block.ic2."+kind);}
    public ResourceLocation getResourceLocation(){return IC2.id("textures/gui/"+texture+".png");}
    private void tank(GuiGraphics g,int n,int x,int bottom,int w,int height){int h=menu.fluids[n].getAmount()*height/menu.base.getTankCapacity(n);DrawUtil.drawFluid(g,menu.fluids[n],leftPos+x,topPos+bottom-h,w,h);}
    protected void drawBackground(GuiGraphics g,float pt,int x,int y){
        if(menu instanceof ContainerThermalFluids.Generator)tank(g,0,74,71,12,47);
        if(menu instanceof ContainerThermalFluids.Exchanger){tank(g,0,19,91,12,44);tank(g,1,145,91,12,44);}
        if(menu instanceof ContainerThermalFluids.Fermenter f){tank(g,0,38,79,48,32);tank(g,1,129,73,12,47);drawTexturedModalRect(g,leftPos+37,topPos+87,176,0,Math.min(42,42*f.base.progress/500),8);drawTexturedModalRect(g,leftPos+41,topPos+40,176,9,Math.min(42,42*f.base.heatbuffer/8000),4);}
    }
    protected void drawForeground(GuiGraphics g,int x,int y){
        if(menu.base instanceof TileEntityHeatSourceInventory h){g.drawString(font,h.transmitHeat+" / "+h.maxHeatEmitpeerTick+" hU/t",menu instanceof ContainerThermalFluids.Generator?94:49,menu instanceof ContainerThermalFluids.Generator?33:30,0x57c55a,false);}
        if(menu instanceof ContainerThermalFluids.Fermenter f){GuiTooltipHelper.drawAreaTooltip(g,font,x,y,Component.translatable("ic2.Fermenter.gui.info.conversion").getString()+" "+(f.base.heatbuffer*100/8000)+"%",41,40,82,44);GuiTooltipHelper.drawAreaTooltip(g,font,x,y,Component.translatable("ic2.Fermenter.gui.info.waste").getString()+" "+f.base.progress+" / 500",37,87,78,95);}
        int[][] areas=menu instanceof ContainerThermalFluids.Generator?new int[][]{{74,24,86,71}}:menu instanceof ContainerThermalFluids.Exchanger?new int[][]{{19,47,31,91},{145,47,157,91}}:new int[][]{{38,47,86,79},{129,26,141,73}};
        for(int n=0;n<areas.length;n++){var f=menu.fluids[n];var a=areas[n];GuiTooltipHelper.drawAreaTooltip(g,font,x,y,(f.isEmpty()?"":f.getHoverName().getString()+": ")+f.getAmount()+" / "+menu.base.getTankCapacity(n)+" mB",a[0],a[1],a[2],a[3]);}
    }
}
