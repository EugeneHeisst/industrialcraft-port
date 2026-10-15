package ic2.core.block.machine.tileentity;

import ic2.core.block.invslot.InvSlotProcessableSmelting;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Sounds;
import ic2.core.upgrade.UpgradableProperty;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityElectricFurnace extends TileEntityStandardMachine {
    public TileEntityElectricFurnace(BlockPos pos, BlockState state) {
        super(Ic2BlockEntities.ELECTRIC_FURNACE.get(), pos, state, 3, 100, 1);
        inputSlot = new InvSlotProcessableSmelting(this, "input", 1);
    }

    @Override public SoundEvent getStartSoundFile() { return Ic2Sounds.ELECTRO_FURNACE_LOOP.get(); }
    @Override public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
}
