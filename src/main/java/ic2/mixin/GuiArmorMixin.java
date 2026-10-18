package ic2.mixin;

import ic2.api.item.ElectricItem;
import ic2.core.item.armor.ItemArmorElectric;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Original getArmorDisplay: HUD only; the special armor handler supplies actual protection. */
@Mixin(Gui.class)
public abstract class GuiArmorMixin {
    @Redirect(method = {"renderArmor", "renderArmorLevel"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getArmorValue()I"))
    private static int ic2$armorDisplay(Player player) {
        int armor = player.getArmorValue();
        for (ItemStack stack : player.getArmorSlots()) {
            if (stack.getItem() instanceof ItemArmorElectric electric && ElectricItem.manager.getCharge(stack) >= electric.getEnergyPerDamage())
                armor += (int)Math.round(20 * electric.getBaseAbsorptionRatio() * electric.getDamageAbsorptionRatio());
        }
        return armor;
    }
}
