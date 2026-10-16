package ic2.core.item;

import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;

/** Inventory stored in the held item (minecraft:container component); valid while the same item (by uid) stays in that hand. */
public class HandHeldInventory extends SimpleContainer {
    protected final Player player;
    protected final InteractionHand hand;
    private final UUID uid;

    public HandHeldInventory(Player player, InteractionHand hand, int size) {
        super(size);
        this.player = player;
        this.hand = hand;
        ItemStack stack = player.getItemInHand(hand);
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.hasUUID("uid")) { UUID u = UUID.randomUUID(); CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putUUID("uid", u)); uid = u; }
        else uid = tag.getUUID("uid");
        NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
        stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
        for (int i = 0; i < size; i++) setItem(i, items.get(i));
    }

    protected ItemStack holder() {
        ItemStack s = player.getItemInHand(hand);
        var tag = s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.hasUUID("uid") && tag.getUUID("uid").equals(uid) ? s : ItemStack.EMPTY;
    }

    @Override public boolean stillValid(Player p) { return p == player && !holder().isEmpty(); }

    @Override
    public void setChanged() {
        super.setChanged();
        ItemStack s = holder();
        if (!s.isEmpty() && !player.level().isClientSide) s.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(getItems()));
    }
}
