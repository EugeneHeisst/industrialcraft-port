package ic2.core.crop;
import ic2.core.*;
import ic2.core.item.ItemCropAnalyzer;
import ic2.core.ref.Ic2Components;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
public final class GuiCrops {
    public static class Matron extends GuiIC2<ContainerCrops.Matron>{
        public Matron(ContainerCrops.Matron c,Inventory i,Component t){super(c,i,t,191);}
        public Component getName(){return Component.translatable("block.ic2.cropmatron");}
        public ResourceLocation getResourceLocation(){return IC2.id("textures/gui/guicropmatron.png");}
        protected void drawForeground(GuiGraphics g,int x,int y){ic2.core.util.GuiTooltipHelper.drawAreaTooltip(g,font,x,y,menu.water+" / 2000 mB",64,74,111,100);}
        protected void drawBackground(GuiGraphics g,float pt,int x,int y){
            int h=Math.max(0,Math.min(24,menu.water*24/2000));
            if(h>0)ic2.core.util.DrawUtil.drawFluid(g,new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,menu.water),leftPos+64,topPos+100-h,47,h);
            int charge=(int)Math.min(14,menu.base.energy*14/menu.base.maxEnergy);if(charge>0)drawTexturedModalRect(g,leftPos+153,topPos+95-charge,176,14-charge,14,charge);
        }
    }
    public static class Harvester extends GuiIC2<ContainerCrops.Harvester>{
        public Harvester(ContainerCrops.Harvester c,Inventory i,Component t){super(c,i,t,191);}
        public Component getName(){return Component.translatable("block.ic2.crop_harvester");}
        public ResourceLocation getResourceLocation(){return IC2.id("textures/gui/guicrophavester.png");}
    }
    public static class Analyzer extends GuiIC2<ItemCropAnalyzer.Container>{
        public Analyzer(ItemCropAnalyzer.Container c,Inventory i,Component t){super(c,i,t,223);}
        public Component getName(){return Component.translatable("item.ic2.crop_analyzer");}
        public ResourceLocation getResourceLocation(){return IC2.id("textures/gui/guicropnalyzer.png");}
        protected void renderLabels(GuiGraphics g,int x,int y){
            var seed=menu.base.getItem(1);var n=seed.get(Ic2Components.CROP_DATA.get());var card=ic2.api.crops.Crops.instance.getCropCard(seed);
            if(card==null||n==null)return;
            int scan=n.getByte("scan");
            if(scan>0)g.drawString(font,Component.translatable("ic2.crop."+card.name()),8,37,0xffffff,false);
            if(scan>=2){g.drawString(font,Component.translatable("ic2.crop.tier",card.tier()),8,50,0xffffff,false);g.drawString(font,card.discoveredBy(),8,86,0xffffff,false);}
            if(scan>=3){g.drawString(font,card.desc(0),8,109,0xffffff,false);g.drawString(font,card.desc(1),8,122,0xffffff,false);}
            if(scan>=4){
                g.drawString(font,Component.translatable("ic2.crop.growth"),118,37,0xadff2f,false);g.drawString(font,""+n.getByte("growth"),118,50,0xadff2f,false);
                g.drawString(font,Component.translatable("ic2.crop.gain"),118,73,0xeec900,false);g.drawString(font,""+n.getByte("gain"),118,86,0xeec900,false);
                g.drawString(font,Component.translatable("ic2.crop.resistance"),118,109,0x00ced1,false);g.drawString(font,""+n.getByte("resistance"),118,122,0x00ced1,false);
            }
        }
    }
}
