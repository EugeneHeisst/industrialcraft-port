package ic2.core.audit;

import ic2.core.block.misc.BlockBarrel;
import ic2.core.block.misc.TileEntityBarrel;
import ic2.core.item.ItemBooze;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Components;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** Booze barrel brewing, aging, tapping and mugs; rum; coffee; recipes. */
public final class BoozeAudit {
    private BoozeAudit() {}

    private static void use(ServerLevel l, FakePlayer pl, ItemStack stack, BlockPos pos, Direction face) {
        pl.setItemInHand(InteractionHand.MAIN_HAND, stack);
        stack.getItem().useOn(new UseOnContext(l, pl, InteractionHand.MAIN_HAND, stack, new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false)));
    }

    private static boolean pour(TileEntityBarrel te, FakePlayer pl, ItemStack stack) { pl.setItemInHand(InteractionHand.MAIN_HAND, stack); return te.rightclick(pl, InteractionHand.MAIN_HAND); }

    private static ItemStack drink(ServerLevel l, FakePlayer pl, ItemStack stack) { pl.setItemInHand(InteractionHand.MAIN_HAND, stack); return stack.getItem().finishUsingItem(stack, l, pl); }

    private static int dropped(ServerLevel l, BlockPos p, Item item) {
        int n = 0;
        for (var e : l.getEntitiesOfClass(ItemEntity.class, new AABB(p).inflate(2))) if (e.getItem().is(item)) { n += e.getItem().getCount(); e.discard(); }
        return n;
    }

    public static void run(ServerLevel l, BlockPos p, CropAudit.Check check) {
        for (var pos : BlockPos.betweenClosed(p.offset(-2, -1, -2), p.offset(6, 2, 2))) l.setBlock(pos, pos.getY() < p.getY() ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
        FakePlayer pl = FakePlayerFactory.getMinecraft(l);
        pl.getInventory().clearContent();
        pl.removeAllEffects();
        pl.setShiftKeyDown(false);

        l.setBlock(p, Ic2Blocks.SCAFFOLD.get().defaultBlockState(), 3);
        use(l, pl, new ItemStack(Ic2Items.BARREL.get()), p, Direction.UP);
        var te = l.getBlockEntity(p) instanceof TileEntityBarrel b ? b : null;
        check.test("empty barrel goes onto a wooden scaffold", te != null && te.isEmpty() && pl.getMainHandItem().isEmpty(), l.getBlockState(p));
        boolean poured = pour(te, pl, new ItemStack(Ic2Items.WATER_CELL.get(), 4)) && pour(te, pl, new ItemStack(Items.WHEAT, 2)) && pour(te, pl, new ItemStack(Ic2Items.get("hops"), 2));
        check.test("water cells, wheat and hops brew beer", poured && te.type == 1 && te.boozeAmount == 4 && te.wheatCount == 2 && te.hopsCount == 2, te.boozeAmount + " water " + te.wheatCount + " wheat " + te.hopsCount + " hops");
        check.test("sugar cane cannot go into beer", !pour(te, pl, new ItemStack(Items.SUGAR_CANE, 4)) && te.type == 1, te.type);
        te.age = 23999;
        te.serverTick();
        check.test("beer ages from Brew to Youngster after a day", te.timeRatio == 1 && te.age == 0, te.timeRatio);

        ItemStack tap = new ItemStack(Ic2Items.TREETAP.get());
        use(l, pl, tap, p, Direction.NORTH);
        check.test("treetap fits on a side of the barrel", te.treetapSide == 2 && l.getBlockState(p).getValue(BlockBarrel.TAP) == BlockBarrel.Tap.NORTH && tap.isEmpty(), te.treetapSide);
        te.age = 5;
        te.serverTick();
        check.test("tapped barrel stops aging", te.age == 5, te.age);
        ItemStack mug = new ItemStack(Ic2Items.EMPTY_MUG.get());
        use(l, pl, mug, p, Direction.SOUTH);
        boolean wrongSide = pl.getMainHandItem().is(Ic2Items.EMPTY_MUG.get());
        use(l, pl, pl.getMainHandItem(), p, Direction.NORTH);
        ItemStack beer = pl.getMainHandItem();
        int v = ItemBooze.value(beer);
        check.test("a mug at the tap draws one litre of beer", wrongSide && beer.is(Ic2Items.BOOZE_MUG.get()) && ItemBooze.getTypeOfValue(v) == 1 && ItemBooze.getAmountOfValue(v) == 4 && ItemBooze.getTimeRatioOfBeerValue(v) == 1
            && ItemBooze.getHopsRatioOfBeerValue(v) == 3 && ItemBooze.getSolidRatioOfBeerValue(v) == 3 && te.boozeAmount == 3 && beer.getHoverName().getString().equals("Youngster"), beer.getHoverName().getString() + " " + te.boozeAmount);
        int food = pl.getFoodData().getFoodLevel();
        pl.getFoodData().setFoodLevel(10);
        ItemStack back = drink(l, pl, beer);
        check.test("drinking beer feeds, slows mining and returns the mug", back.is(Ic2Items.EMPTY_MUG.get()) && pl.hasEffect(MobEffects.DIG_SLOWDOWN) && pl.getFoodData().getFoodLevel() == 13, pl.getFoodData().getFoodLevel());
        pl.getFoodData().setFoodLevel(food);
        drink(l, pl, ItemBooze.of(v));
        check.test("a second beer adds strength", pl.hasEffect(MobEffects.DAMAGE_BOOST), pl.getActiveEffects().size());
        pl.removeAllEffects();

        dropped(l, p, Ic2Items.TREETAP.get());
        l.getBlockState(p).attack(l, p, pl);
        check.test("left click takes the tap off and spills a litre", dropped(l, p, Ic2Items.TREETAP.get()) == 1 && te.treetapSide == 0 && te.boozeAmount == 2 && l.getBlockState(p).getValue(BlockBarrel.TAP) == BlockBarrel.Tap.NONE, te.boozeAmount);
        l.getBlockState(p).attack(l, p, pl);
        var items = l.getEntitiesOfClass(ItemEntity.class, new AABB(p).inflate(2));
        ItemStack barrel = items.stream().map(ItemEntity::getItem).filter(s -> s.is(Ic2Items.BARREL.get())).findFirst().orElse(ItemStack.EMPTY);
        items.forEach(ItemEntity::discard);
        int bv = barrel.getOrDefault(Ic2Components.BOOZE.get(), 0);
        check.test("left click takes the filled barrel off the scaffold", l.getBlockState(p).is(Ic2Blocks.SCAFFOLD.get()) && ItemBooze.getAmountOfValue(bv) == 2 && ItemBooze.getTimeRatioOfBeerValue(bv) == 1, barrel.getHoverName().getString());
        use(l, pl, barrel, p, Direction.UP);
        te = (TileEntityBarrel)l.getBlockEntity(p);
        check.test("the filled barrel keeps its beer when set up again", te.type == 1 && te.boozeAmount == 2 && te.timeRatio == 1 && !te.detailed, te.boozeAmount);

        // rum: sugar cane in an empty barrel, fully fermented after timeNedForRum
        BlockPos r = p.east(3);
        l.setBlock(r, Ic2Blocks.SCAFFOLD.get().defaultBlockState(), 3);
        use(l, pl, new ItemStack(Ic2Items.BARREL.get()), r, Direction.UP);
        var rum = (TileEntityBarrel)l.getBlockEntity(r);
        check.test("sugar cane ferments to rum", pour(rum, pl, new ItemStack(Items.SUGAR_CANE, 10)) && rum.type == 2 && rum.boozeAmount == 10 && !pour(rum, pl, new ItemStack(Items.WHEAT)), rum.type);
        rum.age = rum.timeNedForRum(10);
        int rv = rum.calculateMetaValue();
        drink(l, pl, ItemBooze.of(rv));
        check.test("ripe rum gives fire resistance and resistance", ItemBooze.getProgressOfRumValue(rv) == 100 && pl.hasEffect(MobEffects.FIRE_RESISTANCE) && pl.hasEffect(MobEffects.DAMAGE_RESISTANCE) && ItemBooze.of(rv).getHoverName().getString().equals("Rum"), rv);
        pl.removeAllEffects();

        // coffee
        ItemStack mugBack = drink(l, pl, new ItemStack(Ic2Items.COLD_COFFEE.get()));
        check.test("cold coffee gives speed and haste and returns the mug", mugBack.is(Ic2Items.EMPTY_MUG.get()) && pl.hasEffect(MobEffects.MOVEMENT_SPEED) && pl.hasEffect(MobEffects.DIG_SPEED) && pl.getEffect(MobEffects.MOVEMENT_SPEED).getDuration() == 300, pl.getActiveEffects().size());
        pl.removeAllEffects();
        for (int i = 0; i < 3; i++) drink(l, pl, new ItemStack(Ic2Items.DARK_COFFEE.get()));
        boolean calm = !pl.hasEffect(MobEffects.CONFUSION);
        drink(l, pl, new ItemStack(Ic2Items.DARK_COFFEE.get()));
        check.test("too much dark coffee makes you sick", calm && pl.hasEffect(MobEffects.CONFUSION) && pl.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() == 3, pl.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier());
        pl.removeAllEffects();
        pl.setHealth(pl.getMaxHealth());

        boolean recipes = true;
        for (var item : new Item[]{Ic2Items.EMPTY_MUG.get(), Ic2Items.COLD_COFFEE.get(), Ic2Items.COFFEE.get(), Ic2Items.BARREL.get(), Ic2Items.DARK_COFFEE.get()})
            recipes &= l.getRecipeManager().getRecipes().stream().anyMatch(rr -> rr.value().getResultItem(l.registryAccess()).is(item));
        var smelt = l.getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMELTING, new net.minecraft.world.item.crafting.SingleRecipeInput(new ItemStack(Ic2Items.COLD_COFFEE.get())), l);
        check.test("mug, coffee and barrel recipes, cold coffee smelts to dark coffee", recipes && smelt.isPresent() && smelt.get().value().getResultItem(l.registryAccess()).is(Ic2Items.DARK_COFFEE.get()), "recipes");
        pl.getInventory().clearContent();
    }
}
