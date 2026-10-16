package ic2.core.audit;

import ic2.core.block.explosive.BlockDynamite;
import ic2.core.block.explosive.EntityDynamite;
import ic2.core.item.tool.ItemRemote;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** Dynamite placement, remote linking and detonation, point explosion, sticky fuse, redstone and support loss. */
public final class DynamiteAudit {
    private DynamiteAudit() {}

    private static java.util.List<EntityDynamite> primed(ServerLevel l, BlockPos p) { return l.getEntitiesOfClass(EntityDynamite.class, new AABB(p).inflate(3)); }

    public static void run(ServerLevel l, BlockPos p, CropAudit.Check check) {
        for (var pos : BlockPos.betweenClosed(p.offset(-4, -3, -4), p.offset(14, 4, 4))) l.setBlock(pos, pos.getY() < p.getY() ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
        var actor = FakePlayerFactory.getMinecraft(l);
        actor.getInventory().clearContent();
        ItemStack dyn = new ItemStack(Ic2Items.DYNAMITE.get(), 4);
        actor.setItemInHand(InteractionHand.MAIN_HAND, dyn);
        dyn.getItem().useOn(new UseOnContext(l, actor, InteractionHand.MAIN_HAND, dyn, new BlockHitResult(Vec3.atCenterOf(p.below()), Direction.UP, p.below(), false)));
        check.test("dynamite places as a standing stick", l.getBlockState(p).is(Ic2Blocks.DYNAMITE.get()) && l.getBlockState(p).getValue(BlockDynamite.FACING) == Direction.UP && dyn.getCount() == 3, l.getBlockState(p));
        BlockPos wall = p.east(3);
        l.setBlock(wall.east(), Blocks.STONE.defaultBlockState(), 3);
        l.setBlock(wall.below(), Blocks.AIR.defaultBlockState(), 3);
        dyn.getItem().useOn(new UseOnContext(l, actor, InteractionHand.MAIN_HAND, dyn, new BlockHitResult(Vec3.atCenterOf(wall.east()), Direction.WEST, wall.east(), false)));
        check.test("dynamite hangs on a wall", l.getBlockState(wall).is(Ic2Blocks.DYNAMITE.get()) && l.getBlockState(wall).getValue(BlockDynamite.FACING) == Direction.WEST, l.getBlockState(wall));

        ItemStack remote = new ItemStack(Ic2Items.DYNAMITE_REMOTE.get());
        actor.setItemInHand(InteractionHand.MAIN_HAND, remote);
        remote.getItem().useOn(new UseOnContext(l, actor, InteractionHand.MAIN_HAND, remote, new BlockHitResult(Vec3.atCenterOf(p), Direction.UP, p, false)));
        remote.getItem().useOn(new UseOnContext(l, actor, InteractionHand.MAIN_HAND, remote, new BlockHitResult(Vec3.atCenterOf(wall), Direction.UP, wall, false)));
        check.test("remote links two sticks", ItemRemote.coords(remote).size() == 2 && l.getBlockState(p).is(Ic2Blocks.DYNAMITE_REMOTE.get()) && l.getBlockState(wall).is(Ic2Blocks.DYNAMITE_REMOTE.get()), ItemRemote.coords(remote).size());
        remote.getItem().useOn(new UseOnContext(l, actor, InteractionHand.MAIN_HAND, remote, new BlockHitResult(Vec3.atCenterOf(wall), Direction.UP, wall, false)));
        check.test("remote unlinks a stick", ItemRemote.coords(remote).size() == 1 && l.getBlockState(wall).is(Ic2Blocks.DYNAMITE.get()) && l.getBlockState(wall).getValue(BlockDynamite.FACING) == Direction.WEST, l.getBlockState(wall));
        remote.getItem().use(l, actor, InteractionHand.MAIN_HAND);
        var lit = primed(l, p);
        check.test("remote sets off linked dynamite with a 40-tick fuse", l.getBlockState(p).isAir() && lit.size() == 1 && lit.get(0).fuse == 40 && ItemRemote.coords(remote).isEmpty() && l.getBlockState(wall).is(Ic2Blocks.DYNAMITE.get()), "lit=" + lit.size());

        // the primed stick blows the 3x3x3 stone around it (resistance 6 < 10) but not obsidian, with drops, and hurts entities
        l.setBlock(p.north(), Blocks.OBSIDIAN.defaultBlockState(), 3);
        var victim = net.minecraft.world.entity.EntityType.PIG.create(l);
        victim.moveTo(p.getX() + 1.5, p.getY(), p.getZ() + 0.5);
        victim.setNoAi(true);
        l.addFreshEntity(victim);
        float hp = victim.getHealth();
        EntityDynamite e = lit.get(0);
        e.setDeltaMovement(Vec3.ZERO);
        e.setPos(p.getX() + 0.5, p.getY() + 0.1, p.getZ() + 0.5);
        for (int i = 0; i < 60 && e.isAlive(); i++) e.tick();
        int stone = 0;
        for (var pos : BlockPos.betweenClosed(p.offset(-1, -1, -1), p.offset(1, 1, 1))) if (l.getBlockState(pos).is(Blocks.STONE)) stone++;
        int drops = l.getEntitiesOfClass(ItemEntity.class, new AABB(p).inflate(3)).stream().filter(it -> it.getItem().is(Items.COBBLESTONE)).mapToInt(it -> it.getItem().getCount()).sum();
        check.test("point explosion breaks stone around it, drops it and spares obsidian", !e.isAlive() && stone == 0 && drops > 0 && l.getBlockState(p.north()).is(Blocks.OBSIDIAN), "stone=" + stone + " drops=" + drops);
        check.test("point explosion hurts nearby mobs", !victim.isAlive() || victim.getHealth() < hp, victim.getHealth());

        // redstone lights a placed stick; losing every support sets it off in 5 ticks
        BlockPos r = p.east(6);
        l.setBlock(r.below(), Blocks.STONE.defaultBlockState(), 3);
        l.setBlock(r, Ic2Blocks.DYNAMITE.get().defaultBlockState(), 3);
        l.setBlock(r.east(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
        check.test("redstone ignites dynamite", l.getBlockState(r).isAir() && primed(l, r).stream().anyMatch(d -> d.fuse == 40), primed(l, r).size());
        primed(l, r).forEach(net.minecraft.world.entity.Entity::discard);
        BlockPos s = p.east(10);
        l.setBlock(s.below(), Blocks.STONE.defaultBlockState(), 3);
        l.setBlock(s, Ic2Blocks.DYNAMITE.get().defaultBlockState(), 3);
        l.setBlock(s.below(), Blocks.AIR.defaultBlockState(), 3);
        check.test("dynamite without any support goes off in 5 ticks", l.getBlockState(s).isAir() && primed(l, s).stream().anyMatch(d -> d.fuse == 5), primed(l, s).size());
        primed(l, s).forEach(net.minecraft.world.entity.Entity::discard);

        // sticky dynamite burns its fuse four times faster once stuck
        EntityDynamite sticky = new EntityDynamite(l, s.getX() + 0.5, s.getY() + 0.5, s.getZ() + 0.5, true);
        l.setBlock(s.below(), Blocks.STONE.defaultBlockState(), 3);
        sticky.setDeltaMovement(0, -1, 0);
        l.addFreshEntity(sticky);
        sticky.tick(); sticky.tick();
        int before = sticky.fuse;
        sticky.tick();
        check.test("stuck sticky dynamite burns four ticks per tick", before - sticky.fuse == 4, before + " -> " + sticky.fuse);
        sticky.discard();
        l.getEntitiesOfClass(EntityDynamite.class, new AABB(p).inflate(20)).forEach(net.minecraft.world.entity.Entity::discard);
        actor.getInventory().clearContent();
        boolean recipes = true;
        for (var item : new net.minecraft.world.item.Item[]{Ic2Items.DYNAMITE.get(), Ic2Items.STICKY_DYNAMITE.get(), Ic2Items.DYNAMITE_REMOTE.get()})
            recipes &= l.getRecipeManager().getRecipes().stream().anyMatch(rr -> rr.value().getResultItem(l.registryAccess()).is(item));
        check.test("dynamite recipes and dispenser projectiles", recipes && Ic2Items.DYNAMITE.get() instanceof net.minecraft.world.item.ProjectileItem, "recipes");
    }
}
