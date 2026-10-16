package ic2.core.item.resources;

import ic2.core.ref.Ic2Items;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/** Cells of the original ItemCell: an empty cell or one bucket of a fixed fluid (or none for compressed air). */
public class ItemCell extends Item {
    private static Map<Fluid, Item> filled;

    /** fluid -> filled cell, from every registered fixed-fluid cell. */
    public static Map<Fluid, Item> filled() {
        if (filled == null) {
            var map = new java.util.HashMap<Fluid, Item>();
            for (Item item : net.minecraft.core.registries.BuiltInRegistries.ITEM) if (item instanceof ItemCell cell && cell.fluid != null) map.put(cell.getFluid(), item);
            filled = map;
        }
        return filled;
    }
    private final Supplier<Fluid> fluid;

    public ItemCell(Properties properties, Supplier<Fluid> fluid) { super(properties); this.fluid = fluid; }

    public Fluid getFluid() { return fluid == null ? Fluids.EMPTY : fluid.get(); }

    @Override public boolean hasCraftingRemainingItem(ItemStack stack) { return fluid != null; }
    @Override public ItemStack getCraftingRemainingItem(ItemStack stack) { return fluid != null ? new ItemStack(Ic2Items.get("empty_cell")) : ItemStack.EMPTY; }

    /** Empty cell right-clicked on a water/lava source fills one cell. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (fluid != null) return InteractionResultHolder.pass(stack);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(stack);
        BlockPos pos = hit.getBlockPos();
        Fluid f = level.getFluidState(pos).getType();
        Item filledCell = filled().get(f);
        if (filledCell == null || !(level.getBlockState(pos).getBlock() instanceof BucketPickup pickup)) return InteractionResultHolder.pass(stack);
        if (!level.isClientSide) {
            pickup.pickupBlock(player, level, pos, level.getBlockState(pos));
            ItemStack cell = new ItemStack(filledCell);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            if (!player.getInventory().add(cell)) player.drop(cell, false);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** Item fluid handler: filled cells drain 1000 mB into an empty cell, empty cells accept 1000 mB of a known fluid. */
    public static class Handler implements IFluidHandlerItem {
        private ItemStack container;
        public Handler(ItemStack container) { this.container = container; }

        private Fluid fluid() { return container.getItem() instanceof ItemCell c ? c.getFluid() : Fluids.EMPTY; }
        @Override public ItemStack getContainer() { return container; }
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tank) { return fluid() == Fluids.EMPTY ? FluidStack.EMPTY : new FluidStack(fluid(), 1000); }
        @Override public int getTankCapacity(int tank) { return 1000; }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return filled().containsKey(stack.getFluid()); }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (container.getCount() != 1 || fluid() != Fluids.EMPTY || !container.is(Ic2Items.get("empty_cell")) || resource.getAmount() < 1000 || !filled().containsKey(resource.getFluid())) return 0;
            if (action.execute()) container = new ItemStack(filled().get(resource.getFluid()));
            return 1000;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return resource.getFluid() == fluid() ? drain(resource.getAmount(), action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (container.getCount() != 1 || fluid() == Fluids.EMPTY || maxDrain < 1000) return FluidStack.EMPTY;
            FluidStack ret = new FluidStack(fluid(), 1000);
            if (action.execute()) container = new ItemStack(Ic2Items.get("empty_cell"));
            return ret;
        }
    }

}
