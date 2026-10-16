package ic2.core.block.generator.tileentity;

import ic2.core.ContainerBase;
import ic2.core.block.generator.container.ContainerWaterGenerator;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/** Water mill: water containers give 500 ticks of 1-2 EU/t; otherwise surrounding water blocks feed a micro storage. */
public class TileEntityWaterGenerator extends TileEntityBaseGenerator {
    public final float waterbasevalue = 1;
    public int ticker, water, microStorage;
    public int maxWater = 2000;
    public final InvSlotConsumableLiquid fuelSlot = new InvSlotConsumableLiquid(this, "fuel", InvSlot.Access.NONE, 1, InvSlot.InvSide.TOP, Fluids.WATER);

    public TileEntityWaterGenerator(BlockPos pos, BlockState state) {
        super(Ic2BlockEntities.WATER_MILL.get(), pos, state, 2, 1, 4);
        ticker = (int)(Math.random() * tickRate());
    }

    @Override public void onLoaded() { super.onLoaded(); updateWaterCount(); }
    @Override public int gaugeFuelScaled(int i) { return fuel <= 0 ? 0 : fuel * i / maxWater; }

    @Override
    public boolean gainFuel() {
        if (fuel + 500 > maxWater) return false;
        if (!fuelSlot.isEmpty()) {
            boolean container = fuelSlot.get().hasCraftingRemainingItem();
            ItemStack liquid = fuelSlot.consume(1);
            if (liquid.isEmpty()) return false;
            fuel += 500;
            production = container ? 1 : 2;
            return true;
        }
        if (fuel <= 0) {
            flowPower();
            production = microStorage / 100;
            microStorage -= production * 100;
            if (production > 0) { fuel++; return true; }
        }
        return false;
    }

    @Override public boolean needsFuel() { return fuel <= maxWater; }

    public void flowPower() {
        if (ticker++ % tickRate() == 0) updateWaterCount();
        water = Math.round(water * waterbasevalue);
        if (water > 0) microStorage += water;
    }

    public void updateWaterCount() {
        int count = 0;
        for (BlockPos p : BlockPos.betweenClosed(worldPosition.offset(-1, -1, -1), worldPosition.offset(1, 1, 1)))
            if (level.getFluidState(p).is(Fluids.WATER) || level.getFluidState(p).is(Fluids.FLOWING_WATER)) count++;
        water = count;
    }

    public int tickRate() { return 128; }
    @Override public SoundEvent getOperationSoundFile() { return Ic2Sounds.WATERMILL_LOOP.get(); }
    @Override public boolean delayActiveUpdate() { return true; }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerWaterGenerator(id, inventory, this); }
}
