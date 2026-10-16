package ic2.core.block.generator.tileentity;

import ic2.core.ContainerBase;
import ic2.core.WindSim;
import ic2.core.block.generator.container.ContainerWindGenerator;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/** Wind mill: output from the level wind at this height, reduced by non-air blocks around; may break down into a generator. */
public class TileEntityWindGenerator extends TileEntityBaseGenerator {
    public double subproduction, substorage;
    public final float windbasevalue = 1;
    public int ticker = (int)(Math.random() * 128);
    public int obscuratedBlockCount;

    public TileEntityWindGenerator(BlockPos pos, BlockState state) { super(Ic2BlockEntities.WIND_MILL.get(), pos, state, 4, 1, 5); }

    @Override
    public int gaugeFuelScaled(int i) {
        int re = (int)(subproduction / 3 * i);
        return re < 0 ? 0 : Math.min(re, i);
    }

    public int getOverheatScaled(int i) {
        if (subproduction <= 5) return 0;
        return subproduction >= 10 ? i : (int)((subproduction - 5) / 5 * i);
    }

    @Override public void onLoaded() { super.onLoaded(); updateObscuratedBlockCount(); }

    @Override
    public boolean gainEnergy() {
        ticker++;
        if (ticker % tickRate() == 0) {
            if (ticker % (8 * tickRate()) == 0) updateObscuratedBlockCount();
            subproduction = WindSim.get(level).getWindAt(worldPosition.getY()) / 10 * (1 - obscuratedBlockCount / 567.0);
            if (subproduction <= 0) return false;
            if (subproduction > 5 && level.random.nextInt(5000) <= subproduction - 5) {
                subproduction = 0;
                BlockPos pos = worldPosition;
                var lvl = level;
                lvl.setBlock(pos, Ic2Blocks.GENERATOR.get().defaultBlockState(), 3);
                for (int i = lvl.random.nextInt(5); i > 0; i--) Containers.dropItemStack(lvl, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.IRON_INGOT));
                return false;
            }
            subproduction *= windbasevalue;
        }
        substorage += subproduction;
        production = (short)(int)substorage;
        if (storage + production >= maxStorage) { substorage = 0; return false; }
        storage += production;
        substorage -= production;
        return true;
    }

    @Override public boolean gainFuel() { return false; }

    public void updateObscuratedBlockCount() {
        obscuratedBlockCount = -1;
        for (BlockPos p : BlockPos.betweenClosed(worldPosition.offset(-4, -2, -4), worldPosition.offset(4, 4, 4))) if (!level.isEmptyBlock(p)) obscuratedBlockCount++;
    }

    @Override public boolean needsFuel() { return true; }
    public int tickRate() { return 128; }
    @Override public SoundEvent getOperationSoundFile() { return Ic2Sounds.WINDGEN_LOOP.get(); }
    @Override public boolean delayActiveUpdate() { return true; }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerWindGenerator(id, inventory, this); }
}
