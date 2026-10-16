package ic2.core.block.machine.tileentity;

import ic2.api.item.IBlockCuttingBlade;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.block.machine.container.ContainerBlockCutter;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.upgrade.UpgradableProperty;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Needs a blade at least as hard as the recipe's "hardness". */
public class TileEntityBlockCutter extends TileEntityStandardMachine {
    public boolean bladetoweak;
    public final InvSlotConsumable cutterSlot = new InvSlotConsumable(this, "cutterSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY) {
        @Override public boolean accepts(ItemStack stack) { return stack.getItem() instanceof IBlockCuttingBlade; }
    };

    public TileEntityBlockCutter(BlockPos pos, BlockState state) {
        super(Ic2BlockEntities.BLOCK_CUTTER.get(), pos, state, 48, 900, 1, 2);
        inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.blockcutter);
    }

    @Override
    public RecipeOutput getOutput() {
        if (cutterSlot.isEmpty()) { bladetoweak = true; return null; }
        bladetoweak = false;
        RecipeOutput ret = super.getOutput();
        if (ret != null) {
            if (ret.metadata == null) return null;
            if (ret.metadata.getInt("hardness") > ((IBlockCuttingBlade)cutterSlot.get().getItem()).gethardness()) { bladetoweak = true; return null; }
        }
        return ret;
    }

    public boolean isbladetoweak() { return bladetoweak; }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inventory) { return new ContainerBlockCutter(id, inventory, this); }
    @Override public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
}
