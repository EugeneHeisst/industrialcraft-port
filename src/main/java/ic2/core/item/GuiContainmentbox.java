package ic2.core.item;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiContainmentbox extends GuiIC2<ItemContainmentbox.Container> {
    public GuiContainmentbox(ItemContainmentbox.Container c, Inventory i, Component t) { super(c, i, t); }
    @Override public Component getName() { return Component.translatable("ic2.itemContainmentbox"); }
    @Override public ResourceLocation getResourceLocation() { return IC2.id("textures/gui/guicontainmentbox.png"); }
}
