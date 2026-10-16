package ic2.core.audit;

import ic2.api.item.ElectricItem;
import ic2.core.item.boat.EntityIC2Boat;
import ic2.core.ref.Ic2Entities;
import ic2.core.ref.Ic2Items;
import ic2.core.util.Keyboard;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** IC2 boats: placing, crash breaking per kind, electric boost from armour EU, lava, recipes. */
public final class BoatAudit {
    private BoatAudit() {}

    /** A boat sliding into the stone wall at x+3 with the given speed; returns what it dropped (EMPTY if it survived). */
    private static ItemStack crash(ServerLevel l, BlockPos p, EntityType<EntityIC2Boat> type, double speed) {
        EntityIC2Boat boat = type.create(l);
        boat.moveTo(p.getX() + 3 - 0.7 - 0.05, p.getY() + 0.1, p.getZ() + 0.5, -90, 0);
        l.addFreshEntity(boat);
        boat.setDeltaMovement(speed, 0, 0);
        boat.tick();
        ItemStack drop = ItemStack.EMPTY;
        for (var e : l.getEntitiesOfClass(ItemEntity.class, new AABB(p).inflate(4))) { drop = e.getItem().copy(); e.discard(); }
        boolean alive = boat.isAlive();
        boat.discard();
        return alive ? ItemStack.EMPTY : drop;
    }

    public static void run(ServerLevel l, BlockPos p, CropAudit.Check check) {
        for (var pos : BlockPos.betweenClosed(p.offset(-3, -2, -2), p.offset(4, 3, 2))) l.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
        for (var pos : BlockPos.betweenClosed(p.offset(-2, -1, -1), p.offset(2, -1, 1))) l.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        for (var pos : BlockPos.betweenClosed(p.offset(-2, 0, -1), p.offset(2, 2, 1))) l.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        var pl = FakePlayerFactory.getMinecraft(l);
        pl.getInventory().clearContent();
        pl.moveTo(p.getX() + 0.5, p.getY() + 1, p.getZ() + 0.5, 0, 90);
        ItemStack item = new ItemStack(Ic2Items.CARBON_BOAT.get());
        pl.setItemInHand(InteractionHand.MAIN_HAND, item);
        item.getItem().use(l, pl, InteractionHand.MAIN_HAND);
        var placed = l.getEntitiesOfClass(EntityIC2Boat.class, new AABB(p).inflate(3));
        check.test("carbon boat item places a carbon boat on the water", placed.size() == 1 && placed.get(0).kind == EntityIC2Boat.Kind.CARBON && placed.get(0).getType() == Ic2Entities.CARBON_BOAT.get() && item.isEmpty(), placed.size());
        check.test("boats drop and pick as their own item", placed.size() == 1 && placed.get(0).getDropItem() == Ic2Items.CARBON_BOAT.get() && placed.get(0).getPickResult().is(Ic2Items.CARBON_BOAT.get()), "drop");
        placed.forEach(net.minecraft.world.entity.Entity::discard);
        ItemStack broken = new ItemStack(Ic2Items.BROKEN_RUBBER_BOAT.get());
        pl.setItemInHand(InteractionHand.MAIN_HAND, broken);
        broken.getItem().use(l, pl, InteractionHand.MAIN_HAND);
        check.test("a broken rubber boat cannot be put on the water", broken.getCount() == 1 && l.getEntitiesOfClass(EntityIC2Boat.class, new AABB(p).inflate(3)).isEmpty(), "broken");

        ItemStack carbon = crash(l, p, Ic2Entities.CARBON_BOAT.get(), 0.36);
        ItemStack rubberSlow = crash(l, p, Ic2Entities.RUBBER_BOAT.get(), 0.25);
        ItemStack rubberFast = crash(l, p, Ic2Entities.RUBBER_BOAT.get(), 0.36);
        ItemStack electric = crash(l, p, Ic2Entities.ELECTRIC_BOAT.get(), 0.6);
        check.test("carbon boat survives a full speed crash", carbon.isEmpty(), carbon);
        check.test("rubber boat pops in a crash and tears when faster", rubberSlow.is(Ic2Items.RUBBER_BOAT.get()) && rubberFast.is(Ic2Items.BROKEN_RUBBER_BOAT.get()), rubberSlow + " / " + rubberFast);
        check.test("electric boat breaks into itself above 0.5", electric.is(Ic2Items.ELECTRIC_BOAT.get()), electric);

        EntityIC2Boat eboat = Ic2Entities.ELECTRIC_BOAT.get().create(l), cboat = Ic2Entities.CARBON_BOAT.get().create(l);
        check.test("electric boat floats on lava and is fire proof", eboat.canBoatInFluid(Fluids.LAVA.defaultFluidState()) && !cboat.canBoatInFluid(Fluids.LAVA.defaultFluidState())
            && eboat.canBoatInFluid(Fluids.WATER.defaultFluidState()) && Ic2Entities.ELECTRIC_BOAT.get().fireImmune() && !Ic2Entities.CARBON_BOAT.get().fireImmune(), "lava");
        eboat.moveTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, 0, 0);
        l.addFreshEntity(eboat);
        ItemStack battery = Ic2Items.RE_BATTERY.get().getDefaultInstance();
        ElectricItem.manager.charge(battery, 1000, Integer.MAX_VALUE, true, false);
        pl.setItemSlot(EquipmentSlot.FEET, battery);
        // fake players cannot ride, so the rider's payment is checked directly
        Keyboard.processKeyUpdate(pl, 1 << Keyboard.Key.forward.ordinal());
        double before = ElectricItem.manager.getCharge(pl.getItemBySlot(EquipmentSlot.FEET));
        boolean paid = eboat.payForBoost(pl);
        double after = ElectricItem.manager.getCharge(pl.getItemBySlot(EquipmentSlot.FEET));
        check.test("electric boat burns 4 EU per tick from the rider's armour to accelerate", paid && before - after == 4, before + " -> " + after);
        Keyboard.processKeyUpdate(pl, 0);
        pl.zza = 0;
        check.test("electric boat uses no EU while coasting", !eboat.payForBoost(pl) && ElectricItem.manager.getCharge(pl.getItemBySlot(EquipmentSlot.FEET)) == after, "coast");
        Keyboard.processKeyUpdate(pl, 1 << Keyboard.Key.forward.ordinal());
        pl.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        check.test("electric boat gets no boost without charged armour", !eboat.payForBoost(pl), "empty");
        Keyboard.processKeyUpdate(pl, 0);
        eboat.tick();
        check.test("riderless electric boat is not accelerated", !eboat.isAccelerated(), "no rider");
        pl.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        eboat.discard();

        boolean recipes = true;
        for (var it : new Item[]{Ic2Items.CARBON_BOAT.get(), Ic2Items.RUBBER_BOAT.get(), Ic2Items.ELECTRIC_BOAT.get()})
            recipes &= l.getRecipeManager().getRecipes().stream().anyMatch(r -> r.value().getResultItem(l.registryAccess()).is(it));
        boolean repair = l.getRecipeManager().getRecipes().stream().anyMatch(r -> r.value().getResultItem(l.registryAccess()).is(Ic2Items.RUBBER_BOAT.get())
            && r.value().getIngredients().stream().anyMatch(i -> i.test(new ItemStack(Ic2Items.BROKEN_RUBBER_BOAT.get()))));
        check.test("boat recipes and broken rubber boat repair", recipes && repair, "recipes");
        pl.getInventory().clearContent();
    }
}
