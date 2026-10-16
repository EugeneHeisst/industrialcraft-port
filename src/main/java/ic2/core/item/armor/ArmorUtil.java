package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.core.util.Keyboard;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

final class ArmorUtil {
    private ArmorUtil() {}

    static CompoundTag tag(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(); }
    static void save(ItemStack stack, CompoundTag tag) { stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag)); }

    /** ALT + mode switch toggles night vision; while on, 1 EU/tick, night vision in the dark and blindness in bright light. */
    static boolean nightvision(Level level, Player player, ItemStack stack, String key) {
        CompoundTag nbt = tag(stack);
        boolean active = nbt.getBoolean(key);
        byte toggleTimer = nbt.getByte("toggleTimer");
        boolean changed = false;
        if (Keyboard.isAltKeyDown(player) && Keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0) {
            toggleTimer = 10;
            active = !active;
            if (!level.isClientSide) { nbt.putBoolean(key, active); player.displayClientMessage(Component.literal(active ? "Nightvision enabled." : "Nightvision disabled."), false); }
            changed = true;
        }
        if (!level.isClientSide && toggleTimer > 0) { nbt.putByte("toggleTimer", --toggleTimer); changed = true; }
        if (changed && !level.isClientSide) save(stack, nbt);
        if (active && !level.isClientSide && ElectricItem.manager.use(stack, 1, player)) {
            int light = level.getMaxLocalRawBrightness(player.blockPosition());
            if (light > 8) { player.removeEffect(MobEffects.NIGHT_VISION); player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, true, true)); }
            else { player.removeEffect(MobEffects.BLINDNESS); player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, true)); }
            return true;
        }
        return false;
    }

    /** ALT + hud key cycles HUD mode 0-2 (stored like the original "HudMode"). */
    static void hudMode(Level level, Player player, ItemStack stack) {
        CompoundTag nbt = tag(stack);
        if (Keyboard.isAltKeyDown(player) && Keyboard.isHudModeKeyDown(player) && nbt.getByte("toggleTimer") == 0 && !level.isClientSide) {
            short mode = (short)((nbt.getShort("HudMode") + 1) % 3);
            nbt.putShort("HudMode", mode);
            nbt.putByte("toggleTimer", (byte)10);
            save(stack, nbt);
            player.displayClientMessage(Component.literal(switch (mode) { case 0 -> "HUD disabled."; case 1 -> "HUD (basic) enabled."; default -> "HUD (extended) enabled"; }), false);
        }
    }
}
