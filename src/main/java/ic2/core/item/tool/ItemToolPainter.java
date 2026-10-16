package ic2.core.item.tool;

import ic2.api.event.PaintEvent;
import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.block.wiring.TileEntityCable;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2Sounds;
import ic2.core.util.Keyboard;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Painter (32 uses): recolours IC2 cables, wool, carpets, terracotta, glass, concrete, beds-free coloured blocks and sheep.
 * When empty it turns back into a blank painter, or with auto refill (mode switch + use) eats a matching dye.
 */
@EventBusSubscriber(modid = IC2.MODID)
public class ItemToolPainter extends Item implements IBoxable {
    public final DyeColor color;

    public ItemToolPainter(Properties properties, DyeColor color) { super(properties.durability(32)); this.color = color; }

    @Override public boolean canBeStoredInToolbox(ItemStack stack) { return true; }
    /** Dye metadata of the original (black 0 ... white 15), used for cable colours and PaintEvent. */
    public int dyeIndex() { return 15 - color.getId(); }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        PaintEvent event = new PaintEvent(level, pos, ctx.getClickedFace(), dyeIndex());
        if (level.getBlockEntity(pos) instanceof TileEntityCable cable) event.painted = cable.changeColor(dyeIndex());
        else NeoForge.EVENT_BUS.post(event);
        if (!event.painted && !colorBlock(level, pos, level.getBlockState(pos))) return InteractionResult.PASS;
        if (!level.isClientSide && ctx.getPlayer() != null) damagePainter(ctx.getPlayer(), ctx.getHand());
        level.playSound(null, pos, Ic2Sounds.PAINTER.get(), SoundSource.PLAYERS, 1, 1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Swap between the coloured variants of a vanilla block family (white_wool -> red_wool, glass -> red_stained_glass...). */
    private boolean colorBlock(Level level, BlockPos pos, BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (!id.getNamespace().equals("minecraft")) return false;
        String path = id.getPath(), suffix = null;
        for (String s : new String[]{"stained_glass_pane", "stained_glass", "glazed_terracotta", "terracotta", "concrete_powder", "concrete", "wool", "carpet"}) {
            if (path.endsWith("_" + s)) { suffix = s; break; }
        }
        if (suffix == null) suffix = switch (path) { case "glass" -> "stained_glass"; case "glass_pane" -> "stained_glass_pane"; case "terracotta" -> "terracotta"; default -> null; };
        if (suffix == null) return false;
        Block target = BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(color.getName() + "_" + suffix));
        if (target == state.getBlock() || target == net.minecraft.world.level.block.Blocks.AIR) return false;
        if (!level.isClientSide) level.setBlock(pos, target.withPropertiesOf(state), 3);
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && Keyboard.isModeSwitchKeyDown(player)) {
            boolean refill = !stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("autoRefill");
            CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putBoolean("autoRefill", refill));
            player.displayClientMessage(Component.literal(refill ? "Painter automatic refill mode enabled" : "Painter automatic refill mode disabled"), false);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) { list.add(Component.translatable("color.minecraft." + color.getName())); }

    private void damagePainter(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() < stack.getMaxDamage() - 1) { stack.setDamageValue(stack.getDamageValue() + 1); return; }
        if (stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("autoRefill")) {
            TagKey<Item> dye = color.getTag();
            for (ItemStack s : player.getInventory().items) if (s.is(dye)) { s.shrink(1); stack.setDamageValue(0); return; }
        }
        player.setItemInHand(hand, new ItemStack(Ic2Items.get("painter")));
    }

    @SubscribeEvent
    static void sheep(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ItemToolPainter p) || !(event.getTarget() instanceof Sheep sheep) || sheep.getColor() == p.color) return;
        if (!event.getLevel().isClientSide) { sheep.setColor(p.color); p.damagePainter(event.getEntity(), event.getHand()); }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
    }
}
