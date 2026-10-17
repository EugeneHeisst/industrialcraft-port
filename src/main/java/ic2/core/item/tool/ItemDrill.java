package ic2.core.item.tool;

import ic2.core.util.Keyboard;
import java.util.EnumSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

/** Mining drills: pickaxe + shovel; right click places torches from the inventory. The iridium drill has fortune III or silk touch (mode switch + right click). */
public class ItemDrill extends ItemElectricTool {
    private final boolean iridium;

    public ItemDrill(Properties properties, int operationEnergyCost, HarvestLevel level, int maxCharge, int transferLimit, int tier, float efficiency, boolean iridium) {
        super(properties, operationEnergyCost, level, EnumSet.of(ToolClass.Pickaxe, ToolClass.Shovel));
        this.maxCharge = maxCharge;
        this.transferLimit = transferLimit;
        this.tier = tier;
        this.efficiencyOnProperMaterial = efficiency;
        this.iridium = iridium;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null || iridium && Keyboard.isModeSwitchKeyDown(player)) return InteractionResult.PASS;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack torch = player.getInventory().items.get(i);
            if (torch.isEmpty() || !(torch.getItem() instanceof BlockItem) || !torch.getDescriptionId().toLowerCase().contains("torch")) continue;
            int oldSize = torch.getCount();
            InteractionResult result = torch.useOn(new UseOnContext(ctx.getLevel(), player, ctx.getHand(), torch, new net.minecraft.world.phys.BlockHitResult(ctx.getClickLocation(), ctx.getClickedFace(), ctx.getClickedPos(), ctx.isInside())));
            if (player.getAbilities().instabuild) torch.setCount(oldSize);
            if (result.consumesAction()) return result;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (iridium && !level.isClientSide && Keyboard.isModeSwitchKeyDown(player)) {
            var enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            boolean silk = EnchantmentHelper.getItemEnchantmentLevel(enchants.getOrThrow(Enchantments.SILK_TOUCH), stack) > 0;
            var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            mutable.set(enchants.getOrThrow(Enchantments.FORTUNE), 3);
            if (!silk) mutable.set(enchants.getOrThrow(Enchantments.SILK_TOUCH), 1);
            EnchantmentHelper.setEnchantments(stack, mutable.toImmutable());
            player.displayClientMessage(Component.translatable("ic2.tooltip.mode", Component.translatable(silk ? "ic2.tooltip.mode.normal" : "ic2.tooltip.mode.silkTouch")), false);
        }
        return super.use(level, player, hand);
    }

    /** The iridium drill always carries fortune III (the original set it when the stack was created). */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (iridium && !level.isClientSide && stack.getEnchantments().isEmpty()) {
            var enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            mutable.set(enchants.getOrThrow(Enchantments.FORTUNE), 3);
            EnchantmentHelper.setEnchantments(stack, mutable.toImmutable());
        }
    }

    @Override public boolean isEnchantable(ItemStack stack) { return false; }
}
