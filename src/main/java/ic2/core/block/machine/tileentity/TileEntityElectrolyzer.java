package ic2.core.block.machine.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.machine.container.ContainerElectrolyzer;
import ic2.core.block.wiring.TileEntityElectricBlock;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Energy buffer for an adjacent storage block: above 70 % it electrolyzes water cells (20000 EU each), below 30 % it turns
 * electrolyzed water cells back into water cells and feeds 12000 + 2000 * tier EU into the storage.
 */
public class TileEntityElectrolyzer extends TileEntityInventory implements IHasGui {
    public short energy;
    public TileEntityElectricBlock mfe;
    private int ticker = java.util.concurrent.ThreadLocalRandom.current().nextInt(16);
    public final InvSlotConsumable waterSlot = new InvSlotConsumable(this, "water", InvSlot.Access.IO, 1, InvSlot.InvSide.TOP) {
        @Override public boolean accepts(ItemStack stack) { return stack.is(Ic2Items.WATER_CELL.get()); }
    };
    public final InvSlotConsumable hydrogenSlot = new InvSlotConsumable(this, "hydrogen", InvSlot.Access.IO, 1, InvSlot.InvSide.BOTTOM) {
        @Override public boolean accepts(ItemStack stack) { return stack.is(Ic2Items.get("electrolyzed_water_cell")); }
    };

    public TileEntityElectrolyzer(BlockPos pos, BlockState state) { super(Ic2BlockEntities.ELECTROLYZER.get(), pos, state); }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); energy = nbt.getShort("energy"); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putShort("energy", energy); }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false, turnActive = false;
        if (ticker++ % 16 == 0) mfe = lookForMFE();
        if (mfe == null || mfe.isRemoved()) { mfe = null; setActive(false); return; }
        if (shouldDrain() && canDrain()) { needsInvUpdate = drain(); turnActive = true; }
        if (shouldPower() && (canPower() || energy > 0)) { needsInvUpdate = power(); turnActive = true; }
        setActive(turnActive);
        if (needsInvUpdate) setChanged();
    }

    public boolean shouldDrain() { return mfe.energy / mfe.maxStorage >= 0.7; }
    public boolean shouldPower() { return mfe.energy / mfe.maxStorage <= 0.3; }

    private static boolean hasRoom(InvSlot slot) { return slot.isEmpty() || slot.get().getCount() < Math.min(slot.getStackSizeLimit(), slot.get().getMaxStackSize()); }
    public boolean canDrain() { return !waterSlot.consume(1, true, true).isEmpty() && hasRoom(hydrogenSlot); }
    public boolean canPower() { return !hydrogenSlot.consume(1, true, true).isEmpty() && hasRoom(waterSlot); }

    public boolean drain() {
        mfe.energy -= processRate();
        energy += processRate();
        if (energy < 20000) return false;
        energy -= 20000;
        waterSlot.consume(1, false, true);
        if (hydrogenSlot.isEmpty()) hydrogenSlot.put(new ItemStack(Ic2Items.get("electrolyzed_water_cell")));
        else hydrogenSlot.get().grow(1);
        return true;
    }

    public boolean power() {
        if (energy > 0) {
            int out = Math.min(energy, processRate());
            energy -= out;
            mfe.energy += out;
            return false;
        }
        energy += 12000 + 2000 * mfe.tier;
        hydrogenSlot.consume(1, false, true);
        if (waterSlot.isEmpty()) waterSlot.put(new ItemStack(Ic2Items.WATER_CELL.get()));
        else waterSlot.get().grow(1);
        return true;
    }

    public int processRate() { return switch (mfe.tier) { case 2 -> 8; case 3 -> 32; case 4 -> 128; default -> 2; }; }

    public TileEntityElectricBlock lookForMFE() {
        for (Direction d : Direction.values()) if (ic2.core.util.Util.loadedBlockEntity(level, worldPosition.relative(d)) instanceof TileEntityElectricBlock e) return e;
        return null;
    }

    public int gaugeEnergyScaled(int i) { return energy <= 0 ? 0 : Math.min(i, energy * i / 20000); }

    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerElectrolyzer(id, inv, this); }
}
