package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.core.WindSim;
import java.util.EnumSet;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Wind meter: 50 EU per reading of the wind strength at the player's height. */
public class ItemWindmeter extends ItemElectricTool {
    public ItemWindmeter(Properties properties) {
        super(properties.stacksTo(1), 50, HarvestLevel.Wood, EnumSet.noneOf(ToolClass.class));
        maxCharge = 10000; transferLimit = 100; tier = 1;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !ElectricItem.manager.canUse(stack, operationEnergyCost)) return InteractionResultHolder.pass(stack);
        ElectricItem.manager.use(stack, operationEnergyCost, player);
        double wind = Math.max(0, WindSim.get(level).getWindAt(player.getY()));
        player.displayClientMessage(Component.translatable("ic2.itemwindmeter.info", Math.round(wind * 100) / 100f), false);
        return InteractionResultHolder.success(stack);
    }
}
