package ic2.core.item;

import ic2.core.uu.UuGraph;
import ic2.core.util.Util;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;

/** Crystal memory: holds one replication pattern (the item id). */
public class ItemCrystalMemory extends Item {
    public ItemCrystalMemory(Properties p) { super(p.stacksTo(1)); }

    public ItemStack readItemStack(ItemStack stack) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("Pattern")) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString("Pattern")));
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    public void writeContentsTag(ItemStack stack, ItemStack recorded) {
        String id = BuiltInRegistries.ITEM.getKey(recorded.getItem()).toString();
        CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putString("Pattern", id));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) {
        ItemStack rec = readItemStack(stack);
        if (rec.isEmpty()) { list.add(Component.translatable("ic2.item.CrystalMemory.tooltip.Empty")); return; }
        list.add(Component.translatable("ic2.item.CrystalMemory.tooltip.Item").append(" ").append(rec.getHoverName()));
        list.add(Component.translatable("ic2.item.CrystalMemory.tooltip.UU-Matter").append(" " + Util.toSiString(UuGraph.getInBuckets(rec), 4) + "B"));
    }
}
