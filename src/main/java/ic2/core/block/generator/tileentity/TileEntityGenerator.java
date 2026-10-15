package ic2.core.block.generator.tileentity;

import ic2.core.ContainerBase;
import ic2.core.block.generator.container.ContainerGenerator;
import ic2.core.block.invslot.InvSlotConsumableFuel;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityGenerator extends TileEntityBaseGenerator {
    public int itemFuelTime;
    public final InvSlotConsumableFuel fuelSlot = new InvSlotConsumableFuel(this, "fuel", 1, false);

    public TileEntityGenerator(BlockPos pos, BlockState state) { super(Ic2BlockEntities.GENERATOR.get(), pos, state, 10, 1, 4000); }

    @Override
    public int gaugeFuelScaled(int i) {
        if (fuel <= 0) return 0;
        if (itemFuelTime <= 0) itemFuelTime = fuel;
        return Math.min(fuel * i / itemFuelTime, i);
    }

    @Override
    public boolean gainFuel() {
        int fuelValue = fuelSlot.consumeFuel() / 4;
        if (fuelValue == 0) return false;
        fuel += fuelValue;
        itemFuelTime = fuelValue;
        return true;
    }

    @Override public boolean isConverting() { return fuel > 0; }
    @Override public SoundEvent getOperationSoundFile() { return Ic2Sounds.GENERATOR_LOOP.get(); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerGenerator(id, inventory, this); }
}
