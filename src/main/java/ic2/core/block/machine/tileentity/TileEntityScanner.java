package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IPatternStorage;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.machine.container.ContainerUu;
import ic2.core.item.ItemCrystalMemory;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.uu.UuGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Scanner: 3300 ticks at 256 EU/t destroy one item and record it as a pattern (crystal memory or adjacent pattern storage). */
public class TileEntityScanner extends TileEntityElectricMachine implements IHasGui {
    public enum State { IDLE, SCANNING, COMPLETED, FAILED, NO_STORAGE, NO_ENERGY, TRANSFER_ERROR, ALREADY_RECORDED }
    public static final int DURATION = 3300, ENERGY_PER_TICK = 256;
    private ItemStack currentStack = ItemStack.EMPTY, pattern = ItemStack.EMPTY;
    public int progress;
    public State state = State.IDLE;
    public double patternUu, patternEu;
    public final InvSlotConsumable inputSlot = new InvSlotConsumable(this, "input", InvSlot.Access.I, 1, InvSlot.InvSide.TOP) {
        @Override public boolean accepts(ItemStack s) { return UuGraph.get(s) < Double.POSITIVE_INFINITY; }
    };
    public final InvSlotConsumable diskSlot = new InvSlotConsumable(this, "disk", InvSlot.Access.IO, 1, InvSlot.InvSide.ANY) {
        @Override public boolean accepts(ItemStack s) { return s.getItem() instanceof ItemCrystalMemory; }
    };

    public TileEntityScanner(BlockPos pos, BlockState state) { super(Ic2BlockEntities.SCANNER.get(), pos, state, 512000, 3); inputSlot.setStackSizeLimit(1); }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean active = false;
        if (progress < DURATION) {
            if (!inputSlot.isEmpty() && (currentStack.isEmpty() || currentStack.is(inputSlot.get().getItem()))) {
                if (getPatternStorage() == null && diskSlot.isEmpty()) { state = State.NO_STORAGE; reset(); }
                else if (energy >= ENERGY_PER_TICK) {
                    if (currentStack.isEmpty()) currentStack = inputSlot.get().copyWithCount(1);
                    pattern = UuGraph.find(currentStack);
                    if (pattern.isEmpty()) state = State.FAILED;
                    else if (isPatternRecorded(pattern)) { state = State.ALREADY_RECORDED; reset(); }
                    else {
                        active = true;
                        state = State.SCANNING;
                        energy -= ENERGY_PER_TICK;
                        if (++progress >= DURATION) {
                            refreshInfo();
                            if (patternUu != Double.POSITIVE_INFINITY) { state = State.COMPLETED; inputSlot.consume(1, false, true); setChanged(); }
                            else state = State.FAILED;
                        }
                    }
                } else state = State.NO_ENERGY;
            } else { state = State.IDLE; reset(); }
        } else if (pattern.isEmpty()) { state = State.IDLE; progress = 0; }
        setActive(active);
    }

    public void reset() { progress = 0; currentStack = ItemStack.EMPTY; pattern = ItemStack.EMPTY; }

    private boolean isPatternRecorded(ItemStack stack) {
        if (diskSlot.get().getItem() instanceof ItemCrystalMemory m && m.readItemStack(diskSlot.get()).is(stack.getItem())) return true;
        IPatternStorage storage = getPatternStorage();
        if (storage != null) for (ItemStack s : storage.getPatterns()) if (s.is(stack.getItem())) return true;
        return false;
    }

    private void record() {
        if (!pattern.isEmpty() && patternUu != Double.POSITIVE_INFINITY) {
            if (diskSlot.get().getItem() instanceof ItemCrystalMemory m) m.writeContentsTag(diskSlot.get(), pattern);
            else {
                IPatternStorage storage = getPatternStorage();
                if (storage == null || !storage.addPattern(pattern)) { state = State.TRANSFER_ERROR; return; }
            }
        }
        reset();
    }

    public void onNetworkEvent(int event) {
        if (event == 0) reset();
        else if (event == 1 && progress >= DURATION) record();
        setChanged();
    }

    public IPatternStorage getPatternStorage() {
        for (Direction d : Direction.values()) if (ic2.core.util.Util.loadedBlockEntity(level, worldPosition.relative(d)) instanceof IPatternStorage s) return s;
        return null;
    }

    private void refreshInfo() { if (!pattern.isEmpty()) patternUu = UuGraph.getInBuckets(pattern); }
    public int getPercentageDone() { return 100 * progress / DURATION; }
    public int getSubPercentageDoneScaled(int width) { return width * (100 * progress % DURATION) / DURATION; }
    public boolean isDone() { return progress >= DURATION; }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) {
        super.readFromNBT(nbt, r);
        progress = nbt.getInt("progress");
        currentStack = ItemStack.parseOptional(r, nbt.getCompound("currentStack"));
        pattern = ItemStack.parseOptional(r, nbt.getCompound("pattern"));
        int s = nbt.getInt("state");
        state = s < State.values().length ? State.values()[s] : State.IDLE;
    }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) {
        super.writeToNBT(nbt, r);
        nbt.putInt("progress", progress);
        if (!currentStack.isEmpty()) nbt.put("currentStack", currentStack.save(r));
        if (!pattern.isEmpty()) nbt.put("pattern", pattern.save(r));
        nbt.putInt("state", state.ordinal());
    }
    @Override public void onLoaded() { super.onLoaded(); refreshInfo(); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerUu.Scanner(id, inv, this); }
}
