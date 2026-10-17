package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.core.util.Keyboard;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.common.IShearable;

/** Axe, sword and shears in one; 9 attack damage while charged. Mode switch + right click toggles shearing. */
public class ItemElectricToolChainsaw extends ItemElectricTool {
    public ItemElectricToolChainsaw(Properties properties) {
        super(properties, 100, HarvestLevel.Iron, EnumSet.of(ToolClass.Axe, ToolClass.Sword, ToolClass.Shears));
        maxCharge = 30000;
        transferLimit = 100;
        tier = 1;
        efficiencyOnProperMaterial = 12;
    }

    public static boolean shearDisabled(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("disableShear"); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && Keyboard.isModeSwitchKeyDown(player)) {
            boolean disable = !shearDisabled(stack);
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean("disableShear", disable));
            player.displayClientMessage(Component.translatable("ic2.tooltip.mode", Component.translatable(disable ? "ic2.tooltip.mode.noShear" : "ic2.tooltip.mode.normal")), false);
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        if (!ElectricItem.manager.canUse(stack, operationEnergyCost)) return ItemAttributeModifiers.EMPTY;
        return ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 9, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        ElectricItem.manager.use(stack, operationEnergyCost, attacker);
        return true;
    }

    /** Shears sheep and similar entities for energy. */
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof IShearable shearable && !shearDisabled(stack) && shearable.isShearable(player, stack, target.level(), target.blockPosition())) {
            if (player.level().isClientSide) return ElectricItem.manager.canUse(stack, operationEnergyCost) ? InteractionResult.SUCCESS : InteractionResult.PASS;
            if (!ElectricItem.manager.use(stack, operationEnergyCost, player)) return InteractionResult.PASS;
            for (ItemStack drop : shearable.onSheared(player, stack, target.level(), target.blockPosition())) target.spawnAtLocation(drop, 1);
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.neoforged.neoforge.common.ItemAbility ability) {
        // Native shears block loot reads this from the pre-operation tool copy, so the last full charge still works.
        // Other shears actions (pumpkin carving, tripwire, wolf armour...) have no EU-paying implementation here.
        if (net.neoforged.neoforge.common.ItemAbilities.DEFAULT_SHEARS_ACTIONS.contains(ability))
            return ability == net.neoforged.neoforge.common.ItemAbilities.SHEARS_DIG && !shearDisabled(stack) && ElectricItem.manager.canUse(stack, operationEnergyCost);
        return super.canPerformAction(stack, ability);
    }
}
