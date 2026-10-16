package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.core.ref.Ic2ArmorMaterials;
import ic2.core.util.Keyboard;
import java.util.ArrayList;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 10 MEU, 12000 EU/t, tier 4, 20000 EU per damage. Helmet: air, food from filled tin cans, cures poison/wither, night vision.
 * Chest: jetpack (boost + mode switch) with hover (jump + mode switch), fire immunity. Legs: sprint speed. Boots: super jump (boost + jump).
 */
public class ItemArmorQuantumSuit extends ItemArmorElectric {
    private static final Map<Holder<MobEffect>, Integer> potionRemovalCost = Map.of(MobEffects.POISON, 10000, MobEffects.WITHER, 25000);
    private float jumpCharge;

    public ItemArmorQuantumSuit(Type type, Properties properties) { super(Ic2ArmorMaterials.QUANTUM, type, properties.rarity(Rarity.RARE), 1e7, 12000, 4); }

    @Override public double getDamageAbsorptionRatio() { return armorType() == 1 ? 1.1 : 1.0; }
    @Override public int getEnergyPerDamage() { return 20000; }

    @Override
    public void onArmorTick(Level level, Player player, ItemStack stack) {
        CompoundTag nbt = ArmorUtil.tag(stack);
        boolean ret = false;
        switch (armorType()) {
            case 0 -> {
                if (level.isClientSide) break;
                int air = player.getAirSupply();
                if (ElectricItem.manager.canUse(stack, 1000) && air < 100) { player.setAirSupply(air + 200); ElectricItem.manager.use(stack, 1000, null); ret = true; }
                if (ElectricItem.manager.canUse(stack, 1000) && player.getFoodData().needsFood()) {
                    for (int i = 0; i < player.getInventory().items.size(); i++) {
                        ItemStack can = player.getInventory().items.get(i);
                        if (can.is(ic2.core.ref.Ic2Items.get("filled_tin_can"))) {
                            player.getInventory().items.set(i, can.finishUsingItem(level, player));
                            ElectricItem.manager.use(stack, 1000, null);
                            ret = true;
                            break;
                        }
                    }
                }
                for (MobEffectInstance effect : new ArrayList<>(player.getActiveEffects())) {
                    Integer cost = potionRemovalCost.get(effect.getEffect());
                    if (cost == null && effect.getEffect().getRegisteredName().equals("ic2:radiation")) cost = 10000;
                    if (cost == null) continue;
                    cost *= effect.getAmplifier() + 1;
                    if (ElectricItem.manager.canUse(stack, cost)) { ElectricItem.manager.use(stack, cost, null); player.removeEffect(effect.getEffect()); }
                }
                ArmorUtil.hudMode(level, player, stack);
                ret |= ArmorUtil.nightvision(level, player, stack, "Nightvision");
            }
            case 1 -> {
                byte toggleTimer = nbt.getByte("toggleTimer");
                boolean jetpack = nbt.getBoolean("jetpack"), hoverMode = nbt.getBoolean("hoverMode");
                if (Keyboard.isJumpKeyDown(player) && Keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0) {
                    toggleTimer = 10;
                    hoverMode = !hoverMode;
                    if (!level.isClientSide) { nbt.putBoolean("hoverMode", hoverMode); player.displayClientMessage(Component.literal(hoverMode ? "Quantum Hover Mode enabled." : "Quantum Hover Mode disabled."), false); }
                }
                if (Keyboard.isBoostKeyDown(player) && Keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0) {
                    toggleTimer = 10;
                    jetpack = !jetpack;
                    if (!level.isClientSide) { nbt.putBoolean("jetpack", jetpack); player.displayClientMessage(Component.literal(jetpack ? "Quantum Jetpack enabled." : "Quantum Jetpack disabled."), false); }
                }
                if (jetpack && (Keyboard.isJumpKeyDown(player) || hoverMode && player.getDeltaMovement().y < -0.03)) ret = useJetpack(player, hoverMode);
                if (!level.isClientSide && toggleTimer > 0) nbt.putByte("toggleTimer", --toggleTimer);
                if (!level.isClientSide) ArmorUtil.save(stack, nbt);
                player.clearFire();
            }
            case 2 -> {
                if (ElectricItem.manager.canUse(stack, 1000) && (player.onGround() || player.isInWater()) && Keyboard.isForwardKeyDown(player) && player.isSprinting()) {
                    byte speedTicker = nbt.getByte("speedTicker");
                    if (++speedTicker >= 10) { speedTicker = 0; if (!level.isClientSide) ElectricItem.manager.use(stack, 1000, null); ret = true; }
                    nbt.putByte("speedTicker", speedTicker);
                    if (!level.isClientSide) ArmorUtil.save(stack, nbt);
                    float speed = 0.22f;
                    if (player.isInWater()) {
                        speed = 0.1f;
                        if (Keyboard.isJumpKeyDown(player)) player.setDeltaMovement(player.getDeltaMovement().add(0, 0.1, 0));
                    }
                    player.moveRelative(speed, new Vec3(0, 0, 1));
                }
            }
            case 3 -> {
                if (!level.isClientSide) {
                    boolean wasOnGround = !nbt.contains("wasOnGround") || nbt.getBoolean("wasOnGround");
                    if (wasOnGround && !player.onGround() && Keyboard.isJumpKeyDown(player) && Keyboard.isBoostKeyDown(player)) { ElectricItem.manager.use(stack, 4000, null); ret = true; }
                    if (player.onGround() != wasOnGround) { nbt.putBoolean("wasOnGround", player.onGround()); ArmorUtil.save(stack, nbt); }
                } else {
                    if (ElectricItem.manager.canUse(stack, 4000) && player.onGround()) jumpCharge = 1;
                    Vec3 m = player.getDeltaMovement();
                    if (m.y >= 0 && jumpCharge > 0 && !player.isInWater()) {
                        if (Keyboard.isJumpKeyDown(player) && Keyboard.isBoostKeyDown(player)) {
                            if (jumpCharge == 1) m = new Vec3(m.x * 3.5, m.y, m.z * 3.5);
                            player.setDeltaMovement(m.x, m.y + jumpCharge * 0.3f, m.z);
                            jumpCharge *= 0.75f;
                        } else if (jumpCharge < 1) jumpCharge = 0;
                    }
                }
            }
        }
        if (ret && !level.isClientSide) player.inventoryMenu.broadcastChanges();
    }

    public boolean useJetpack(Player player, boolean hoverMode) {
        ItemStack jetpack = player.getItemBySlot(EquipmentSlot.CHEST);
        double charge = ElectricItem.manager.getCharge(jetpack);
        if (charge == 0) return false;
        float power = 1, dropPercentage = 0.05f;
        if (charge / getMaxCharge(jetpack) <= dropPercentage) power *= (float)(charge / (getMaxCharge(jetpack) * dropPercentage));
        if (Keyboard.isForwardKeyDown(player)) {
            float forwardpower = power * (hoverMode ? 0.5f : 3.5f) * 2;
            if (forwardpower > 0) player.moveRelative(0.02f, new Vec3(0, 0, 0.4f * forwardpower));
        }
        int worldHeight = player.level().getMaxBuildHeight();
        double y = player.getY();
        if (y > worldHeight - 25) { if (y > worldHeight) y = worldHeight; power *= (float)((worldHeight - y) / 25); }
        Vec3 m = player.getDeltaMovement();
        double prev = m.y, my = Math.min(m.y + power * 0.2f, 0.6f);
        if (hoverMode) {
            float maxHoverY = player.isShiftKeyDown() ? -0.1f : Keyboard.isJumpKeyDown(player) ? 0.1f : -0.025f;
            if (my > maxHoverY) { my = maxHoverY; if (prev > my) my = prev; }
        }
        player.setDeltaMovement(m.x, my, m.z);
        if (!player.level().isClientSide) ElectricItem.manager.discharge(jetpack, hoverMode ? 10 : 8, Integer.MAX_VALUE, true, false, false);
        player.fallDistance = 0;
        ic2.core.util.Util.resetPlayerInAirTime(player);
        return true;
    }
}
