package ic2.core.item.tool;

import ic2.core.block.wiring.TileEntityCable;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Insulation cutter: right click insulates a bare cable using rubber from the inventory, left click strips insulation. */
@EventBusSubscriber(modid = ic2.core.IC2.MODID)
public class ItemToolCutter extends ItemCraftingTool {
    public ItemToolCutter(Properties properties) { super(properties); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null || !(ctx.getLevel().getBlockEntity(ctx.getClickedPos()) instanceof TileEntityCable cable)) return InteractionResult.PASS;
        var target = cable.getCableType().insulated();
        if (target == null) return InteractionResult.PASS;
        int slot = player.getInventory().findSlotMatchingItem(new ItemStack(Ic2Items.get("rubber")));
        if (slot < 0 && !player.getAbilities().instabuild) return InteractionResult.PASS;
        if (!ctx.getLevel().isClientSide) {
            if (slot >= 0 && !player.getAbilities().instabuild) player.getInventory().removeItem(slot, 1);
            cable.changeType(target);
            damageCutter(ctx.getItemInHand(), 1, player);
        }
        return InteractionResult.sidedSuccess(ctx.getLevel().isClientSide);
    }

    public static void cutInsulationFrom(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof TileEntityCable cable)) return;
        var target = cable.getCableType().uninsulated();
        if (target == null || level.isClientSide) return;
        cable.changeType(target);
        ItemEntity rubber = new ItemEntity(level, pos.getX() + level.random.nextFloat() * .7 + .15, pos.getY() + level.random.nextFloat() * .7 + .15, pos.getZ() + level.random.nextFloat() * .7 + .15, new ItemStack(Ic2Items.get("rubber")));
        rubber.setPickUpDelay(10);
        level.addFreshEntity(rubber);
        damageCutter(stack, 3, player);
    }

    private static void damageCutter(ItemStack stack, int amount, Player player) {
        stack.hurtAndBreak(amount, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
    }

    @SubscribeEvent
    static void leftClick(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack held = event.getEntity().getMainHandItem();
        if (held.getItem() instanceof ItemToolCutter && event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START)
            cutInsulationFrom(held, event.getLevel(), event.getPos(), event.getEntity());
    }
}
