package ic2.core.item.tool;

import ic2.api.event.LaserEvent;
import ic2.api.item.ElectricItem;
import ic2.core.ref.Ic2Sounds;
import ic2.core.util.Keyboard;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

/** Mining laser: 300k EU, tier 3; mode switch key + right click cycles the seven modes. */
public class ItemToolMiningLaser extends ItemElectricTool {
    private static final String[] MODES = {"mining", "lowFocus", "longRange", "horizontal", "superHeat", "scatter", "explosive"};
    private static final int[] COST = {1250, 100, 5000, 0, 2500, 10000, 5000};

    public ItemToolMiningLaser(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON), 100, HarvestLevel.Iron, EnumSet.noneOf(ToolClass.class));
        maxCharge = 300000; transferLimit = 512; tier = 3;
    }

    public static int getMode(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("laserSetting"); }
    private static Component modeName(int mode) { return Component.translatable("ic2.tooltip.mode." + MODES[Math.floorMod(mode, 7)]); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, list, flag);
        list.add(Component.translatable("ic2.tooltip.mode", modeName(getMode(stack))));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        int mode = getMode(stack);
        if (Keyboard.isModeSwitchKeyDown(player)) {
            int next = (mode + 1) % 7;
            CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putInt("laserSetting", next));
            player.displayClientMessage(Component.translatable("ic2.tooltip.mode", modeName(next)), false);
            return InteractionResultHolder.success(stack);
        }
        if (mode == 3 || !ElectricItem.manager.use(stack, COST[mode], player)) return InteractionResultHolder.pass(stack);
        float yaw = player.getYRot(), pitch = player.getXRot();
        switch (mode) {
            case 0 -> { if (shoot(level, player, stack, Float.POSITIVE_INFINITY, 5, Integer.MAX_VALUE, false, false, yaw, pitch)) sound(player, Ic2Sounds.LASER); }
            case 1 -> { if (shoot(level, player, stack, 4, 5, 1, false, false, yaw, pitch)) sound(player, Ic2Sounds.LASER_LOW_FOCUS); }
            case 2 -> { if (shoot(level, player, stack, Float.POSITIVE_INFINITY, 20, Integer.MAX_VALUE, false, false, yaw, pitch)) sound(player, Ic2Sounds.LASER_LONG_RANGE); }
            case 4 -> { if (shoot(level, player, stack, Float.POSITIVE_INFINITY, 8, Integer.MAX_VALUE, false, true, yaw, pitch)) sound(player, Ic2Sounds.LASER); }
            case 5 -> {
                for (int x = -2; x <= 2; x++) for (int y = -2; y <= 2; y++) shoot(level, player, stack, Float.POSITIVE_INFINITY, 12, Integer.MAX_VALUE, false, false, yaw + 20 * x, pitch + 20 * y);
                sound(player, Ic2Sounds.LASER_SCATTER);
            }
            case 6 -> { if (shoot(level, player, stack, Float.POSITIVE_INFINITY, 12, Integer.MAX_VALUE, true, false, yaw, pitch)) sound(player, Ic2Sounds.LASER_EXPLOSIVE); }
            default -> {}
        }
        return InteractionResultHolder.success(stack);
    }

    /** Horizontal mode: clicking a block at eye level fires a level beam through it. */
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null || getMode(stack) != 3 || Keyboard.isModeSwitchKeyDown(player)) return InteractionResult.PASS;
        if (ctx.getLevel().isClientSide) return InteractionResult.SUCCESS;
        double y = ctx.getClickedPos().getY() + 0.5;
        if (Math.abs(player.getEyeY() - 0.1 - y) < 1.5) {
            if (ElectricItem.manager.use(stack, 3000, player) && shoot(ctx.getLevel(), player, stack, Float.POSITIVE_INFINITY, 5, Integer.MAX_VALUE, false, false, player.getYRot(), 0, y)) sound(player, Ic2Sounds.LASER);
        } else player.displayClientMessage(Component.literal("Mining laser aiming angle too steep"), false);
        return InteractionResult.SUCCESS;
    }

    private static void sound(Player p, Supplier<SoundEvent> s) { p.level().playSound(null, p.getX(), p.getY(), p.getZ(), s.get(), SoundSource.PLAYERS, 1, 1); }

    public boolean shoot(Level level, LivingEntity owner, ItemStack stack, float range, float power, int breaks, boolean explosive, boolean smelt, double yaw, double pitch) {
        return shoot(level, owner, stack, range, power, breaks, explosive, smelt, yaw, pitch, owner.getEyeY() - 0.1);
    }

    public boolean shoot(Level level, LivingEntity owner, ItemStack stack, float range, float power, int breaks, boolean explosive, boolean smelt, double yaw, double pitch, double y) {
        EntityMiningLaser laser = new EntityMiningLaser(level, owner, range, power, breaks, explosive, yaw, pitch, y);
        var e = new LaserEvent.LaserShootEvent(level, laser, owner, range, power, breaks, explosive, smelt, stack);
        NeoForge.EVENT_BUS.post(e);
        if (!laser.takeDataFromEvent(e)) return false;
        level.addFreshEntity(laser);
        return true;
    }
}
