package ic2.core.item;

import ic2.core.ContainerBase;
import ic2.core.ref.Ic2Menus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

/** Containment box: twelve slots for radioactive items, which do not irradiate the carrier while inside. */
public class ItemContainmentbox extends Item implements IHandHeldInventory {
    public ItemContainmentbox(Properties p) { super(p.stacksTo(1).rarity(Rarity.UNCOMMON)); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer sp) IHandHeldInventory.open(sp, hand, id -> createContainer(id, sp.getInventory(), sp.getItemInHand(hand), hand));
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override public ContainerBase<?> createContainer(int id, Inventory inv, ItemStack stack, InteractionHand hand) { return new Container(id, inv, new HandHeldInventory(inv.player, hand, 12)); }

    public static class Container extends ContainerBase<HandHeldInventory> {
        public Container(int id, Inventory inv, HandHeldInventory box) {
            super(Ic2Menus.HANDHELD.get(), id, inv, box);
            for (int i = 0; i < 12; i++) addSlot(new Slot(box, i, 53 + i % 4 * 18, 19 + i / 4 * 18) { @Override public boolean mayPlace(ItemStack s) { return RadioactiveItems.isRadioactive(s); } });
            addPlayerInventorySlots(inv, 176, 166);
        }
        @Override public void removed(Player p) { super.removed(p); base.setChanged(); }
    }
}
