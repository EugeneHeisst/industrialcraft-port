package ic2.core.block;

import ic2.api.energy.tile.IHeatSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Heat generator: keeps a buffer of up to getMaxHeatEmittedPerTick hU and hands it out through its front face. */
public abstract class TileEntityHeatSourceInventory extends TileEntityInventory implements IHeatSource {
    public int transmitHeat, maxHeatEmitpeerTick;
    protected int HeatBuffer;

    protected TileEntityHeatSourceInventory(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        int amount = getMaxHeatEmittedPerTick() - HeatBuffer;
        if (amount > 0) HeatBuffer += fillHeatBuffer(amount);
    }

    @Override public int maxrequestHeatTick(Direction from) { return from == getFacing() ? getMaxHeatEmittedPerTick() : 0; }

    @Override
    public int requestHeat(Direction from, int requestheat) {
        if (from != getFacing()) return 0;
        if (HeatBuffer >= requestheat) { HeatBuffer -= requestheat; transmitHeat = requestheat; return requestheat; }
        int ret = HeatBuffer;
        transmitHeat = ret;
        HeatBuffer = 0;
        return ret;
    }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); HeatBuffer = nbt.getInt("HeatBuffer"); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putInt("HeatBuffer", HeatBuffer); }
    @Override public void setChanged() { super.setChanged(); if (level != null && !level.isClientSide) maxHeatEmitpeerTick = getMaxHeatEmittedPerTick(); }
    @Override public void onLoaded() { super.onLoaded(); maxHeatEmitpeerTick = getMaxHeatEmittedPerTick(); }
    @Override public boolean wrenchCanSetFacing(Player player, Direction side) { return getFacing() != side; }

    public int getHeatBuffer() { return HeatBuffer; }
    public int gettransmitHeat() { return transmitHeat; }
    protected abstract int fillHeatBuffer(int maxAmount);
    public abstract int getMaxHeatEmittedPerTick();
}
