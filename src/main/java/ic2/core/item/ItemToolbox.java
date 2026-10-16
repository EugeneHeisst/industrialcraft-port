package ic2.core.item;

import ic2.api.item.IBoxable;
import ic2.api.item.ItemWrapper;
import ic2.core.ContainerBase;
import ic2.core.block.wiring.BlockCable;
import ic2.core.item.tool.ItemCraftingTool;
import ic2.core.item.tool.ItemElectricTool;
import ic2.core.item.tool.ItemScanner;
import ic2.core.item.tool.ItemToolCutter;
import ic2.core.item.tool.ItemToolWrench;
import ic2.core.item.tool.ItemTreetap;
import ic2.core.ref.Ic2Menus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

/** Tool box: nine slots for tools, cables and single-use batteries, stored in the item. */
public class ItemToolbox extends Item implements IHandHeldInventory {
    public ItemToolbox(Properties properties) { super(properties.stacksTo(1).rarity(Rarity.UNCOMMON)); }

    public static boolean isBoxable(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof ItemToolbox) return false;
        Item i = stack.getItem();
        return ItemWrapper.canBeStoredInToolbox(stack) || i instanceof IBoxable || i instanceof ItemElectricTool || i instanceof ItemScanner || i instanceof ItemToolWrench
            || i instanceof ItemTreetap || i instanceof ItemToolCutter || i instanceof ItemCraftingTool || i instanceof ItemBatterySU
            || i instanceof BlockItem b && b.getBlock() instanceof BlockCable;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer sp) IHandHeldInventory.open(sp, hand, id -> createContainer(id, sp.getInventory(), sp.getItemInHand(hand), hand));
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override public ContainerBase<?> createContainer(int id, Inventory inv, ItemStack stack, InteractionHand hand) { return new Container(id, inv, new HandHeldInventory(inv.player, hand, 9)); }

    public static class Container extends ContainerBase<HandHeldInventory> {
        public Container(int id, Inventory inv, HandHeldInventory box) {
            super(Ic2Menus.HANDHELD.get(), id, inv, box);
            for (int col = 0; col < 9; col++) addSlot(new Slot(box, col, 8 + col * 18, 41) { @Override public boolean mayPlace(ItemStack s) { return isBoxable(s); } });
            addPlayerInventorySlots(inv, 176, 166);
        }
        @Override public void removed(Player p) { super.removed(p); base.setChanged(); }
    }
}
