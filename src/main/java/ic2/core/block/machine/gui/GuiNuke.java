package ic2.core.block.machine.gui;
import ic2.core.*;
import ic2.core.block.machine.container.ContainerNuke;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
public class GuiNuke extends GuiIC2<ContainerNuke>{
    public GuiNuke(ContainerNuke c,Inventory i,Component t){super(c,i,t,219);}
    public Component getName(){return Component.translatable("block.ic2.nuke");}
    public ResourceLocation getResourceLocation(){return IC2.id("textures/gui/guinuke.png");}
    @Override protected void renderLabels(GuiGraphics g,int x,int y){}
}
