package ic2.core.audit;

import ic2.core.block.misc.BlockFoamWall;
import ic2.core.block.wiring.CableType;
import ic2.core.block.wiring.TileEntityCable;
import ic2.core.fluid.Ic2Fluids;
import ic2.core.item.tool.ItemSprayer;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

/** Construction foam: sprayer modes and spreading, CF pack, hardening, walls, reinforced foam and foamed cables. */
public final class FoamAudit {
    private FoamAudit() {}

    private static void use(ServerLevel l, net.minecraft.world.entity.player.Player actor, ItemStack spray, BlockPos clicked, Direction face) {
        actor.setItemInHand(InteractionHand.MAIN_HAND, spray);
        spray.getItem().useOn(new UseOnContext(l, actor, InteractionHand.MAIN_HAND, spray, new BlockHitResult(Vec3.atCenterOf(clicked), face, clicked, false)));
    }

    private static void hardenAll(ServerLevel l, BlockPos pos) {
        for (int i = 0; i < 2000 && (l.getBlockState(pos).is(Ic2Blocks.CONSTRUCTION_FOAM.get()) || l.getBlockState(pos).is(Ic2Blocks.REINFORCED_CONSTRUCTION_FOAM.get())); i++)
            l.getBlockState(pos).tick(l, pos, l.random);
    }

    public static void run(ServerLevel l, BlockPos p, CropAudit.Check check) {
        for (var pos : BlockPos.betweenClosed(p.offset(-3, -1, -3), p.offset(12, 6, 6))) l.setBlock(pos, pos.getY() < p.getY() ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
        var actor = FakePlayerFactory.getMinecraft(l);
        actor.getInventory().clearContent();
        actor.setPos(p.getX() + 0.5, p.getY(), p.getZ() + 3.5);
        actor.setYRot(180); actor.setXRot(30);
        ItemSprayer sprayer = Ic2Items.FOAM_SPRAYER.get();
        ItemStack spray = sprayer.filled();
        var h = spray.getCapability(Capabilities.FluidHandler.ITEM);
        check.test("CF sprayer holds 8000 mB construction foam only", h != null && h.getTankCapacity(0) == 8000 && h.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 100), FluidAction.SIMULATE) == 0 && ItemSprayer.getFluid(spray).getAmount() == 8000, ItemSprayer.getFluid(spray));
        use(l, actor, spray, p.below(), Direction.UP);
        int foam = 0;
        for (var pos : BlockPos.betweenClosed(p.offset(-3, 0, -3), p.offset(3, 5, 3))) if (l.getBlockState(pos).is(Ic2Blocks.CONSTRUCTION_FOAM.get())) foam++;
        check.test("sprayer places ten foam blocks for 1000 mB", foam == 10 && ItemSprayer.getFluid(spray).getAmount() == 7000, "foam=" + foam + " left=" + ItemSprayer.getFluid(spray).getAmount());
        check.test("soft foam has no collision", l.getBlockState(p).getCollisionShape(l, p, CollisionContext.empty()).isEmpty(), "collision");
        hardenAll(l, p);
        check.test("foam hardens in light into a light gray wall", l.getBlockState(p).is(Ic2Blocks.CONSTRUCTION_FOAM_WALL.get()) && l.getBlockState(p).getValue(BlockFoamWall.COLOR) == 7, l.getBlockState(p));
        ItemStack painter = new ItemStack(Ic2Items.PAINTERS.get(DyeColor.RED).get());
        actor.setItemInHand(InteractionHand.MAIN_HAND, painter);
        painter.getItem().onItemUseFirst(painter, new UseOnContext(l, actor, InteractionHand.MAIN_HAND, painter, new BlockHitResult(Vec3.atCenterOf(p), Direction.UP, p, false)));
        check.test("painter recolours foam wall", l.getBlockState(p).getValue(BlockFoamWall.COLOR) == 1, l.getBlockState(p));
        for (var pos : BlockPos.betweenClosed(p.offset(-3, 0, -3), p.offset(12, 6, 6))) l.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

        CompoundSingle: {
            var tag = new net.minecraft.nbt.CompoundTag(); tag.putInt("mode", 1);
            spray.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
            use(l, actor, spray, p.below(), Direction.UP);
            check.test("single mode sprays one block for 100 mB", l.getBlockState(p).is(Ic2Blocks.CONSTRUCTION_FOAM.get()) && ItemSprayer.getFluid(spray).getAmount() == 6900, ItemSprayer.getFluid(spray).getAmount());
            ItemStack sand = new ItemStack(Items.SAND, 2);
            l.getBlockState(p).useItemOn(sand, l, actor, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(p), Direction.UP, p, false));
            check.test("sand hardens foam immediately", l.getBlockState(p).is(Ic2Blocks.CONSTRUCTION_FOAM_WALL.get()) && sand.getCount() == 1, l.getBlockState(p));
        }

        // iron scaffold -> reinforced foam -> reinforced stone; wooden scaffold -> normal foam
        BlockPos sc = p.east(4);
        l.setBlock(sc.below(), Blocks.STONE.defaultBlockState(), 3);
        l.setBlock(sc, Ic2Blocks.IRON_SCAFFOLD.get().defaultBlockState().setValue(ic2.core.block.misc.BlockScaffold.STRENGTH, 5), 3);
        use(l, actor, spray, sc, Direction.UP);
        check.test("iron scaffold becomes reinforced foam", l.getBlockState(sc).is(Ic2Blocks.REINFORCED_CONSTRUCTION_FOAM.get()), l.getBlockState(sc));
        hardenAll(l, sc);
        check.test("reinforced foam hardens into reinforced stone", l.getBlockState(sc).is(Ic2Blocks.REINFORCED_STONE.get()), l.getBlockState(sc));

        // foamed cable keeps conducting, hardens, paints the wall, and loses only the foam when broken
        BlockPos cp = p.east(8);
        l.setBlock(cp, Ic2Blocks.CABLES.get(CableType.insulated_copper).get().defaultBlockState(), 3);
        var cable = (TileEntityCable)l.getBlockEntity(cp);
        cable.onLoaded();
        use(l, actor, spray, cp, Direction.UP);
        check.test("sprayer foams a cable", cable.foamed == 1 && l.getBlockState(cp).getCollisionShape(l, cp, CollisionContext.empty()).isEmpty() && l.getBlockState(cp).getShape(l, cp).bounds().getXsize() == 1, cable.foamed);
        ItemStack sand = new ItemStack(Items.SAND, 1);
        l.getBlockState(cp).useItemOn(sand, l, actor, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(cp), Direction.UP, cp, false));
        check.test("sand hardens cable foam into an opaque wall", cable.foamed == 2 && l.getBlockState(cp).getLightBlock(l, cp) == 15 && !l.getBlockState(cp).getCollisionShape(l, cp, CollisionContext.empty()).isEmpty(), "foamed=" + cable.foamed + " light=" + l.getBlockState(cp).getLightBlock(l, cp));
        ItemStack blue = new ItemStack(Ic2Items.PAINTERS.get(DyeColor.BLUE).get());
        blue.getItem().onItemUseFirst(blue, new UseOnContext(l, actor, InteractionHand.MAIN_HAND, blue, new BlockHitResult(Vec3.atCenterOf(cp), Direction.UP, cp, false)));
        check.test("painting a foamed cable colours the wall, not the insulation", cable.foamColor == 4 && cable.getColor() == 0, "foamColor=" + cable.foamColor + " cable=" + cable.getColor());
        var saved = (TileEntityCable)net.minecraft.world.level.block.entity.BlockEntity.loadStatic(cp, l.getBlockState(cp), cable.saveWithFullMetadata(l.registryAccess()), l.registryAccess());
        check.test("cable foam persists", saved != null && saved.foamed == 2 && saved.foamColor == 4, saved == null ? "null" : saved.foamed);
        boolean kept = !l.getBlockState(cp).onDestroyedByPlayer(l, cp, actor, true, l.getFluidState(cp));
        check.test("breaking a foamed cable removes only the foam", kept && l.getBlockState(cp).getBlock() == Ic2Blocks.CABLES.get(CableType.insulated_copper).get() && cable.foamed == 0, l.getBlockState(cp));

        // CF pack: 80000 mB, sprayer drains the worn pack before itself
        var pack = Ic2Items.CF_PACK.get();
        ItemStack ps = pack.filled();
        actor.setItemSlot(EquipmentSlot.CHEST, ps);
        int before = ItemSprayer.getFluid(spray).getAmount();
        l.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        use(l, actor, spray, p.below(), Direction.UP);
        check.test("CF pack holds 80000 mB and feeds the sprayer first", pack.capacity == 80000 && pack.getFluid(ps).getAmount() == 79900 && ItemSprayer.getFluid(spray).getAmount() == before, "pack=" + pack.getFluid(ps).getAmount() + " spray=" + ItemSprayer.getFluid(spray).getAmount());
        actor.getInventory().clearContent();
        actor.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        boolean recipes = true;
        for (var item : new net.minecraft.world.item.Item[]{sprayer, pack}) recipes &= l.getRecipeManager().getRecipes().stream().anyMatch(r -> r.value().getResultItem(l.registryAccess()).is(item));
        check.test("CF sprayer and pack recipes", recipes, "recipes");
    }
}
