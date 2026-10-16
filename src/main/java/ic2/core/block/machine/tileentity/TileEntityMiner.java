package ic2.core.block.machine.tileentity;

import ic2.api.item.ElectricItem;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerMiner;
import ic2.core.item.tool.ItemScanner;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2Sounds;
import ic2.core.upgrade.IUpgradableBlock;
import ic2.core.upgrade.IUpgradeItem;
import ic2.core.upgrade.UpgradableProperty;
import ic2.core.util.OreValues;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Digs a mining pipe shaft down; with a scanner it mines the valuable ores of each layer within half the scanner range,
 * tunnelling towards them in a straight line. Without a drill it pulls the pipe back up. Liquid sources stop it until a pump
 * next to it drains them.
 */
public class TileEntityMiner extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock {
    private Mode lastMode = Mode.None;
    public int progress;
    private int scannedLevel = -1, scanRange, lastX, lastZ;
    public boolean canProvideLiquid;
    public BlockPos liquidPos = BlockPos.ZERO;
    private AudioSource audioSource;
    public final InvSlot buffer = new InvSlot(this, "buffer", InvSlot.Access.IO, 15, InvSlot.InvSide.SIDE);
    public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 1);
    public final InvSlotConsumable drillSlot = new InvSlotConsumable(this, "drill", InvSlot.Access.IO, 1, InvSlot.InvSide.TOP) {
        @Override public boolean accepts(ItemStack stack) { return stack.is(Ic2Items.MINING_DRILL.get()) || stack.is(Ic2Items.DIAMOND_DRILL.get()); }
    };
    public final InvSlotConsumable pipeSlot = new InvSlotConsumable(this, "pipe", InvSlot.Access.IO, 1, InvSlot.InvSide.TOP) {
        @Override public boolean accepts(ItemStack stack) { return stack.is(Ic2Items.MINING_PIPE.get()) || stack.getItem() instanceof BlockItem; }
    };
    public final InvSlotConsumable scannerSlot = new InvSlotConsumable(this, "scanner", InvSlot.Access.IO, 1, InvSlot.InvSide.BOTTOM) {
        @Override public boolean accepts(ItemStack stack) { return stack.getItem() instanceof ItemScanner; }
    };

    public TileEntityMiner(BlockPos pos, BlockState state) { super(Ic2BlockEntities.MINER.get(), pos, state, 1000, 1, false); }

    @Override
    public void onLoaded() {
        super.onLoaded();
        scannedLevel = -1;
        lastX = worldPosition.getX();
        lastZ = worldPosition.getZ();
        canProvideLiquid = false;
    }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); lastMode = Mode.values()[Math.min(nbt.getInt("lastMode"), Mode.values().length - 1)]; progress = nbt.getInt("progress"); }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putInt("lastMode", lastMode.ordinal()); nbt.putInt("progress", progress); }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        chargeTools();
        for (int i = 0; i < upgradeSlot.size(); i++) {
            ItemStack s = upgradeSlot.get(i);
            if (s.getItem() instanceof IUpgradeItem u && u.onTick(s, this)) setChanged();
        }
        if (work()) { setChanged(); setActive(true); }
        else setActive(false);
    }

    private void chargeTools() {
        if (!scannerSlot.isEmpty()) energy -= ElectricItem.manager.charge(scannerSlot.get(), energy, 2, false, false);
        if (!drillSlot.isEmpty()) energy -= ElectricItem.manager.charge(drillSlot.get(), energy, 1, false, false);
    }

    private BlockPos at(int x, int y, int z) { return new BlockPos(x, y, z); }
    private boolean isPipe(BlockState s) { return s.is(Ic2Blocks.MINING_PIPE.get()); }
    private boolean isTip(BlockState s) { return s.is(Ic2Blocks.MINING_PIPE_TIP.get()); }

    private boolean work() {
        int y = getOperationHeight();
        if (drillSlot.isEmpty()) return withdrawPipe(y);
        if (y < level.getMinBuildHeight()) return false;
        BlockState state = level.getBlockState(at(worldPosition.getX(), y, worldPosition.getZ()));
        if (!isTip(state)) return y > level.getMinBuildHeight() && digDown(y, false);
        MineResult result = mineLevel(y);
        return result == MineResult.Done ? digDown(y - 1, true) : result == MineResult.Working;
    }

    private int getOperationHeight() {
        for (int y = worldPosition.getY() - 1; y >= level.getMinBuildHeight(); y--)
            if (!isPipe(level.getBlockState(at(worldPosition.getX(), y, worldPosition.getZ())))) return y;
        return level.getMinBuildHeight() - 1;
    }

    private boolean withdrawPipe(int y) {
        if (lastMode != Mode.Withdraw) { lastMode = Mode.Withdraw; progress = 0; }
        if (y < level.getMinBuildHeight() || !isTip(level.getBlockState(at(worldPosition.getX(), y, worldPosition.getZ())))) y++;
        if (y == worldPosition.getY() || energy < 3) return false;
        if (progress < 20) { energy -= 3; progress++; }
        else { progress = 0; removePipe(y); }
        return true;
    }

    private void removePipe(int y) {
        BlockPos p = at(worldPosition.getX(), y, worldPosition.getZ());
        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        storeDrop(new ItemStack(Ic2Items.MINING_PIPE.get()));
        ItemStack filler = pipeSlot.consume(1, true, false);
        if (!filler.isEmpty() && !filler.is(Ic2Items.MINING_PIPE.get()) && filler.getItem() instanceof BlockItem bi) {
            BlockState fill = bi.getBlock().defaultBlockState();
            if (fill.canSurvive(level, p)) { pipeSlot.consume(1); level.setBlock(p, fill, 3); }
        }
    }

    private boolean digDown(int y, boolean removeTipAbove) {
        ItemStack pipe = pipeSlot.consume(1, true, false);
        if (pipe.isEmpty() || !pipe.is(Ic2Items.MINING_PIPE.get())) return false;
        int x = worldPosition.getX(), z = worldPosition.getZ();
        if (y < level.getMinBuildHeight()) {
            if (removeTipAbove) level.setBlock(at(x, y + 1, z), Ic2Blocks.MINING_PIPE.get().defaultBlockState(), 3);
            return false;
        }
        MineResult result = mineBlock(x, y, z);
        if (result == MineResult.Failed_Temp || result == MineResult.Failed_Perm) {
            if (removeTipAbove) level.setBlock(at(x, y + 1, z), Ic2Blocks.MINING_PIPE.get().defaultBlockState(), 3);
            return false;
        }
        if (result == MineResult.Done) {
            if (removeTipAbove) level.setBlock(at(x, y + 1, z), Ic2Blocks.MINING_PIPE.get().defaultBlockState(), 3);
            pipeSlot.consume(1);
            level.setBlock(at(x, y, z), Ic2Blocks.MINING_PIPE_TIP.get().defaultBlockState(), 3);
        }
        return true;
    }

    private MineResult mineLevel(int y) {
        if (scannerSlot.isEmpty()) return MineResult.Done;
        if (scannedLevel != y) scanRange = ((ItemScanner)scannerSlot.get().getItem()).startLayerScan(scannerSlot.get());
        if (scanRange <= 0) return MineResult.Failed_Temp;
        scannedLevel = y;
        int cx = worldPosition.getX(), cz = worldPosition.getZ();
        for (int x = cx - scanRange; x <= cx + scanRange; x++) {
            for (int z = cz - scanRange; z <= cz + scanRange; z++) {
                if (OreValues.isValuable(level.getBlockState(at(x, y, z))) && canMine(x, y, z)) {
                    MineResult result = mineTowards(x, y, z);
                    if (result == MineResult.Done) return MineResult.Working;
                    if (result != MineResult.Failed_Perm) return result;
                }
            }
        }
        return MineResult.Done;
    }

    /** Bresenham line from the shaft to the target; mines the first blocking block on it. */
    private MineResult mineTowards(int x, int y, int z) {
        int dx = Math.abs(x - worldPosition.getX()), sx = worldPosition.getX() < x ? 1 : -1;
        int dz = -Math.abs(z - worldPosition.getZ()), sz = worldPosition.getZ() < z ? 1 : -1;
        int err = dx + dz, cx = worldPosition.getX(), cz = worldPosition.getZ();
        while (cx != x || cz != z) {
            boolean isCurrentPos = cx == lastX && cz == lastZ;
            int e2 = 2 * err;
            if (e2 > dz) { err += dz; cx += sx; }
            else if (e2 < dx) { err += dx; cz += sz; }
            boolean blocking;
            if (isCurrentPos) blocking = true;
            else {
                BlockState s = level.getBlockState(at(cx, y, cz));
                blocking = !s.isAir() && (!(s.getBlock() instanceof LiquidBlock) || s.getFluidState().isSource());
            }
            if (blocking) {
                MineResult result = mineBlock(cx, y, cz);
                if (result == MineResult.Done) { lastX = cx; lastZ = cz; }
                return result;
            }
        }
        lastX = worldPosition.getX();
        lastZ = worldPosition.getZ();
        return MineResult.Done;
    }

    private MineResult mineBlock(int x, int y, int z) {
        BlockPos p = at(x, y, z);
        BlockState state = level.getBlockState(p);
        boolean air = state.isAir();
        if (!air) {
            if (state.getBlock() instanceof LiquidBlock) {
                if (state.getFluidState().isSource()) {
                    liquidPos = p;
                    canProvideLiquid = true;
                    return MineResult.Failed_Perm;
                }
            } else if (!canMine(x, y, z)) return MineResult.Failed_Perm;
        }
        canProvideLiquid = false;
        Mode mode;
        int energyPerTick, duration;
        if (air) { mode = Mode.MineAir; energyPerTick = 3; duration = 20; }
        else if (drillSlot.get().is(Ic2Items.MINING_DRILL.get())) { mode = Mode.MineDrill; energyPerTick = 6; duration = 200; }
        else { mode = Mode.MineDDrill; energyPerTick = 20; duration = 50; }
        if (lastMode != mode) { lastMode = mode; progress = 0; }
        if (progress < duration) {
            if (energy >= energyPerTick) { energy -= energyPerTick; progress++; return MineResult.Working; }
        } else if (air || harvestBlock(p, state)) {
            progress = 0;
            return MineResult.Done;
        }
        return MineResult.Failed_Temp;
    }

    private boolean harvestBlock(BlockPos p, BlockState state) {
        int energyCost = 2 * (worldPosition.getY() - p.getY());
        if (energy < energyCost) return false;
        ItemStack drill = drillSlot.get();
        if (!ElectricItem.manager.use(drill, drill.is(Ic2Items.MINING_DRILL.get()) ? 50 : 80, null)) return false;
        energy -= energyCost;
        BlockEntity be = level.getBlockEntity(p);
        for (ItemStack drop : Block.getDrops(state, (ServerLevel)level, p, be, null, drill)) storeDrop(drop);
        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        return true;
    }

    /** Drops go to the pipe slot (mining pipes) or the buffer; what does not fit is dropped on top of the miner. */
    private void storeDrop(ItemStack stack) {
        if (stack.is(Ic2Items.MINING_PIPE.get())) stack = insert(pipeSlot, stack);
        stack = insert(buffer, stack);
        if (!stack.isEmpty()) Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.2, worldPosition.getZ() + 0.5, stack);
    }

    private static ItemStack insert(InvSlot slot, ItemStack stack) {
        for (int i = 0; i < slot.size() && !stack.isEmpty(); i++) {
            ItemStack cur = slot.get(i);
            if (cur.isEmpty()) { slot.put(i, stack.copy()); return ItemStack.EMPTY; }
            if (ItemStack.isSameItemSameComponents(cur, stack)) {
                int n = Math.min(stack.getCount(), cur.getMaxStackSize() - cur.getCount());
                if (n > 0) { cur.grow(n); stack.shrink(n); }
            }
        }
        return stack;
    }

    public boolean canMine(int x, int y, int z) {
        BlockPos p = at(x, y, z);
        BlockState state = level.getBlockState(p);
        if (state.isAir()) return true;
        if (isPipe(state) || isTip(state) || state.is(Blocks.CHEST)) return false;
        if (state.getBlock() instanceof LiquidBlock) return isPumpConnected(p);
        if (state.getDestroySpeed(level, p) < 0) return false;
        if (!state.requiresCorrectToolForDrops() || state.is(Blocks.COBWEB)) return true;
        return !drillSlot.isEmpty() && drillSlot.get().isCorrectToolForDrops(state);
    }

    public boolean isPumpConnected(BlockPos target) {
        for (Direction d : Direction.values())
            if (ic2.core.util.Util.loadedBlockEntity(level, worldPosition.relative(d)) instanceof TileEntityPump pump && pump.pump(target, true, this) != null) return true;
        return false;
    }

    private boolean lastClientActive;
    @Override
    protected void updateEntityClient() {
        boolean active = getActive();
        if (active == lastClientActive) return;
        lastClientActive = active;
        if (audioSource == null) audioSource = AudioSource.create(this, Ic2Sounds.MINER_OP.get());
        if (audioSource != null) { if (active) audioSource.play(); else audioSource.stop(); }
    }

    @Override public void onUnloaded() { if (audioSource != null) { audioSource.remove(); audioSource = null; } super.onUnloaded(); }
    @Override public double getEnergy() { return energy; }
    @Override public boolean useEnergy(double amount) { if (energy >= amount) { energy -= amount; return true; } return false; }
    @Override public Set<UpgradableProperty> getUpgradableProperties() { return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerMiner(id, inv, this); }

    enum MineResult { Working, Done, Failed_Temp, Failed_Perm }
    enum Mode { None, Withdraw, MineAir, MineDrill, MineDDrill }
}
