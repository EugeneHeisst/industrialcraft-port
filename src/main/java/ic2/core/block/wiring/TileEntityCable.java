package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.block.TileEntityBlock;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCable extends TileEntityBlock implements IEnergyConductor {
    public boolean addedToEnergyNet;
    /** Construction foam on the cable: 0 none, 1 soft (hardens in light), 2 hard wall; foamColor is the wall dye index (7 light gray). */
    public byte foamed, foamColor = 7;
    /** Obscurator look of the hardened foam's sides. */
    public final ic2.core.block.Retexture retexture = new ic2.core.block.Retexture();

    public TileEntityCable(BlockPos pos, BlockState state) { super(Ic2BlockEntities.CABLE.get(), pos, state); }
    protected TileEntityCable(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    public CableType getCableType() { return ((BlockCable)getBlockState().getBlock()).type; }
    public int getColor() { return getBlockState().getValue(BlockCable.COLOR); }

    @Override
    public void onLoaded() {
        super.onLoaded();
        EnergyNet.instance.addTile(this);
        addedToEnergyNet = true;
        onNeighborBlockChange();
    }

    @Override
    public void onUnloaded() {
        if (addedToEnergyNet && level != null && !level.isClientSide) { EnergyNet.instance.removeTile(this); addedToEnergyNet = false; }
        super.onUnloaded();
    }

    @Override public void onNeighborUpdate(BlockPos from) { if (level != null && !level.isClientSide && isLoaded()) onNeighborBlockChange(); }

    /** Connects to every registered energy tile that accepts or emits towards this cable. */
    public void onNeighborBlockChange() {
        BlockState state = getBlockState();
        BlockState next = state;
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = EnergyNet.instance.getNeighbor(this, dir);
            boolean connect = (neighbor instanceof IEnergyAcceptor a && a.acceptsEnergyFrom(this, dir.getOpposite())
                || neighbor instanceof IEnergyEmitter e && e.emitsEnergyTo(this, dir.getOpposite())) && canInteractWith(neighbor);
            next = next.setValue(BlockCable.PROPS.get(dir), connect);
        }
        if (next != state) level.setBlock(worldPosition, next, 3);
    }

    @Override public void readFromNBT(net.minecraft.nbt.CompoundTag nbt, net.minecraft.core.HolderLookup.Provider r) { super.readFromNBT(nbt, r); foamed = nbt.getByte("foamed"); foamColor = nbt.contains("foamColor") ? nbt.getByte("foamColor") : 7; retexture.read(nbt, level); }
    @Override public void writeToNBT(net.minecraft.nbt.CompoundTag nbt, net.minecraft.core.HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putByte("foamed", foamed); nbt.putByte("foamColor", foamColor); retexture.write(nbt); }
    @Override protected void writeClientData(net.minecraft.nbt.CompoundTag nbt, net.minecraft.core.HolderLookup.Provider r) { nbt.putByte("foamed", foamed); nbt.putByte("foamColor", foamColor); retexture.write(nbt); }
    @Override protected void readClientData(net.minecraft.nbt.CompoundTag nbt, net.minecraft.core.HolderLookup.Provider r) { foamed = nbt.getByte("foamed"); foamColor = nbt.getByte("foamColor"); retexture.read(nbt, level); if (level != null) level.getLightEngine().checkBlock(worldPosition); }

    public boolean isFoamed() { return foamed != 0; }

    /** Applies (1), hardens (2) or removes (0) construction foam; soft foam hardens through scheduled block ticks. */
    public boolean changeFoam(int newFoam) {
        if (foamed == newFoam) return false;
        if (level == null || level.isClientSide) return true;
        foamed = (byte)newFoam;
        if (newFoam <= 1) { foamColor = 7; retexture.clear(); }
        if (newFoam == 1) level.scheduleTick(worldPosition, getBlockState().getBlock(), 500);
        setChanged();
        sendClientUpdate();
        level.getLightEngine().checkBlock(worldPosition);
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        return true;
    }

    /** Scheduled tick of soft foam: original 1/500 per tick chance gated by light * 6 >= rand(1000). */
    public void foamTick() {
        if (foamed != 1) return;
        if (level.getMaxLocalRawBrightness(worldPosition) * 6 >= level.random.nextInt(1000)) changeFoam(2);
        else level.scheduleTick(worldPosition, getBlockState().getBlock(), 500);
    }

    /** Painter: recolours insulation (or the hardened foam wall) and reconnects. */
    public boolean changeColor(int newColor) {
        if (foamed == 2) {
            if (foamColor == newColor) return false;
            if (!level.isClientSide) { foamColor = (byte)newColor; setChanged(); sendClientUpdate(); }
            return true;
        }
        if (foamed == 1) return false;
        if (!getCableType().colorable || getColor() == newColor) return false;
        if (level.isClientSide) return true;
        if (addedToEnergyNet) EnergyNet.instance.removeTile(this);
        level.setBlock(worldPosition, getBlockState().setValue(BlockCable.COLOR, newColor), 3);
        EnergyNet.instance.addTile(this);
        addedToEnergyNet = true;
        onNeighborBlockChange();
        return true;
    }

    /** Rubber / cutter: swap to the (un)insulated block keeping the colour-less state. */
    public boolean changeType(CableType target) {
        if (target == null) return false;
        if (level.isClientSide) return true;
        if (addedToEnergyNet) EnergyNet.instance.removeTile(this);
        addedToEnergyNet = false;
        level.setBlock(worldPosition, BlockCable.byType(target).defaultBlockState(), 3);
        return true;
    }

    @Override public boolean acceptsEnergyFrom(BlockEntity emitter, Direction direction) { return canInteractWith(emitter); }
    @Override public boolean emitsEnergyTo(BlockEntity receiver, Direction direction) { return canInteractWith(receiver); }

    public boolean canInteractWith(BlockEntity te) {
        if (!(te instanceof IEnergyTile)) return false;
        if (te instanceof TileEntityCable cable) return getColor() == 0 || cable.getColor() == 0 || getColor() == cable.getColor();
        return true;
    }

    @Override public double getConductionLoss() { return getCableType().loss; }
    @Override public double getInsulationEnergyAbsorption() { return getCableType().insulationAbsorption; }
    @Override public double getInsulationBreakdownEnergy() { return 9001; }
    @Override public double getConductorBreakdownEnergy() { return getCableType().capacity + 1; }
    @Override public void removeInsulation() {}
    @Override public void removeConductor() { level.removeBlock(worldPosition, false); }
    @Override public boolean wrenchCanSetFacing(Player player, Direction side) { return false; }
    @Override public boolean wrenchCanRemove(Player player) { return false; }
    @Override public float getWrenchDropRate() { return 0; }

    /** The obscurator only works on hardened foam. */
    public boolean retexture(net.minecraft.core.Direction side, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.Direction refSide) {
        if (foamed != 2 || !retexture.set(side, state, refSide)) return false;
        setChanged();
        sendClientUpdate();
        return true;
    }

    @net.neoforged.fml.common.EventBusSubscriber(modid = ic2.core.IC2.MODID)
    static final class Retexturing {
        @net.neoforged.bus.api.SubscribeEvent
        static void retexture(ic2.api.event.RetextureEvent event) {
            if (event.level.getBlockEntity(event.pos) instanceof TileEntityCable cable && cable.retexture(event.side, event.referencedState, event.referencedSide)) event.applied = true;
        }
    }
}
