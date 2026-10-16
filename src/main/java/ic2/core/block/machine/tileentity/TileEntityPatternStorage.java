package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IPatternStorage;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.machine.container.ContainerUu;
import ic2.core.item.ItemCrystalMemory;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.uu.UuGraph;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/** Pattern storage: keeps scanned patterns, browsable, with import / export to crystal memory. */
public class TileEntityPatternStorage extends TileEntityInventory implements IHasGui, IPatternStorage {
    public final InvSlotConsumable diskSlot = new InvSlotConsumable(this, "SaveSlot", InvSlot.Access.IO, 1, InvSlot.InvSide.ANY) {
        @Override public boolean accepts(ItemStack s) { return s.getItem() instanceof ItemCrystalMemory; }
    };
    private final List<ItemStack> patterns = new ArrayList<>();
    public int index, maxIndex;
    public ItemStack pattern = ItemStack.EMPTY;
    public double patternUu, patternEu;

    public TileEntityPatternStorage(BlockPos pos, BlockState state) { super(Ic2BlockEntities.PATTERN_STORAGE.get(), pos, state); }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); patterns.clear(); readPatterns(nbt); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); writePatterns(nbt); }

    public void readPatterns(CompoundTag nbt) {
        for (Tag t : nbt.getList("patterns", Tag.TAG_STRING)) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(t.getAsString()));
            if (item != Items.AIR) addPattern(new ItemStack(item));
        }
        refreshInfo();
    }
    public void writePatterns(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (ItemStack s : patterns) list.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(s.getItem()).toString()));
        nbt.put("patterns", list);
    }

    @Override public ItemStack getWrenchDrop(Player player) {
        ItemStack ret = super.getWrenchDrop(player);
        CompoundTag tag = new CompoundTag(); writePatterns(tag);
        ret.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA, net.minecraft.world.item.component.CustomData.of(withId(tag)));
        return ret;
    }
    private CompoundTag withId(CompoundTag t) { t.putString("id", "ic2:pattern_storage"); return t; }

    public void onNetworkEvent(int event) {
        switch (event) {
            case 0 -> { if (!patterns.isEmpty()) { index = index <= 0 ? patterns.size() - 1 : index - 1; refreshInfo(); } }
            case 1 -> { if (!patterns.isEmpty()) { index = index >= patterns.size() - 1 ? 0 : index + 1; refreshInfo(); } }
            case 2 -> { if (index >= 0 && index < patterns.size() && diskSlot.get().getItem() instanceof ItemCrystalMemory m) m.writeContentsTag(diskSlot.get(), patterns.get(index)); }
            case 3 -> { if (diskSlot.get().getItem() instanceof ItemCrystalMemory m) { ItemStack rec = m.readItemStack(diskSlot.get()); if (!rec.isEmpty()) addPattern(rec); } }
            default -> {}
        }
        setChanged();
    }

    public void refreshInfo() {
        if (index < 0 || index >= patterns.size()) index = 0;
        maxIndex = patterns.size();
        if (patterns.isEmpty()) pattern = ItemStack.EMPTY;
        else { pattern = patterns.get(index); patternUu = UuGraph.getInBuckets(pattern); }
    }

    @Override
    public boolean addPattern(ItemStack stack) {
        for (ItemStack p : patterns) if (p.is(stack.getItem())) return false;
        patterns.add(stack.copyWithCount(1));
        refreshInfo();
        setChanged();
        return true;
    }

    @Override public List<ItemStack> getPatterns() { return patterns; }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerUu.PatternStorage(id, inv, this); }
}
