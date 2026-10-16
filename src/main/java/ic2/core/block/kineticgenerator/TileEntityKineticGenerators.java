package ic2.core.block.kineticgenerator;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IKineticSource;
import ic2.api.item.IKineticRotor;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.WindSim;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;

public final class TileEntityKineticGenerators {
    private TileEntityKineticGenerators() {}

    /** Rotor generator base: rotor slot, free space check along the facing axis and a rotation synced for the renderer. */
    public static abstract class Rotor extends TileEntityInventory implements IKineticSource, IHasGui {
        public final InvSlotConsumable rotorSlot;
        protected final IKineticRotor.GearboxType gearbox;
        public float rotationSpeed;
        protected int crossSection, obstructedCrossSection;
        private float angle;
        private long lastcheck;

        protected Rotor(BlockEntityType<?> type, BlockPos pos, BlockState state, IKineticRotor.GearboxType gearbox) {
            super(type, pos, state);
            this.gearbox = gearbox;
            rotorSlot = new InvSlotConsumable(this, "rotorslot", InvSlot.Access.IO, 1, InvSlot.InvSide.ANY) {
                @Override public boolean accepts(ItemStack s) { return s.getItem() instanceof IKineticRotor r && r.isAcceptedType(s, gearbox); }
            };
        }

        protected IKineticRotor rotor() { ItemStack s = rotorSlot.get(); return s.getItem() instanceof IKineticRotor r ? r : null; }
        public int getRotorDiameter() { var r = rotor(); return r == null ? 0 : r.getDiameter(rotorSlot.get()); }
        public float getefficiency() { var r = rotor(); return r == null ? 0 : r.getEfficiency(rotorSlot.get()); }
        public ResourceLocation getRotorRenderTexture() { var r = rotor(); return r == null ? ic2.core.IC2.id("textures/item/rotor/rotor_wood_model.png") : r.getRotorRenderTexture(rotorSlot.get()); }
        public boolean checkrotor() { return !rotorSlot.isEmpty(); }
        public boolean rotorspace() { return checkSpace(1, true) == 0; }
        public String getRotorhealth() {
            ItemStack s = rotorSlot.get();
            return s.isEmpty() ? "" : Component.translatable("ic2.WindKineticGenerator.gui.rotorhealth", (int)(100 - (float)s.getDamageValue() / s.getMaxDamage() * 100)).getString();
        }

        protected abstract boolean isFree(BlockPos pos);

        /** Count of blocked cells in the rotor plane (or -1 when another generator of this kind is in range). */
        public int checkSpace(int length, boolean onlyrotor) {
            int box = getRotorDiameter() / 2, lentemp = 0;
            if (onlyrotor) { length = 1; lentemp = length + 1; } else box *= 2;
            Direction fwd = getFacing(), right = fwd.getAxis().isHorizontal() ? fwd.getCounterClockWise() : Direction.EAST;
            int ret = 0;
            for (int up = -box; up <= box; up++) for (int r = -box; r <= box; r++) {
                boolean occupied = false;
                for (int f = lentemp - length; f <= length; f++) {
                    BlockPos p = worldPosition.relative(fwd, f).relative(right, r).above(up);
                    if (!isFree(p)) {
                        occupied = true;
                        if ((up != 0 || r != 0 || f != 0) && !onlyrotor && level.getBlockEntity(p) != null && level.getBlockEntity(p).getClass() == getClass()) return -1;
                    }
                }
                if (occupied) ret++;
            }
            return ret;
        }

        protected void damageRotor(int amount) {
            ItemStack s = rotorSlot.get();
            if (s.isEmpty() || amount <= 0) return;
            s.setDamageValue(s.getDamageValue() + amount);
            if (s.getDamageValue() >= s.getMaxDamage()) rotorSlot.put(ItemStack.EMPTY);
        }

        protected void setRotationSpeed(float speed) { if (rotationSpeed != speed) { rotationSpeed = speed; sendClientUpdate(); } }

        @Override public void setActive(boolean active) { if (active != getActive()) sendClientUpdate(); super.setActive(active); }
        @Override public void setChanged() { super.setChanged(); }

        @Override protected void writeClientData(CompoundTag nbt, HolderLookup.Provider r) { nbt.putFloat("speed", rotationSpeed); nbt.put("rotor", rotorSlot.get().saveOptional(r)); }
        @Override protected void readClientData(CompoundTag nbt, HolderLookup.Provider r) { rotationSpeed = nbt.getFloat("speed"); rotorSlot.put(ItemStack.parseOptional(r, nbt.getCompound("rotor"))); }

        public float getAngle(float rotationModifier) {
            long now = System.currentTimeMillis();
            if (rotationSpeed != 0) angle = (angle + (now - lastcheck) * rotationSpeed * rotationModifier) % 360;
            lastcheck = now;
            return angle;
        }

        @Override public boolean wrenchCanSetFacing(Player player, Direction side) { return side.getAxis().isHorizontal() && getFacing() != side; }
        public abstract int getKuOutput();
        @Override public int maxrequestkineticenergyTick(Direction from) { return getKuOutput(); }
        @Override public int requestkineticenergy(Direction from, int amount) { return from.getOpposite() == getFacing() ? Math.min(amount, getKuOutput()) : 0; }
    }

    /** Wind: output = wind x10 x rotor efficiency between the rotor's min and max wind; overloading wears the rotor 4x. */
    public static class Wind extends Rotor {
        public static final float outputModifier = 10;
        public double windStrength;
        private int updateTicker = (int)(Math.random() * 32);

        public Wind(BlockPos pos, BlockState state) { super(Ic2BlockEntities.WIND_KINETIC_GENERATOR.get(), pos, state, IKineticRotor.GearboxType.WIND); }

        @Override protected boolean isFree(BlockPos pos) { return level.isEmptyBlock(pos); }

        @Override
        protected void updateEntityServer() {
            super.updateEntityServer();
            if (updateTicker++ % 32 != 0) return;
            setActive(!rotorSlot.isEmpty() && checkSpace(1, true) == 0);
            if (getActive()) {
                crossSection = getRotorDiameter() / 2 * 2 * 2 + 1;
                crossSection *= crossSection;
                obstructedCrossSection = checkSpace(getRotorDiameter() * 3, false);
                if (obstructedCrossSection > 0 && obstructedCrossSection <= (getRotorDiameter() + 1) / 2) obstructedCrossSection = 0;
                if (obstructedCrossSection < 0) { windStrength = 0; setRotationSpeed(0); }
                else {
                    windStrength = calcWindStrength();
                    setRotationSpeed((float)Util.limit((windStrength - getMinWindStrength()) / getMaxWindStrength(), 0, 2));
                    if (windStrength >= getMinWindStrength()) damageRotor(windStrength <= getMaxWindStrength() ? 1 : 4);
                }
            } else setRotationSpeed(0);
            setChanged();
        }

        public double calcWindStrength() {
            double w = WindSim.get(level).getWindAt(worldPosition.getY()) * (1 - Math.pow((double)obstructedCrossSection / crossSection, 2));
            return Math.max(0, w);
        }

        public int getMinWindStrength() { var r = rotor(); return r == null ? 0 : r.getMinWindStrength(rotorSlot.get()); }
        public int getMaxWindStrength() { var r = rotor(); return r == null ? 0 : r.getMaxWindStrength(rotorSlot.get()); }
        public boolean guiisoverload() { return windStrength > getMaxWindStrength(); }
        public boolean guiisminWindStrength() { return windStrength >= getMinWindStrength(); }
        @Override public int getKuOutput() { return windStrength >= getMinWindStrength() && getActive() ? (int)(windStrength * outputModifier * getefficiency()) : 0; }
        @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerKineticGenerators.Wind(id, inv, this); }
    }

    /** Water: in oceans driven by the tide (day time sine), in rivers by a steady flow; the rotor must be fully submerged. */
    public static class Water extends Rotor {
        private static final float outputModifier = 0.2f;
        public int type, waterFlow;
        private boolean firsttick = true, rightFacing;
        private int distanceToNormalBiome, updateTicker = (int)(Math.random() * 20);

        public Water(BlockPos pos, BlockState state) { super(Ic2BlockEntities.WATER_KINETIC_GENERATOR.get(), pos, state, IKineticRotor.GearboxType.WATER); }

        @Override protected boolean isFree(BlockPos pos) { return level.getBlockState(pos).is(Blocks.WATER); }

        private boolean isValidBiome(BlockPos pos) { var b = level.getBiome(pos); return b.is(Biomes.OCEAN) || b.is(Biomes.DEEP_OCEAN) || b.is(Biomes.RIVER); }

        public void updateSeaInfo() {
            Direction facing = getFacing();
            for (int i = 1; i < 200; i++) {
                if (!isValidBiome(worldPosition.relative(facing, i))) { distanceToNormalBiome = i; rightFacing = true; return; }
                if (!isValidBiome(worldPosition.relative(facing, -i))) { distanceToNormalBiome = i; rightFacing = false; return; }
            }
            distanceToNormalBiome = 200;
            rightFacing = true;
        }

        @Override
        protected void updateEntityServer() {
            super.updateEntityServer();
            if (firsttick) { updateSeaInfo(); firsttick = false; }
            if (updateTicker++ % 20 != 0) return;
            if (type == 0) {
                var biome = level.getBiome(worldPosition);
                type = biome.is(BiomeTags.IS_OCEAN) ? 1 : biome.is(BiomeTags.IS_RIVER) ? 2 : -1;
            }
            if (type == -1) return;
            setActive(!rotorSlot.isEmpty() && checkSpace(1, true) == 0);
            if (!getActive()) return;
            crossSection = getRotorDiameter() / 2 * 2 * 2 + 1;
            crossSection *= crossSection;
            obstructedCrossSection = checkSpace(getRotorDiameter() * 3, false);
            if (obstructedCrossSection > 0 && obstructedCrossSection <= (getRotorDiameter() + 1) / 2) obstructedCrossSection = 0;
            int rotordamage = 0;
            if (obstructedCrossSection < 0) { setRotationSpeed(0); waterFlow = 0; }
            else if (type == 1) {
                float diff = (float)Math.sin(level.getDayTime() * Math.PI / 6000);
                diff *= Math.abs(diff);
                float speed = (float)(diff * distanceToNormalBiome / 100f * (1 - Math.pow((double)obstructedCrossSection / crossSection, 2)));
                waterFlow = (int)(speed * 3000);
                setRotationSpeed(rightFacing ? -speed : speed);
                waterFlow = (int)(waterFlow * getefficiency());
                rotordamage = 2;
            } else {
                float speed = Math.max(Math.min(distanceToNormalBiome, 20), 50) / 50;
                waterFlow = (int)(speed * 1000);
                setRotationSpeed(getFacing() == Direction.EAST || getFacing() == Direction.NORTH ? -speed : speed);
                waterFlow = (int)(waterFlow * getefficiency() * (1 - 0.3f * level.random.nextFloat() - 0.1f * ((float)obstructedCrossSection / crossSection)));
                rotordamage = 1;
            }
            damageRotor(rotordamage);
            setChanged();
        }

        @Override public int getKuOutput() { return getActive() ? (int)Math.abs(waterFlow * outputModifier) : 0; }
        @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerKineticGenerators.Water(id, inv, this); }
    }

    /** Electric motor gearbox: 4 kU per EU, 100 kU/t per motor (10 motors), 10000 EU buffer, input on all sides but the front. */
    public static class Electric extends TileEntityInventory implements IKineticSource, IHasGui, IEnergySink {
        public final InvSlotConsumable slotMotor = new InvSlotConsumable(this, "slotMotor", 10) { @Override public boolean accepts(ItemStack s) { return s.is(Ic2Items.get("electric_motor")); } };
        public final InvSlotDischarge dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, 4);
        private final float kuPerEU = 4;
        public double ku, energy;
        public final int maxKU = 1000;
        private boolean addedToEnergyNet;

        public Electric(BlockPos pos, BlockState state) { super(Ic2BlockEntities.ELECTRIC_KINETIC_GENERATOR.get(), pos, state); slotMotor.setStackSizeLimit(1); }

        public int getMaxKU() { int c = 0; for (int i = 0; i < slotMotor.size(); i++) if (!slotMotor.get(i).isEmpty()) c += 100; return c; }
        public int getMaxKUForGUI() { return 1000; }
        public float getChargeLevel() { return (float)Math.min(1, energy / 10000); }

        @Override
        protected void updateEntityServer() {
            super.updateEntityServer();
            if (10000 - energy >= 1 && !dischargeSlot.isEmpty()) energy += dischargeSlot.discharge(10000 - energy, false);
            boolean newActive = false;
            if (1000 - ku > 1) {
                double max = Math.min(1000 - ku, energy * kuPerEU);
                energy -= max / kuPerEU;
                ku += max;
                if (max > 0) { setChanged(); newActive = true; }
            }
            if (getActive() != newActive) setActive(newActive);
        }

        @Override public int maxrequestkineticenergyTick(Direction from) { return from != getFacing() ? 0 : (int)Math.min(getMaxKU(), ku); }
        @Override public int requestkineticenergy(Direction from, int amount) { int out = Math.min(amount, maxrequestkineticenergyTick(from)); ku -= out; setChanged(); return out; }
        @Override public double getDemandedEnergy() { return 10000 - energy; }
        @Override public int getSinkTier() { return 4; }
        @Override public double injectEnergy(Direction from, double amount, double voltage) { if (energy >= 10000) return amount; energy += amount; return 0; }
        @Override public boolean acceptsEnergyFrom(BlockEntity emitter, Direction dir) { return dir != getFacing(); }
        @Override public void setFacing(Direction facing) { if (addedToEnergyNet) EnergyNet.instance.removeTile(this); super.setFacing(facing); if (addedToEnergyNet) EnergyNet.instance.addTile(this); }
        @Override public boolean wrenchCanSetFacing(Player player, Direction side) { return side != getFacing(); }
        @Override public void onLoaded() { super.onLoaded(); EnergyNet.instance.addTile(this); addedToEnergyNet = true; }
        @Override public void onUnloaded() { if (addedToEnergyNet && level != null && !level.isClientSide) { EnergyNet.instance.removeTile(this); addedToEnergyNet = false; } super.onUnloaded(); }
        @Override public void readFromNBT(CompoundTag nbt, HolderLookup.Provider r) { super.readFromNBT(nbt, r); energy = nbt.getDouble("energy"); ku = nbt.getDouble("ku"); }
        @Override public void writeToNBT(CompoundTag nbt, HolderLookup.Provider r) { super.writeToNBT(nbt, r); nbt.putDouble("energy", energy); nbt.putDouble("ku", ku); }
        @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ContainerKineticGenerators.Electric(id, inv, this); }
    }

    /** Hand crank: right click adds 400 kU (20 for fake players), at most 10 clicks per tick, costs hunger. */
    public static class Manual extends TileEntityBlock implements IKineticSource {
        public int clicks, currentKU;
        public final int maxKU = 1000;

        public Manual(BlockPos pos, BlockState state) { super(Ic2BlockEntities.MANUAL_KINETIC_GENERATOR.get(), pos, state); }

        @Override protected void updateEntityServer() { super.updateEntityServer(); clicks = 0; }

        @Override
        public boolean onActivated(Player player) {
            if (player.getFoodData().getFoodLevel() <= 6) return false;
            if (!(player instanceof ServerPlayer)) return true;
            if (clicks >= 10) return true;
            currentKU = Math.min(1000, currentKU + (player instanceof FakePlayer ? 20 : 400));
            player.causeFoodExhaustion(0.25f);
            clicks++;
            return true;
        }

        @Override public int maxrequestkineticenergyTick(Direction from) { return currentKU; }
        @Override public int requestkineticenergy(Direction from, int amount) { int max = Math.min(currentKU, amount); currentKU -= max; return max; }
    }
}
