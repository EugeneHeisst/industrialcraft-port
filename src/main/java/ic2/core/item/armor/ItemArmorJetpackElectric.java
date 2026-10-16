package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.core.ref.Ic2ArmorMaterials;
import ic2.core.util.Keyboard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Electric jetpack: 30000 EU, jump to fly, mode switch + jump toggles hover; flight ceiling at height / 1.28. */
public class ItemArmorJetpackElectric extends ItemArmorElectric {
    public ItemArmorJetpackElectric(Properties properties) { super(Ic2ArmorMaterials.JETPACK, Type.CHESTPLATE, properties, 30000, 60, 1); }

    @Override public double getDamageAbsorptionRatio() { return 0; }
    @Override public int getEnergyPerDamage() { return 0; }

    @Override
    public void onArmorTick(Level level, Player player, ItemStack stack) {
        CompoundTag nbt = ArmorUtil.tag(stack);
        boolean hoverMode = nbt.getBoolean("hoverMode");
        byte toggleTimer = nbt.getByte("toggleTimer");
        if (Keyboard.isJumpKeyDown(player) && Keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0) {
            toggleTimer = 10;
            hoverMode = !hoverMode;
            if (!level.isClientSide) { nbt.putBoolean("hoverMode", hoverMode); player.displayClientMessage(Component.literal(hoverMode ? "Hover Mode enabled." : "Hover Mode disabled."), false); }
        }
        if (Keyboard.isJumpKeyDown(player) || hoverMode && player.getDeltaMovement().y < -0.03) useJetpack(player, hoverMode);
        if (!level.isClientSide) { if (toggleTimer > 0) nbt.putByte("toggleTimer", --toggleTimer); ArmorUtil.save(stack, nbt); }
    }

    public boolean useJetpack(Player player, boolean hoverMode) {
        ItemStack jetpack = player.getItemBySlot(EquipmentSlot.CHEST);
        double charge = ElectricItem.manager.getCharge(jetpack);
        if (charge <= 0) return false;
        float power = 0.7f, dropPercentage = 0.05f;
        if (charge / getMaxCharge(jetpack) <= dropPercentage) power *= (float)(charge / (getMaxCharge(jetpack) * dropPercentage));
        if (Keyboard.isForwardKeyDown(player)) {
            float forwardpower = power * ((hoverMode ? 1f : 0.15f) + 0.15f) * 2;
            if (forwardpower > 0) player.moveRelative(0.02f, new Vec3(0, 0, 0.4f * forwardpower));
        }
        int maxFlightHeight = (int)(player.level().getMaxBuildHeight() / 1.28f);
        double y = player.getY();
        if (y > maxFlightHeight - 25) { if (y > maxFlightHeight) y = maxFlightHeight; power *= (float)((maxFlightHeight - y) / 25); }
        Vec3 m = player.getDeltaMovement();
        double prev = m.y, my = Math.min(m.y + power * 0.2f, 0.6f);
        if (hoverMode) {
            float maxHoverY = Keyboard.isJumpKeyDown(player) ? 0.1f : player.isShiftKeyDown() ? -0.1f : 0;
            if (my > maxHoverY) { my = maxHoverY; if (prev > my) my = prev; }
        }
        player.setDeltaMovement(m.x, my, m.z);
        if (!player.level().isClientSide) ElectricItem.manager.discharge(jetpack, hoverMode ? 1 : 2, Integer.MAX_VALUE, true, false, false);
        player.fallDistance = 0;
        return true;
    }
}
