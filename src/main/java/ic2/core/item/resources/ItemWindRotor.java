package ic2.core.item.resources;

import ic2.api.item.IKineticRotor;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Kinetic generator rotor; the wooden one only fits wind gearboxes. Durability counts in rotor operations. */
public class ItemWindRotor extends Item implements IKineticRotor {
    private final int radius, minWindStrength, maxWindStrength;
    private final float efficiency;
    private final ResourceLocation renderTexture;
    private final boolean water;

    public ItemWindRotor(Properties properties, int radius, int durability, float efficiency, int minWind, int maxWind, ResourceLocation renderTexture, boolean water) {
        super(properties.durability(durability));
        this.radius = radius;
        this.efficiency = efficiency;
        this.minWindStrength = minWind;
        this.maxWindStrength = maxWind;
        this.renderTexture = renderTexture;
        this.water = water;
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) { list.add(Component.translatable("ic2.itemrotor.wind.info", minWindStrength, maxWindStrength).withStyle(ChatFormatting.GRAY)); }
    @Override public int getDiameter(ItemStack stack) { return radius; }
    @Override public ResourceLocation getRotorRenderTexture(ItemStack stack) { return renderTexture; }
    @Override public float getEfficiency(ItemStack stack) { return efficiency; }
    @Override public int getMinWindStrength(ItemStack stack) { return minWindStrength; }
    @Override public int getMaxWindStrength(ItemStack stack) { return maxWindStrength; }
    @Override public boolean isAcceptedType(ItemStack stack, GearboxType type) { return type == GearboxType.WIND || water; }
}
