package ic2.core.item;

import ic2.core.block.BlockTileEntity;
import ic2.core.block.wiring.TileEntityElectricBlock;
import ic2.core.ref.Ic2Blocks;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

/** MFSU upgrade kit: turns an MFE (or MFE charge pad) into an MFSU in place, keeping energy, facing, redstone mode and items. */
public class ItemUpgradeKit extends Item {
    public ItemUpgradeKit(Properties p) { super(p); }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        var level = ctx.getLevel();
        var pos = ctx.getClickedPos();
        var state = level.getBlockState(pos);
        Block target = state.is(Ic2Blocks.MFE.get()) ? Ic2Blocks.MFSU.get() : state.is(Ic2Blocks.CHARGEPAD_MFE.get()) ? Ic2Blocks.CHARGEPAD_MFSU.get() : null;
        if (target == null || !(level.getBlockEntity(pos) instanceof TileEntityElectricBlock mfe)) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        double energy = mfe.energy;
        byte mode = mfe.redstoneMode;
        ItemStack[] items = new ItemStack[mfe.getContainerSize()];
        for (int i = 0; i < items.length; i++) items[i] = mfe.getItem(i).copy();
        mfe.clearContent(); // the items move to the MFSU instead of dropping
        var newState = target.defaultBlockState();
        if (state.hasProperty(BlockTileEntity.FACING) && newState.hasProperty(BlockTileEntity.FACING)) newState = newState.setValue(BlockTileEntity.FACING, state.getValue(BlockTileEntity.FACING));
        level.setBlock(pos, newState, 3);
        if (!(level.getBlockEntity(pos) instanceof TileEntityElectricBlock mfsu)) return InteractionResult.FAIL;
        mfsu.energy = energy;
        mfsu.redstoneMode = mode;
        for (int i = 0; i < items.length && i < mfsu.getContainerSize(); i++) mfsu.setItem(i, items[i]);
        mfsu.setChanged();
        if (ctx.getPlayer() == null || !ctx.getPlayer().getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) { list.add(Component.translatable("ic2.itemMFSUupgradekit.info")); }
}
