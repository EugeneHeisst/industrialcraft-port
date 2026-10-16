package ic2.core.item.tool;

import ic2.core.block.machine.tileentity.TileEntityTeleporter;
import ic2.core.ref.Ic2Blocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

/** Links teleporters: click the first one to store it, then the second to link both. Right click in the air unlinks. */
public class ItemFrequencyTransmitter extends Item {
    public ItemFrequencyTransmitter(Properties properties) { super(properties.stacksTo(1)); }

    private static CompoundTag data(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && data(stack).getBoolean("targetSet")) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putBoolean("targetSet", false));
            player.sendSystemMessage(Component.literal("Frequency Transmitter unlinked"));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (!(level.getBlockEntity(ctx.getClickedPos()) instanceof TileEntityTeleporter tp)) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        Player player = ctx.getPlayer();
        CompoundTag nbt = data(stack);
        boolean targetSet = nbt.getBoolean("targetSet");
        BlockPos target = new BlockPos(nbt.getInt("targetX"), nbt.getInt("targetY"), nbt.getInt("targetZ"));
        if (targetSet && !level.getBlockState(target).is(Ic2Blocks.TELEPORTER.get())) targetSet = false;
        BlockPos here = tp.getBlockPos();
        String msg;
        if (!targetSet) { targetSet = true; target = here; msg = "Frequency Transmitter linked to Teleporter."; }
        else if (here.equals(target)) msg = "Can't link Teleporter to itself.";
        else if (tp.targetSet && tp.target.equals(target)) msg = "Teleportation link unchanged.";
        else {
            tp.setTarget(target);
            if (level.getBlockEntity(target) instanceof TileEntityTeleporter tp2 && !tp2.targetSet) tp2.setTarget(here);
            msg = "Teleportation link established.";
        }
        if (player != null) player.sendSystemMessage(Component.literal(msg));
        boolean set = targetSet;
        BlockPos t = target;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> { tag.putBoolean("targetSet", set); tag.putInt("targetX", t.getX()); tag.putInt("targetY", t.getY()); tag.putInt("targetZ", t.getZ()); });
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) {
        CompoundTag nbt = data(stack);
        if (nbt.getBoolean("targetSet")) list.add(Component.translatable("ic2.itemFreq.tooltip.target", nbt.getInt("targetX"), nbt.getInt("targetY"), nbt.getInt("targetZ")));
        else list.add(Component.translatable("ic2.itemFreq.tooltip.blank"));
    }
}
