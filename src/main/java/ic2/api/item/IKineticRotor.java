package ic2.api.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IKineticRotor {
    enum GearboxType { WATER, WIND }
    int getDiameter(ItemStack stack);
    ResourceLocation getRotorRenderTexture(ItemStack stack);
    float getEfficiency(ItemStack stack);
    int getMinWindStrength(ItemStack stack);
    int getMaxWindStrength(ItemStack stack);
    boolean isAcceptedType(ItemStack stack, GearboxType type);
}
