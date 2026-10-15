package ic2.core.block.machine.tileentity;

import ic2.api.recipe.Recipes;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Sounds;
import ic2.core.upgrade.UpgradableProperty;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCompressor extends TileEntityStandardMachine {
    public TileEntityCompressor(BlockPos pos, BlockState state) {
        super(Ic2BlockEntities.COMPRESSOR.get(), pos, state, 2, 300, 1);
        inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.compressor);
    }

    @Override public SoundEvent getStartSoundFile() { return Ic2Sounds.COMPRESSOR_OP.get(); }
    @Override public SoundEvent getInterruptSoundFile() { return Ic2Sounds.INTERRUPT_ONE.get(); }
    @Override public float getWrenchDropRate() { return 0.85f; }
    @Override public Set<UpgradableProperty> getUpgradableProperties() { return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing); }
}
