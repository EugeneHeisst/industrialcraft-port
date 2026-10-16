package ic2.core.block.reactor;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IMetaDelegate;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import ic2.core.ContainerBase;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.fluid.Ic2Fluids;
import ic2.core.item.reactor.ItemReactorHeatStorage;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2Sounds;
import ic2.core.util.Ic2DamageSources;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.apache.commons.lang3.mutable.MutableObject;

/**
 * Nuclear reactor: every 20 ticks the components run a heat pass and an energy pass; output * 5 EU/t is offered from the
 * reactor and its chambers. Inside a complete 5x5x5 pressure vessel it runs fluid cooled, heating coolant to hot coolant.
 */
public class TileEntityNuclearReactor extends TileEntityInventory implements IHasGui, IReactor, IEnergySource, IMetaDelegate, IFluidHandler {
    public float output;
    private int updateTicker = java.util.concurrent.ThreadLocalRandom.current().nextInt(20);
    public int heat, maxHeat = 10000;
    public float hem = 1;
    private int emitHeatBuffer;
    public int emitHeat;
    private boolean redstone, fluidCooled;
    private boolean addedToEnergyNet;
    private List<BlockEntity> subTiles;
    private AudioSource audioMain, audioGeiger;
    private float lastOutput;
    public final FluidTank inputTank = new FluidTank(10000), outputTank = new FluidTank(10000);
    public final InvSlotReactor reactorSlot = new InvSlotReactor(this, "reactor");
    public final InvSlotConsumableLiquid coolantInputSlot = new InvSlotConsumableLiquid(this, "coolantinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, Ic2Fluids.COOLANT.get());
    public final InvSlotConsumableLiquid hotCoolantInputSlot = new InvSlotConsumableLiquid(this, "hotcoolinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, Ic2Fluids.HOT_COOLANT.get());
    public final InvSlotOutput coolantOutputSlot = new InvSlotOutput(this, "coolantoutputSlot", 1), hotCoolantOutputSlot = new InvSlotOutput(this, "hotcoolantoutputSlot", 1);
    /** balance/energy/FluidReactor/outputModifier (1.0) * 2 */
    private static final float HU_OUTPUT_MODIFIER = 2;

    public TileEntityNuclearReactor(BlockPos pos, BlockState state) {
        super(Ic2BlockEntities.NUCLEAR_REACTOR.get(), pos, state);
        hotCoolantInputSlot.fillMode = true;
    }

    @Override public void onLoaded() { super.onLoaded(); if (!level.isClientSide && !fluidCooled) { EnergyNet.instance.addTile(this); addedToEnergyNet = true; } }
    @Override public void onUnloaded() {
        if (audioMain != null) { audioMain.remove(); audioMain = null; }
        if (audioGeiger != null) { audioGeiger.remove(); audioGeiger = null; }
        if (addedToEnergyNet && level != null && !level.isClientSide) { EnergyNet.instance.removeTile(this); addedToEnergyNet = false; }
        super.onUnloaded();
    }

    public int gaugeHeatScaled(int i) { return i * heat / (maxHeat / 100 * 85); }

    @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) {
        super.readFromNBT(nbt, r);
        heat = nbt.getInt("heat");
        inputTank.readFromNBT(r, nbt.getCompound("inputTank"));
        outputTank.readFromNBT(r, nbt.getCompound("outputTank"));
        output = nbt.getShort("output");
    }
    @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) {
        super.writeToNBT(nbt, r);
        nbt.put("inputTank", inputTank.writeToNBT(r, new CompoundTag()));
        nbt.put("outputTank", outputTank.writeToNBT(r, new CompoundTag()));
        nbt.putInt("heat", heat);
        nbt.putShort("output", (short)output);
    }
    @Override protected void writeClientData(CompoundTag nbt, HolderLookup.Provider r) { nbt.putFloat("output", output); nbt.putInt("heat", heat); }
    @Override protected void readClientData(CompoundTag nbt, HolderLookup.Provider r) { output = nbt.getFloat("output"); heat = nbt.getInt("heat"); }

    // ---- energy
    @Override public void setRedstoneSignal(boolean redstone) { this.redstone = redstone; }
    @Override public void drawEnergy(double amount) {}
    @Override public boolean emitsEnergyTo(BlockEntity receiver, Direction direction) { return true; }
    @Override public double getOfferedEnergy() { return output * 5; }
    @Override public int getSourceTier() { return 4; }
    @Override public double getReactorEUEnergyOutput() { return getOfferedEnergy(); }

    @Override
    public List<BlockEntity> getSubTiles() {
        if (subTiles == null) {
            subTiles = new ArrayList<>();
            subTiles.add(this);
            for (Direction d : Direction.values()) if (loaded(worldPosition.relative(d)) instanceof TileEntityReactorChamber c && !c.isRemoved()) subTiles.add(c);
        }
        return subTiles;
    }

    /** Neighbour block entity without loading its chunk (chunk unload at shutdown would otherwise loop). */
    BlockEntity loaded(BlockPos p) { return level != null && level.isLoaded(p) ? level.getBlockEntity(p) : null; }

    public void refreshChambers() {
        if (addedToEnergyNet) EnergyNet.instance.removeTile(this);
        subTiles = null;
        if (addedToEnergyNet) EnergyNet.instance.addTile(this);
    }

    // ---- simulation
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (updateTicker++ % getTickRate() != 0) return;
        if (!level.isAreaLoaded(worldPosition, 2)) { output = 0; return; }
        if (getReactorSize() == 9) {
            boolean ready = readyForPressurizedReactor();
            if (ready != fluidCooled) {
                if (ready) {
                    if (addedToEnergyNet) { EnergyNet.instance.removeTile(this); addedToEnergyNet = false; }
                    moveFluidInWorld(false);
                } else {
                    if (!addedToEnergyNet) { EnergyNet.instance.addTile(this); addedToEnergyNet = true; }
                    moveFluidInWorld(true);
                }
                fluidCooled = ready;
            }
        } else if (fluidCooled) {
            fluidCooled = false;
            if (!addedToEnergyNet) { EnergyNet.instance.addTile(this); addedToEnergyNet = true; }
        }
        dropAllUnfittingStuff();
        output = 0;
        maxHeat = 10000;
        hem = 1;
        processChambers();
        if (fluidCooled) {
            processFluidSlots();
            int huOutput = (int)(HU_OUTPUT_MODIFIER * emitHeatBuffer);
            int room = outputTank.getCapacity() - outputTank.getFluidAmount();
            emitHeat = 0;
            if (room > 0) {
                FluidStack drain = inputTank.drain(Math.min(huOutput, room), IFluidHandler.FluidAction.SIMULATE);
                if (!drain.isEmpty()) {
                    emitHeat = drain.getAmount();
                    huOutput -= inputTank.drain(drain.getAmount(), IFluidHandler.FluidAction.EXECUTE).getAmount();
                    outputTank.fill(new FluidStack(Ic2Fluids.HOT_COOLANT.get(), drain.getAmount()), IFluidHandler.FluidAction.EXECUTE);
                }
            }
            addHeat(huOutput / 2);
        }
        emitHeatBuffer = 0;
        if (calculateHeatEffects()) return;
        setActive(heat >= 1000 || output > 0);
        setChanged();
        sendClientUpdate();
    }

    private void processFluidSlots() {
        MutableObject<ItemStack> out = new MutableObject<>();
        if (!coolantInputSlot.isEmpty() && coolantInputSlot.transferToTank(inputTank, out, true) && (out.getValue() == null || coolantOutputSlot.canAdd(out.getValue()))) {
            coolantInputSlot.transferToTank(inputTank, out, false);
            if (out.getValue() != null) coolantOutputSlot.add(out.getValue());
        }
        out.setValue(null);
        if (!hotCoolantInputSlot.isEmpty() && hotCoolantInputSlot.transferFromTank(outputTank, out, true) && (out.getValue() == null || hotCoolantOutputSlot.canAdd(out.getValue()))) {
            hotCoolantInputSlot.transferFromTank(outputTank, out, false);
            if (out.getValue() != null) hotCoolantOutputSlot.add(out.getValue());
        }
    }

    public void dropAllUnfittingStuff() {
        int size = getReactorSize();
        for (int y = 0; y < 6; y++) for (int x = 0; x < 9; x++) {
            ItemStack s = reactorSlot.get(x, y);
            if (s.isEmpty() || x < size && isUsefulItem(s, false)) continue;
            reactorSlot.put(x, y, ItemStack.EMPTY);
            eject(s);
        }
    }

    public boolean isUsefulItem(ItemStack stack, boolean forInsertion) {
        Item item = stack.getItem();
        if (forInsertion && fluidCooled && item.getClass() == ItemReactorHeatStorage.class && ((ItemReactorHeatStorage)item).getCustomDamage(stack) > 0) return false;
        if (item instanceof IReactorComponent) return true;
        for (String id : DEPLETED) if (Ic2Items.has(id) && item == Ic2Items.get(id)) return true;
        return false;
    }
    private static final String[] DEPLETED = {"tritium_cell", "depleted_uranium_fuel_rod", "depleted_dual_uranium_fuel_rod", "depleted_quad_uranium_fuel_rod", "depleted_mox_fuel_rod", "depleted_dual_mox_fuel_rod", "depleted_quad_mox_fuel_rod"};

    public void eject(ItemStack drop) { if (!drop.isEmpty()) Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, drop); }

    public boolean calculateHeatEffects() {
        if (heat < 4000) return false;
        float power = (float)heat / maxHeat;
        if (power >= 1) { explode(); return true; }
        var rnd = level.random;
        if (power >= 0.85f && rnd.nextFloat() <= 0.2f * hem) {
            BlockPos p = randCoord(2);
            if (p != null) {
                BlockState s = level.getBlockState(p);
                if (s.isAir()) level.setBlock(p, BaseFireBlock.getState(level, p), 3);
                else if (s.getDestroySpeed(level, p) >= 0 && level.getBlockEntity(p) == null) {
                    boolean mineral = s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(BlockTags.MINEABLE_WITH_PICKAXE) || s.is(BlockTags.MINEABLE_WITH_SHOVEL) || s.getBlock() instanceof LiquidBlock;
                    level.setBlock(p, mineral ? Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, 15) : BaseFireBlock.getState(level, p), 3);
                }
            }
        }
        if (power >= 0.7f)
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(worldPosition.getX() - 3, worldPosition.getY() - 3, worldPosition.getZ() - 3, worldPosition.getX() + 4, worldPosition.getY() + 4, worldPosition.getZ() + 4)))
                e.hurt(Ic2DamageSources.get(level, Ic2DamageSources.RADIATION), (int)(rnd.nextInt(4) * hem));
        if (power >= 0.5f && rnd.nextFloat() <= hem) {
            BlockPos p = randCoord(2);
            if (p != null && level.getBlockState(p).is(Blocks.WATER)) level.removeBlock(p, false);
        }
        if (power >= 0.4f && rnd.nextFloat() <= hem) {
            BlockPos p = randCoord(2);
            if (p != null && level.getBlockEntity(p) == null) {
                BlockState s = level.getBlockState(p);
                if (s.ignitedByLava() && !s.isAir()) level.setBlock(p, BaseFireBlock.getState(level, p), 3);
            }
        }
        return false;
    }

    private BlockPos randCoord(int r) {
        var rnd = level.random;
        BlockPos p = worldPosition.offset(rnd.nextInt(2 * r + 1) - r, rnd.nextInt(2 * r + 1) - r, rnd.nextInt(2 * r + 1) - r);
        return p.equals(worldPosition) ? null : p;
    }

    public void processChambers() {
        int size = getReactorSize();
        for (int pass = 0; pass < 2; pass++)
            for (int y = 0; y < 6; y++)
                for (int x = 0; x < size; x++) {
                    ItemStack s = reactorSlot.get(x, y);
                    if (!s.isEmpty() && s.getItem() instanceof IReactorComponent c) c.processChamber(this, s, x, y, pass == 0);
                }
    }

    @Override public boolean produceEnergy() { return receiveRedstone(); }
    public boolean receiveRedstone() { return level.hasNeighborSignal(worldPosition) || redstone; }

    public short getReactorSize() {
        if (level == null) return 9;
        short cols = 3;
        for (Direction d : Direction.values()) if (loaded(worldPosition.relative(d)) instanceof TileEntityReactorChamber) cols++;
        return cols;
    }

    @Override public int getTickRate() { return 20; }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerNuclearReactor(id, inv, this); }
    @Override public float getWrenchDropRate() { return 0.8f; }
    @Override public int getMaxStackSize() { return 1; }

    @Override
    protected void updateEntityClient() {
        if (output > 0) {
            if (lastOutput <= 0) { if (audioMain == null) audioMain = AudioSource.create(this, Ic2Sounds.REACTOR_LOOP.get()); if (audioMain != null) audioMain.play(); }
            int band = output < 40 ? 0 : output < 80 ? 1 : 2, lastBand = lastOutput <= 0 ? -1 : lastOutput < 40 ? 0 : lastOutput < 80 ? 1 : 2;
            if (band != lastBand) {
                if (audioGeiger != null) audioGeiger.remove();
                audioGeiger = AudioSource.create(this, (band == 0 ? Ic2Sounds.GEIGER_LOW : band == 1 ? Ic2Sounds.GEIGER_MED : Ic2Sounds.GEIGER_HIGH).get());
                if (audioGeiger != null) audioGeiger.play();
            }
        } else if (lastOutput > 0) {
            if (audioMain != null) audioMain.stop();
            if (audioGeiger != null) audioGeiger.stop();
        }
        lastOutput = output;
    }

    // ---- IReactor
    @Override public BlockPos getPosition() { return worldPosition; }
    @Override public Level getWorld() { return level; }
    @Override public int getHeat() { return heat; }
    @Override public void setHeat(int heat) { this.heat = heat; }
    @Override public int addHeat(int amount) { return heat += amount; }
    @Override public ItemStack getItemAt(int x, int y) { return x >= 0 && x < getReactorSize() && y >= 0 && y < 6 ? reactorSlot.get(x, y) : ItemStack.EMPTY; }
    @Override public void setItemAt(int x, int y, ItemStack stack) { if (x >= 0 && x < getReactorSize() && y >= 0 && y < 6) reactorSlot.put(x, y, stack); }
    @Override public void addEmitHeat(int heat) { emitHeatBuffer += heat; }
    @Override public int getMaxHeat() { return maxHeat; }
    @Override public void setMaxHeat(int maxHeat) { this.maxHeat = maxHeat; }
    @Override public float getHeatEffectModifier() { return hem; }
    @Override public void setHeatEffectModifier(float hem) { this.hem = hem; }
    @Override public float getReactorEnergyOutput() { return output; }
    @Override public float addOutput(float energy) { return output += energy; }
    @Override public boolean isFluidCooled() { return fluidCooled; }
    public void setFluidCooledClient(boolean v) { fluidCooled = v; }

    /** protection/reactorExplosionPowerLimit of the original config */
    public static final float EXPLOSION_POWER_LIMIT = 45;

    @Override
    public void explode() {
        float boomPower = 10, boomMod = 1;
        for (int i = 0; i < reactorSlot.size(); i++) {
            ItemStack s = reactorSlot.get(i);
            if (!s.isEmpty() && s.getItem() instanceof IReactorComponent c) {
                float f = c.influenceExplosion(this, s);
                if (f > 0 && f < 1) boomMod *= f; else boomPower += f;
            }
            reactorSlot.put(i, ItemStack.EMPTY);
        }
        boomPower *= hem * boomMod;
        IC2.LOG.info("Nuclear Reactor at {} melted (raw explosion power {})", worldPosition, boomPower);
        boomPower = Math.min(boomPower, EXPLOSION_POWER_LIMIT);
        for (Direction d : Direction.values()) if (ic2.core.util.Util.loadedBlockEntity(level, worldPosition.relative(d)) instanceof TileEntityReactorChamber) level.removeBlock(worldPosition.relative(d), false);
        level.removeBlock(worldPosition, false);
        new ExplosionIC2(level, null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), boomPower, 0.01f, ExplosionIC2.Type.Nuclear, null, 0).doExplosion();
    }

    // ---- pressurized (fluid cooled) mode
    private static boolean isVesselWall(Block b) {
        return b == Ic2Blocks.REACTOR_VESSEL.get() || b == Ic2Blocks.REACTOR_ACCESS_HATCH.get() || b == Ic2Blocks.REACTOR_REDSTONE_PORT.get() || b == Ic2Blocks.REACTOR_FLUID_PORT.get();
    }

    private Block surrounding(int dx, int dy, int dz) {
        BlockPos p = worldPosition.offset(dx, dy, dz);
        BlockState s = level.getBlockState(p);
        if (s.isAir()) return Blocks.AIR;
        if ((s.is(Ic2Fluids.COOLANT.block.get()) || s.is(Ic2Fluids.HOT_COOLANT.block.get())) && !s.getFluidState().isSource()) return Blocks.AIR;
        return s.getBlock();
    }

    private boolean readyForPressurizedReactor() {
        Block coolant = Ic2Fluids.COOLANT.block.get(), hot = Ic2Fluids.HOT_COOLANT.block.get();
        for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++) for (int z = -1; z <= 1; z++) {
            Block b = surrounding(x, y, z);
            if (!(b == Ic2Blocks.NUCLEAR_REACTOR.get() || b == Ic2Blocks.REACTOR_CHAMBER.get() || b == coolant || b == hot || b == Blocks.AIR)) return false;
        }
        for (int a = -2; a <= 2; a++) for (int b = -2; b <= 2; b++) {
            if (!isVesselWall(surrounding(a, 2, b)) || !isVesselWall(surrounding(a, -2, b)) || !isVesselWall(surrounding(-2, a, b)) || !isVesselWall(surrounding(2, a, b))
                || !isVesselWall(surrounding(b, a, -2)) || !isVesselWall(surrounding(b, a, 2))) return false;
        }
        return true;
    }

    private void moveFluidInWorld(boolean out) {
        Block coolant = Ic2Fluids.COOLANT.block.get(), hot = Ic2Fluids.HOT_COOLANT.block.get();
        for (int y = -1; y <= 1; y++) for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            BlockPos p = worldPosition.offset(x, y, z);
            Block b = surrounding(x, y, z);
            if (out) {
                if (b != Blocks.AIR) continue;
                if (inputTank.getFluidAmount() >= 1000) { level.setBlock(p, coolant.defaultBlockState(), 3); inputTank.drain(1000, IFluidHandler.FluidAction.EXECUTE); }
                else if (outputTank.getFluidAmount() >= 1000) { level.setBlock(p, hot.defaultBlockState(), 3); outputTank.drain(1000, IFluidHandler.FluidAction.EXECUTE); }
            } else if (b == coolant) { level.setBlock(p, Blocks.AIR.defaultBlockState(), 3); inputTank.fill(new FluidStack(Ic2Fluids.COOLANT.get(), 1000), IFluidHandler.FluidAction.EXECUTE); }
            else if (b == hot) { level.setBlock(p, Blocks.AIR.defaultBlockState(), 3); outputTank.fill(new FluidStack(Ic2Fluids.HOT_COOLANT.get(), 1000), IFluidHandler.FluidAction.EXECUTE); }
        }
        if (out) {
            if (inputTank.getFluidAmount() < 1000) inputTank.setFluid(FluidStack.EMPTY);
            if (outputTank.getFluidAmount() < 1000) outputTank.setFluid(FluidStack.EMPTY);
        }
    }

    public int gaugeLiquidScaled(int i, int tank) { FluidTank t = tank == 0 ? inputTank : outputTank; return t.getFluidAmount() <= 0 ? 0 : t.getFluidAmount() * i / t.getCapacity(); }

    // ---- fluid handler: coolant in, hot coolant out, only when fluid cooled
    @Override public int getTanks() { return 2; }
    @Override public FluidStack getFluidInTank(int tank) { return (tank == 0 ? inputTank : outputTank).getFluid(); }
    @Override public int getTankCapacity(int tank) { return 10000; }
    @Override public boolean isFluidValid(int tank, FluidStack stack) { return tank == 0 && stack.getFluid() == Ic2Fluids.COOLANT.get(); }
    @Override public int fill(FluidStack resource, FluidAction action) { return fluidCooled && resource.getFluid() == Ic2Fluids.COOLANT.get() ? inputTank.fill(resource, action) : 0; }
    @Override public FluidStack drain(FluidStack resource, FluidAction action) { return fluidCooled && FluidStack.isSameFluidSameComponents(resource, outputTank.getFluid()) ? outputTank.drain(resource.getAmount(), action) : FluidStack.EMPTY; }
    @Override public FluidStack drain(int maxDrain, FluidAction action) { return fluidCooled ? outputTank.drain(maxDrain, action) : FluidStack.EMPTY; }
}
