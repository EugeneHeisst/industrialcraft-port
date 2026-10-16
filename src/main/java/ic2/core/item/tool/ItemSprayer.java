package ic2.core.item.tool;

import ic2.api.item.IBoxable;
import ic2.core.block.misc.BlockFoam;
import ic2.core.block.wiring.BlockCable;
import ic2.core.block.wiring.TileEntityCable;
import ic2.core.fluid.Ic2Fluids;
import ic2.core.item.armor.ItemArmorFluidTank;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Components;
import ic2.core.ref.Ic2Items;
import ic2.core.util.Keyboard;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

/**
 * CF sprayer: 8000 mB of construction foam, 100 mB per block, up to 10 blocks (or 1 in single mode) spread away from the player;
 * foams scaffolds (reinforced for iron scaffolds) and cables, and also draws from a worn CF pack.
 */
public class ItemSprayer extends Item implements IBoxable {
    public static final int CAPACITY = 8000, FLUID_PER_FOAM = 100;
    public enum Target { Any, Scaffold, Cable }

    public ItemSprayer(Properties p) { super(p.stacksTo(1)); }

    public FluidHandlerItemStack handler(ItemStack stack) {
        return new FluidHandlerItemStack(Ic2Components.FLUID, stack, CAPACITY) { @Override public boolean canFillFluidType(FluidStack f) { return f.is(Ic2Fluids.CONSTRUCTION_FOAM.get()); } };
    }
    public static FluidStack getFluid(ItemStack s) { return s.getOrDefault(Ic2Components.FLUID.get(), SimpleFluidContent.EMPTY).copy(); }
    public ItemStack filled() { ItemStack s = new ItemStack(this); handler(s).fill(new FluidStack(Ic2Fluids.CONSTRUCTION_FOAM.get(), CAPACITY), IFluidHandler.FluidAction.EXECUTE); return s; }
    private static int mode(ItemStack s) { return s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("mode"); }

    @Override public boolean canBeStoredInToolbox(ItemStack stack) { return true; }
    @Override public boolean isBarVisible(ItemStack s) { return !getFluid(s).isEmpty(); }
    @Override public int getBarWidth(ItemStack s) { return Math.round(13f * getFluid(s).getAmount() / CAPACITY); }
    @Override public int getBarColor(ItemStack s) { return 0x9a9a9a; }
    @Override public void appendHoverText(ItemStack s, TooltipContext c, List<Component> lines, TooltipFlag f) {
        lines.add(Component.literal(getFluid(s).getAmount() + " / " + CAPACITY + " mB"));
        lines.add(Component.translatable("ic2.tooltip.mode", Component.translatable(mode(s) == 0 ? "ic2.tooltip.mode.normal" : "ic2.tooltip.mode.single")));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && Keyboard.isModeSwitchKeyDown(player)) {
            int m = mode(stack) == 0 ? 1 : 0;
            CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putInt("mode", m));
            player.displayClientMessage(Component.translatable("ic2.tooltip.mode", Component.translatable(m == 0 ? "ic2.tooltip.mode.normal" : "ic2.tooltip.mode.single")), false);
            return InteractionResultHolder.success(stack);
        }
        // aiming at a construction foam source fills the sprayer
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() == HitResult.Type.BLOCK && level.getFluidState(hit.getBlockPos()).is(Ic2Fluids.CONSTRUCTION_FOAM.get()) && level.getFluidState(hit.getBlockPos()).isSource()) {
            var h = handler(stack);
            if (h.fill(new FluidStack(Ic2Fluids.CONSTRUCTION_FOAM.get(), 1000), IFluidHandler.FluidAction.SIMULATE) == 1000) {
                if (!level.isClientSide) { h.fill(new FluidStack(Ic2Fluids.CONSTRUCTION_FOAM.get(), 1000), IFluidHandler.FluidAction.EXECUTE); level.removeBlock(hit.getBlockPos(), false); }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null || Keyboard.isModeSwitchKeyDown(player)) return InteractionResult.PASS;
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        ItemStack stack = ctx.getItemInHand();
        int max = getFluid(stack).getAmount() / FLUID_PER_FOAM;
        ItemStack pack = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemArmorFluidTank cf = pack.is(Ic2Items.CF_PACK.get()) ? (ItemArmorFluidTank)pack.getItem() : null;
        if (cf != null) max += cf.getFluid(pack).getAmount() / FLUID_PER_FOAM;
        if (max == 0) return InteractionResult.FAIL;
        max = Math.min(max, mode(stack) == 0 ? 10 : 1);
        BlockPos pos = ctx.getClickedPos();
        Target target;
        if (canPlaceFoam(level, pos, Target.Scaffold)) target = Target.Scaffold;
        else if (canPlaceFoam(level, pos, Target.Cable)) target = Target.Cable;
        else { pos = pos.relative(ctx.getClickedFace()); target = Target.Any; }
        int amount = sprayFoam(level, pos, directionsFromPlayer(player), target, max) * FLUID_PER_FOAM;
        if (amount <= 0) return InteractionResult.FAIL;
        if (cf != null && cf.drain(pack, amount, false).getAmount() >= amount) { cf.drain(pack, amount, true); amount = 0; }
        if (amount > 0) handler(stack).drain(amount, IFluidHandler.FluidAction.EXECUTE);
        return InteractionResult.SUCCESS;
    }

    /** Directions the foam may spread to: not back towards the player (down/up/north/south/west/east order of the original). */
    public static boolean[] directionsFromPlayer(Player player) {
        float yaw = (player.getYRot() % 360 + 360) % 360, pitch = player.getXRot();
        boolean[] r = {true, true, true, true, true, true};
        if (pitch >= -65 && pitch <= 65) {
            if (yaw >= 300 || yaw <= 60) r[2] = false;
            if (yaw >= 30 && yaw <= 150) r[5] = false;
            if (yaw >= 120 && yaw <= 240) r[3] = false;
            if (yaw >= 210 && yaw <= 330) r[4] = false;
        }
        if (pitch <= -40) r[0] = false;
        if (pitch >= 40) r[1] = false;
        return r;
    }

    public static boolean canPlaceFoam(Level level, BlockPos pos, Target target) {
        return switch (target) {
            case Any -> BlockFoam.canPlaceFoam(level, pos);
            case Scaffold -> level.getBlockState(pos).is(Ic2Blocks.SCAFFOLD.get()) || level.getBlockState(pos).is(Ic2Blocks.IRON_SCAFFOLD.get());
            case Cable -> level.getBlockEntity(pos) instanceof TileEntityCable c && !c.isFoamed();
        };
    }

    private static final Direction[] ORDER = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public int sprayFoam(Level level, BlockPos start, boolean[] directions, Target target, int maxFoamBlocks) {
        if (!canPlaceFoam(level, start, target)) return 0;
        List<BlockPos> check = new ArrayList<>(List.of(start)), place = new ArrayList<>();
        int foamBlocks = 0;
        for (int i = 0; i < check.size() && foamBlocks < maxFoamBlocks; i++) {
            BlockPos p = check.get(i);
            if (!canPlaceFoam(level, p, target)) continue;
            if (!place.contains(p)) place.add(p);
            for (int d : rngSpread(level.random)) if (target != Target.Any || directions[d]) { BlockPos n = p.relative(ORDER[d]); if (!check.contains(n)) check.add(n); }
            foamBlocks++;
        }
        for (BlockPos p : place) {
            BlockState s = level.getBlockState(p);
            if (s.is(Ic2Blocks.SCAFFOLD.get())) { Block.dropResources(s, level, p); level.setBlock(p, Ic2Blocks.CONSTRUCTION_FOAM.get().defaultBlockState(), 3); }
            else if (s.is(Ic2Blocks.IRON_SCAFFOLD.get())) level.setBlock(p, Ic2Blocks.REINFORCED_CONSTRUCTION_FOAM.get().defaultBlockState(), 3);
            else if (level.getBlockEntity(p) instanceof TileEntityCable c) c.changeFoam(1);
            else level.setBlock(p, Ic2Blocks.CONSTRUCTION_FOAM.get().defaultBlockState(), 3);
        }
        return foamBlocks;
    }

    private static int[] rngSpread(RandomSource r) {
        int[] re = {0, 1, 2, 3, 4, 5};
        for (int i = 0; i < 16; i++) { int a = r.nextInt(6), b = r.nextInt(6), t = re[a]; re[a] = re[b]; re[b] = t; }
        return re;
    }
}
