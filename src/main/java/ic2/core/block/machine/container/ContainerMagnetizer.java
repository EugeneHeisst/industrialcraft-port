package ic2.core.block.machine.container;

import com.mojang.datafixers.util.Pair;
import ic2.core.block.machine.tileentity.TileEntityMagnetizer;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMagnetizer extends ContainerElectricMachine<TileEntityMagnetizer> {
    public final Player player;
    public ContainerMagnetizer(int id, Inventory inv, TileEntityMagnetizer base) {
        super(id, inv, base, 166, 8, 44);
        player = inv.player;
        for (int i = 0; i < 4; i++) addSlot(new SlotInvSlot(base.upgradeSlot, i, 152, 8 + i * 18));
        addSlot(new Slot(inv, 36, 45, 26) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.canEquip(EquipmentSlot.FEET, player); }
            @Override public int getMaxStackSize() { return 1; }
            @Override public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() { return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS); }
        });
    }
}
