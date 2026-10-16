package ic2.core.item.tool;

import ic2.core.block.explosive.BlockDynamite;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Sounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Dynamite-O-Mote: click placed dynamite to link (or unlink) it, right click in the air to set off every linked stick. */
public class ItemRemote extends Item {
    public ItemRemote(Properties p) { super(p.stacksTo(1)); }

    public static ListTag coords(ItemStack s) { return s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getList("coords", Tag.TAG_LONG); }
    private static void setCoords(ItemStack s, ListTag list) { CustomData.update(DataComponents.CUSTOM_DATA, s, t -> t.put("coords", list)); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState s = level.getBlockState(pos);
        boolean plain = s.is(Ic2Blocks.DYNAMITE.get()), linked = s.is(Ic2Blocks.DYNAMITE_REMOTE.get());
        if (!plain && !linked) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        ItemStack stack = ctx.getItemInHand();
        ListTag list = coords(stack);
        if (plain) {
            list.add(LongTag.valueOf(pos.asLong()));
            setCoords(stack, list);
            level.setBlock(pos, Ic2Blocks.DYNAMITE_REMOTE.get().defaultBlockState().setValue(BlockDynamite.FACING, s.getValue(BlockDynamite.FACING)), 3);
        } else {
            int index = list.indexOf(LongTag.valueOf(pos.asLong()));
            if (index < 0) { if (ctx.getPlayer() != null) ctx.getPlayer().displayClientMessage(Component.literal("This dynamite stick is not linked to this remote, cannot unlink."), false); }
            else {
                list.remove(index);
                setCoords(stack, list);
                level.setBlock(pos, Ic2Blocks.DYNAMITE.get().defaultBlockState().setValue(BlockDynamite.FACING, s.getValue(BlockDynamite.FACING)), 3);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), Ic2Sounds.DYNAMITE_REMOTE.get(), SoundSource.PLAYERS, 1, 1);
        for (Tag t : coords(stack)) {
            BlockPos p = BlockPos.of(((LongTag)t).getAsLong());
            if (level.isLoaded(p) && level.getBlockState(p).is(Ic2Blocks.DYNAMITE_REMOTE.get())) BlockDynamite.ignite(level, p, player, 40);
        }
        setCoords(stack, new ListTag());
        return InteractionResultHolder.success(stack);
    }

    @Override public void appendHoverText(ItemStack s, TooltipContext c, List<Component> l, TooltipFlag f) { int n = coords(s).size(); if (n > 0) l.add(Component.literal("Linked to " + n + " dynamite")); }
}
