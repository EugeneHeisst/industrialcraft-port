package ic2.core.item;

import ic2.api.item.ElectricItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** RE battery, advanced battery, energy and lapotron crystals. The RE battery right-click charges the hotbar. */
public class ItemBattery extends BaseElectricItem {
    private final boolean rechargeHotbarOnUse;

    public ItemBattery(Properties properties, double maxCharge, double transferLimit, int tier, boolean rechargeHotbarOnUse) {
        super(properties, maxCharge, transferLimit, tier);
        this.rechargeHotbarOnUse = rechargeHotbarOnUse;
    }

    @Override public boolean canProvideEnergy(ItemStack stack) { return true; }
    @Override public boolean isBarVisible(ItemStack stack) { return ElectricItemManager.getRawCharge(stack) > 0 || getDefaultMaxStackSize() == 1; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide && rechargeHotbarOnUse && ElectricItemManager.getRawCharge(itemStack) > 0) {
            boolean transferred = false;
            for (int i = 0; i < 9; i++) {
                ItemStack stack = player.getInventory().items.get(i);
                if (stack.isEmpty() || stack.getItem() instanceof ItemBattery) continue;
                double transfer = ElectricItem.manager.discharge(itemStack, 2 * transferLimit, Integer.MAX_VALUE, true, true, true);
                if (transfer <= 0) continue;
                transfer = ElectricItem.manager.charge(stack, transfer, tier, true, false);
                if (transfer <= 0) continue;
                ElectricItem.manager.discharge(itemStack, transfer, Integer.MAX_VALUE, true, true, false);
                transferred = true;
            }
            if (transferred) player.containerMenu.broadcastChanges();
        }
        return InteractionResultHolder.pass(itemStack);
    }
}
