package ic2.core.item.tool;

import ic2.api.event.RetextureEvent;
import ic2.api.item.ElectricItem;
import ic2.core.block.Retexture;
import ic2.core.item.BaseElectricItem;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Obscurator: sneak-click a full block to scan the clicked side (20000 EU), then click a side of hardened construction
 * foam (wall or foamed cable) to give it that look (5000 EU).
 */
public class ItemObscurator extends BaseElectricItem {
    public static final double SCAN_COST = 20000, PRINT_COST = 5000;

    public ItemObscurator(Properties p) { super(p.stacksTo(1), 100000, 250, 2); }

    public static BlockState referenced(ItemStack stack, Level level) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("referencedBlock")) return null;
        BlockState s = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), tag.getCompound("referencedBlock"));
        return s.isAir() ? null : s;
    }

    public static Direction referencedSide(ItemStack stack) {
        return Direction.from3DDataValue(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("referencedSide"));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        var player = ctx.getPlayer();
        Level level = ctx.getLevel();
        var pos = ctx.getClickedPos();
        if (player == null) return InteractionResult.PASS;
        if (!player.isShiftKeyDown()) {
            BlockState ref = referenced(stack, level);
            if (ref == null || !ElectricItem.manager.canUse(stack, PRINT_COST)) return InteractionResult.PASS;
            if (level.isClientSide) return InteractionResult.SUCCESS;
            var event = NeoForge.EVENT_BUS.post(new RetextureEvent(level, pos, ctx.getClickedFace(), ref, referencedSide(stack)));
            if (!event.applied) return InteractionResult.FAIL;
            ElectricItem.manager.use(stack, PRINT_COST, player);
            return InteractionResult.SUCCESS;
        }
        BlockState state = level.getBlockState(pos);
        if (!Retexture.isSuitable(state, level, pos) || !ElectricItem.manager.canUse(stack, SCAN_COST)) return InteractionResult.PASS;
        if (state.equals(referenced(stack, level)) && referencedSide(stack) == ctx.getClickedFace()) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!ElectricItem.manager.use(stack, SCAN_COST, player)) return InteractionResult.FAIL;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> { t.put("referencedBlock", NbtUtils.writeBlockState(state)); t.putInt("referencedSide", ctx.getClickedFace().get3DDataValue()); });
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, list, flag);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("referencedBlock")) {
            var name = tag.getCompound("referencedBlock").getString("Name");
            var block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.tryParse(name));
            list.add(Component.translatable(block.getDescriptionId()).append(" (" + referencedSide(stack).getName() + ")").withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }
}
