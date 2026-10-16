package ic2.core.block.wiring;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.core.ContainerBase;
import ic2.core.ref.Ic2BlockEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Charge pads: storage blocks that charge the armor and inventory of a player standing on them (32/128/512/2048 EU per tick). */
public abstract class TileEntityChargepad extends TileEntityElectricBlock {
    private int updateTicker = java.util.concurrent.ThreadLocalRandom.current().nextInt(2);
    private Player player;
    private boolean emitting;
    private final int chargeFactor;

    protected TileEntityChargepad(BlockEntityType<?> type, BlockPos pos, BlockState state, int tier, int output, int maxStorage) {
        super(type, pos, state, tier, output, maxStorage);
        chargeFactor = output;
    }

    public void playerStandsAt(Player p) { if (player == null || !player.getUUID().equals(p.getUUID())) player = p; }

    @Override protected byte redstoneModeCount() { return 2; }

    @Override
    protected void updateRedstone() {
        if (updateTicker++ % 2 != 0) return;
        boolean changed = false;
        if (player != null && energy >= 1) {
            if (!getActive()) { setActive(true); changed = true; }
            for (ItemStack s : player.getInventory().armor) charge(s);
            for (ItemStack s : player.getInventory().items) charge(s);
            player = null;
            changed = true;
        } else if (getActive()) { setActive(false); changed = true; }
        boolean emit = redstoneMode == 0 ? getActive() : !getActive();
        if (emit != emitting) { emitting = emit; level.updateNeighborsAt(worldPosition, getBlockState().getBlock()); }
        if (changed) setChanged();
    }

    private void charge(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IElectricItem)) return;
        double free = ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, tier, true, true);
        if (free <= 0) return;
        double amount = Math.min(Math.min(free, chargeFactor * 2), energy);
        energy -= ElectricItem.manager.charge(stack, amount, tier, true, false);
    }

    @Override public boolean isEmittingRedstone() { return emitting; }
    @Override public boolean acceptsEnergyFrom(BlockEntity emitter, Direction direction) { return direction != Direction.UP && direction != getFacing(); }
    @Override public boolean wrenchCanSetFacing(Player p, Direction side) { return side != Direction.UP && getFacing() != side; }
    @Override public Component getRedstoneMode() { return redstoneMode <= 1 && redstoneMode >= 0 ? Component.translatable("ic2.blockChargepad.gui.mod.redstone" + redstoneMode) : Component.empty(); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerChargepad(id, inv, this); }

    public static class BatBox extends TileEntityChargepad { public BatBox(BlockPos p, BlockState s) { super(Ic2BlockEntities.CHARGEPAD_BATBOX.get(), p, s, 1, 32, 40000); } }
    public static class CESU extends TileEntityChargepad { public CESU(BlockPos p, BlockState s) { super(Ic2BlockEntities.CHARGEPAD_CESU.get(), p, s, 2, 128, 300000); } }
    public static class MFE extends TileEntityChargepad { public MFE(BlockPos p, BlockState s) { super(Ic2BlockEntities.CHARGEPAD_MFE.get(), p, s, 3, 512, 4000000); } }
    public static class MFSU extends TileEntityChargepad { public MFSU(BlockPos p, BlockState s) { super(Ic2BlockEntities.CHARGEPAD_MFSU.get(), p, s, 4, 2048, 40000000); } }
}
