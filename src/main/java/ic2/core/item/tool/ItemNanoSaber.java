package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.core.item.armor.ItemArmorNanoSuit;
import ic2.core.item.armor.ItemArmorQuantumSuit;
import java.util.EnumSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Right click toggles the blade (needs 16 EU): 20 damage while active, drains 64 EU every 16 ticks in the hotbar and strips nano/quantum armour charge. */
public class ItemNanoSaber extends ItemElectricTool {
    public static int ticker;

    public ItemNanoSaber(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON), 10, HarvestLevel.Diamond, EnumSet.of(ToolClass.Sword));
        maxCharge = 160000;
        transferLimit = 500;
        tier = 3;
    }

    /** Charge updates keep the blade in hand; toggles, replacement and slot changes still animate. */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (slotChanged || oldStack.getItem() != newStack.getItem()) return true;
        ItemStack oldVisual = oldStack.copy(), newVisual = newStack.copy();
        oldVisual.remove(ic2.core.ref.Ic2Components.CHARGE.get());
        newVisual.remove(ic2.core.ref.Ic2Components.CHARGE.get());
        return !ItemStack.isSameItemSameComponents(oldVisual, newVisual);
    }

    public static boolean isActive(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("active"); }
    private static void setActive(ItemStack stack, boolean active) { CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean("active", active)); }

    @Override public float getDestroySpeed(ItemStack stack, BlockState state) { return isActive(stack) ? 4 : 1; }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        int dmg = ElectricItem.manager.canUse(stack, 400) && isActive(stack) ? 20 : 4;
        return ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, dmg, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
    }

    public static void drainSaber(ItemStack stack, double amount, LivingEntity entity) {
        if (!ElectricItem.manager.use(stack, amount, entity)) setActive(stack, false);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity source) {
        if (!isActive(stack) || source.level().isClientSide) return true;
        drainSaber(stack, 400, source);
        boolean pvp = !(source instanceof Player) || !(target instanceof Player) || source.level().getServer() == null || source.level().getServer().isPvpAllowed();
        if (pvp) for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            if (!ElectricItem.manager.canUse(stack, 2000)) break;
            ItemStack armor = target.getItemBySlot(slot);
            double amount = armor.getItem() instanceof ItemArmorNanoSuit ? 48000 : armor.getItem() instanceof ItemArmorQuantumSuit ? 300000 : 0;
            if (amount <= 0) continue;
            ElectricItem.manager.discharge(armor, amount, tier, true, false, false);
            if (!ElectricItem.manager.canUse(armor, 1)) target.setItemSlot(slot, ItemStack.EMPTY);
            drainSaber(stack, 2000, source);
        }
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, net.minecraft.core.BlockPos pos, LivingEntity entity) {
        if (isActive(stack)) drainSaber(stack, 80, entity);
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (isActive(stack)) setActive(stack, false);
            else if (ElectricItem.manager.canUse(stack, 16)) setActive(stack, true);
        }
        return super.use(level, player, hand);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!isActive(stack) || !(entity instanceof Player player) || level.isClientSide) return;
        long t = level.getGameTime();
        if (t % 16 == 0) { if (slot < 9) drainSaber(stack, 64, player); else if (t % 64 == 0) drainSaber(stack, 16, player); }
    }
}
