package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.core.block.generator.tileentity.TileEntitySolarGenerator;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.Ic2ArmorMaterials;
import ic2.core.ref.Ic2Items;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Non-protective special armor: hazmat suit, night vision goggles, solar helmet and static boots. */
public final class ItemArmorUtility {
    private ItemArmorUtility() {}

    public static class Hazmat extends ItemArmorIC2 {
        public Hazmat(Type type, Properties properties) { super(Ic2ArmorMaterials.HAZMAT, type, properties); }

        public static boolean hasCompleteHazmat(LivingEntity living) {
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD})
                if (!(living.getItemBySlot(slot).getItem() instanceof Hazmat)) return false;
            return true;
        }

        @Override
        public void onArmorTick(Level level, Player player, ItemStack stack) {
            if (armorType() != 0 || level.isClientSide) return;
            if (player.isOnFire() && hasCompleteHazmat(player)) {
                if (player.isInLava()) player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20, 0, true, true));
                player.clearFire();
            }
            if (player.getAirSupply() <= 100) {
                int slot = player.getInventory().findSlotMatchingItem(new ItemStack(Ic2Items.AIR_CELL.get()));
                if (slot >= 0) {
                    player.getInventory().removeItem(slot, 1);
                    ItemStack cell = new ItemStack(Ic2Items.EMPTY_CELL.get());
                    if (!player.getInventory().add(cell)) player.drop(cell, false);
                    player.setAirSupply(player.getAirSupply() + 150);
                    player.inventoryMenu.broadcastChanges();
                }
            }
        }
    }

    public static class NightvisionGoggles extends ItemArmorIC2 implements IElectricItem {
        public NightvisionGoggles(Properties properties) { super(Ic2ArmorMaterials.NIGHTVISION, Type.HELMET, properties.stacksTo(1).setNoRepair()); }
        @Override public void onArmorTick(Level level, Player player, ItemStack stack) { if (ArmorUtil.nightvision(level, player, stack, "active")) player.inventoryMenu.broadcastChanges(); }
        @Override public boolean canProvideEnergy(ItemStack stack) { return false; }
        @Override public double getMaxCharge(ItemStack stack) { return 200000; }
        @Override public int getTier(ItemStack stack) { return 1; }
        @Override public double getTransferLimit(ItemStack stack) { return 200; }
        @Override public boolean isBarVisible(ItemStack stack) { return true; }
        @Override public int getBarWidth(ItemStack stack) { return Math.round(13f * (float)Mth.clamp(ElectricItemManager.getRawCharge(stack) / 200000, 0, 1)); }
        @Override public int getBarColor(ItemStack stack) { return Mth.hsvToRgb((float)Mth.clamp(ElectricItemManager.getRawCharge(stack) / 200000, 0, 1) / 3f, 1, 1); }
        @Override public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) {
            String tip = ElectricItem.manager.getToolTip(stack);
            if (tip != null) list.add(Component.literal(tip).withStyle(ChatFormatting.GRAY));
        }
    }

    /** Charges the chest piece by 1 EU per tick in sunlight. */
    public static class SolarHelmet extends ItemArmorIC2 {
        public SolarHelmet(Properties properties) { super(Ic2ArmorMaterials.SOLAR, Type.HELMET, properties.stacksTo(1)); }
        @Override public void onArmorTick(Level level, Player player, ItemStack stack) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (level.isClientSide || chest.isEmpty()) return;
            if (TileEntitySolarGenerator.isSunVisible(level, player.blockPosition().above()) && ElectricItem.manager.charge(chest, 1, Integer.MAX_VALUE, true, false) > 0) player.inventoryMenu.broadcastChanges();
        }
    }

    /** Rubber boots: charge the chest piece by up to 3 EU per 5 blocks walked. */
    public static class StaticBoots extends ItemArmorIC2 {
        public StaticBoots(Properties properties) { super(Ic2ArmorMaterials.RUBBER, Type.BOOTS, properties.stacksTo(1)); }
        @Override public void onArmorTick(Level level, Player player, ItemStack stack) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (level.isClientSide || chest.isEmpty()) return;
            CompoundTag nbt = ArmorUtil.tag(stack);
            boolean notWalking = player.getVehicle() != null || player.isInWater();
            int px = (int)player.getX(), pz = (int)player.getZ();
            if (!nbt.contains("x") || notWalking) nbt.putInt("x", px);
            if (!nbt.contains("z") || notWalking) nbt.putInt("z", pz);
            double distance = Math.sqrt(Math.pow(nbt.getInt("x") - px, 2) + Math.pow(nbt.getInt("z") - pz, 2));
            boolean ret = false;
            if (distance >= 5) {
                nbt.putInt("x", px);
                nbt.putInt("z", pz);
                ret = ElectricItem.manager.charge(chest, Math.min(3, distance / 5), Integer.MAX_VALUE, true, false) > 0;
            }
            ArmorUtil.save(stack, nbt);
            if (ret) player.inventoryMenu.broadcastChanges();
        }
    }
}
