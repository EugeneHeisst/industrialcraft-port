package ic2.core.item;

import ic2.core.ref.Ic2Components;
import ic2.core.upgrade.IUpgradableBlock;
import ic2.core.upgrade.IUpgradeItem;
import ic2.core.upgrade.UpgradableProperty;
import ic2.core.util.StackUtil;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Machine upgrades; ejector/pulling store a side (1-6: west, east, bottom, top, north, south of the original Direction order). */
public class ItemUpgradeModule extends Item implements IUpgradeItem {
    private static final DecimalFormat decimalformat = new DecimalFormat("0.##");
    public enum Type { Overclocker, Transformer, EnergyStorage, Ejector, FluidEjector, RedstoneInverter, Pulling }
    public static final Direction[] DIRECTIONS = {Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};
    public final Type type;

    public ItemUpgradeModule(Properties properties, Type type) { super(properties); this.type = type; }

    public static int getDirection(ItemStack stack) { return stack.getOrDefault(Ic2Components.UPGRADE_DIR.get(), 0); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) {
        switch (type) {
            case Ejector, FluidEjector -> list.add(Component.translatable("ic2.tooltip.upgrade.ejector", Component.translatable(getSideName(stack))).withStyle(ChatFormatting.GRAY));
            case Pulling -> list.add(Component.translatable("ic2.tooltip.upgrade.pulling", Component.translatable(getSideName(stack))).withStyle(ChatFormatting.GRAY));
            case Overclocker -> {
                list.add(Component.translatable("ic2.tooltip.upgrade.overclocker.time", decimalformat.format(100 * Math.pow(getProcessTimeMultiplier(stack, null), stack.getCount()))).withStyle(ChatFormatting.GRAY));
                list.add(Component.translatable("ic2.tooltip.upgrade.overclocker.power", decimalformat.format(100 * Math.pow(getEnergyDemandMultiplier(stack, null), stack.getCount()))).withStyle(ChatFormatting.GRAY));
            }
            case Transformer -> list.add(Component.translatable("ic2.tooltip.upgrade.transformer", getExtraTier(stack, null) * stack.getCount()).withStyle(ChatFormatting.GRAY));
            case EnergyStorage -> list.add(Component.translatable("ic2.tooltip.upgrade.storage", getExtraEnergyStorage(stack, null) * stack.getCount()).withStyle(ChatFormatting.GRAY));
            case RedstoneInverter -> list.add(Component.translatable("ic2.tooltip.upgrade.redstone").withStyle(ChatFormatting.GRAY));
        }
    }

    private static String getSideName(ItemStack stack) {
        return switch (getDirection(stack) - 1) {
            case 0 -> "ic2.dir.west"; case 1 -> "ic2.dir.east"; case 2 -> "ic2.dir.bottom"; case 3 -> "ic2.dir.top"; case 4 -> "ic2.dir.north"; case 5 -> "ic2.dir.south";
            default -> "ic2.tooltip.upgrade.ejector.anyside";
        };
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (type != Type.Ejector && type != Type.FluidEjector && type != Type.Pulling) return InteractionResult.PASS;
        ItemStack stack = ctx.getItemInHand();
        int dir = 1;
        for (int i = 0; i < 6; i++) if (DIRECTIONS[i] == ctx.getClickedFace()) dir = i + 1;
        stack.set(Ic2Components.UPGRADE_DIR.get(), getDirection(stack) == dir ? 0 : dir);
        return InteractionResult.sidedSuccess(ctx.getLevel().isClientSide);
    }

    @Override
    public boolean isSuitableFor(ItemStack stack, Set<UpgradableProperty> types) {
        return switch (type) {
            case Ejector -> types.contains(UpgradableProperty.ItemProducing);
            case FluidEjector -> types.contains(UpgradableProperty.FluidProducing);
            case Pulling -> types.contains(UpgradableProperty.ItemConsuming);
            case Overclocker -> types.contains(UpgradableProperty.Processing) || types.contains(UpgradableProperty.Augmentable);
            case Transformer -> types.contains(UpgradableProperty.Transformer);
            case EnergyStorage -> types.contains(UpgradableProperty.EnergyStorage);
            case RedstoneInverter -> types.contains(UpgradableProperty.RedstoneSensitive);
        };
    }

    @Override public int getAugmentation(ItemStack stack, IUpgradableBlock parent) { return type == Type.Overclocker ? 1 : 0; }
    @Override public int getExtraProcessTime(ItemStack stack, IUpgradableBlock parent) { return 0; }
    @Override public double getProcessTimeMultiplier(ItemStack stack, IUpgradableBlock parent) { return type == Type.Overclocker ? 0.7 : 1; }
    @Override public int getExtraEnergyDemand(ItemStack stack, IUpgradableBlock parent) { return 0; }
    @Override public double getEnergyDemandMultiplier(ItemStack stack, IUpgradableBlock parent) { return type == Type.Overclocker ? 1.6 : 1; }
    @Override public int getExtraEnergyStorage(ItemStack stack, IUpgradableBlock parent) { return type == Type.EnergyStorage ? 10000 : 0; }
    @Override public double getEnergyStorageMultiplier(ItemStack stack, IUpgradableBlock parent) { return 1; }
    @Override public int getExtraTier(ItemStack stack, IUpgradableBlock parent) { return type == Type.Transformer ? 1 : 0; }
    @Override public boolean modifiesRedstoneInput(ItemStack stack, IUpgradableBlock parent) { return type == Type.RedstoneInverter; }
    @Override public int getRedstoneInput(ItemStack stack, IUpgradableBlock parent, int input) { return type == Type.RedstoneInverter ? 15 - input : input; }

    @Override
    public boolean onTick(ItemStack stack, IUpgradableBlock parent) {
        if (type == Type.FluidEjector) return ejectFluid(stack, (BlockEntity)parent);
        if (type != Type.Ejector && type != Type.Pulling) return false;
        int amount = (int)Math.min(parent.getEnergy(), Integer.MAX_VALUE) / 20;
        if (amount <= 0) return false;
        BlockEntity be = (BlockEntity)parent;
        int rawDir = getDirection(stack);
        boolean ret = false;
        for (Direction dir : rawDir >= 1 && rawDir <= 6 ? new Direction[]{DIRECTIONS[rawDir - 1]} : Direction.values()) {
            int moved = type == Type.Ejector ? StackUtil.eject(be, dir, amount) : StackUtil.pull(be, dir, amount);
            if (moved > 0) {
                ret = true;
                parent.useEnergy(20 * moved);
                amount -= moved;
                if (amount <= 0) break;
            }
        }
        return ret;
    }

    /** Fluid ejector: pushes up to 50 mB per tick from the block's fluid handler into the chosen / every adjacent fluid handler. */
    private boolean ejectFluid(ItemStack stack, BlockEntity be) {
        var level = be.getLevel();
        int rawDir = getDirection(stack);
        boolean ret = false;
        for (Direction dir : rawDir >= 1 && rawDir <= 6 ? new Direction[]{DIRECTIONS[rawDir - 1]} : Direction.values()) {
            var src = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, be.getBlockPos(), dir);
            if (src == null && be instanceof net.neoforged.neoforge.fluids.capability.IFluidHandler h) src = h;
            var dst = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, be.getBlockPos().relative(dir), dir.getOpposite());
            if (src == null || dst == null) continue;
            if (!net.neoforged.neoforge.fluids.FluidUtil.tryFluidTransfer(dst, src, 50, true).isEmpty()) ret = true;
        }
        return ret;
    }

    @Override public void onProcessEnd(ItemStack stack, IUpgradableBlock parent, List<ItemStack> output) {}
}
