package ic2.core.audit;

import ic2.api.item.ElectricItem;
import ic2.core.ref.Ic2Items;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Electric hoe and the native tool-modification event (the modern UseHoeEvent): a protection listener can cancel or replace
 * the tilled state; the hoe pays its 50 EU only for an allowed till, keeps IC2's mycelium support and needs a full operation charge.
 */
public final class HoeEventAudit {
    private HoeEventAudit() {}

    public static void run(ServerLevel l, BlockPos base, CropAudit.Check check) {
        for (var p : BlockPos.betweenClosed(base.offset(-1, -1, -1), base.offset(8, 2, 1))) l.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        BlockPos denied = base, allowed = base.east(2), mycelium = base.east(4), replaced = base.east(6), poor = base.east(8);
        for (var p : new BlockPos[]{denied, allowed, replaced, poor}) l.setBlock(p, Blocks.DIRT.defaultBlockState(), 3);
        l.setBlock(mycelium, Blocks.MYCELIUM.defaultBlockState(), 3);
        int[] events = {0};
        Consumer<BlockEvent.BlockToolModificationEvent> listener = e -> {
            if (e.getItemAbility() != ItemAbilities.HOE_TILL || e.getLevel() != l) return;
            events[0]++;
            if (e.getPos().equals(denied)) e.setCanceled(true);
            if (e.getPos().equals(replaced)) e.setFinalState(Blocks.DIRT_PATH.defaultBlockState());
        };
        NeoForge.EVENT_BUS.addListener(listener);
        var player = FakePlayerFactory.getMinecraft(l);
        try {
            ItemStack hoe = new ItemStack(Ic2Items.ELECTRIC_HOE.get());
            ElectricItem.manager.charge(hoe, Double.MAX_VALUE, Integer.MAX_VALUE, true, false);
            double full = ElectricItem.manager.getCharge(hoe);
            player.setItemInHand(InteractionHand.MAIN_HAND, hoe);
            java.util.function.Function<BlockPos, BlockState> till = pos -> {
                hoe.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos).add(0, .5, 0), Direction.UP, pos, false)));
                return l.getBlockState(pos);
            };
            check.test("a cancelling protection listener stops the electric hoe: dirt kept, no EU", till.apply(denied).is(Blocks.DIRT) && ElectricItem.manager.getCharge(hoe) == full && events[0] == 1, events[0]);
            check.test("an allowed till makes farmland and pays 50 EU", till.apply(allowed).is(Blocks.FARMLAND) && ElectricItem.manager.getCharge(hoe) == full - 50, ElectricItem.manager.getCharge(hoe));
            check.test("IC2's mycelium tilling is kept and also goes through the event", till.apply(mycelium).is(Blocks.FARMLAND) && events[0] == 3, events[0]);
            check.test("a listener's replacement state is what gets placed", till.apply(replaced).is(Blocks.DIRT_PATH), l.getBlockState(replaced));
            ElectricItem.manager.discharge(hoe, Double.MAX_VALUE, Integer.MAX_VALUE, true, false, false);
            ElectricItem.manager.charge(hoe, 49, Integer.MAX_VALUE, true, false);
            check.test("49 EU (needs 50): no till, no charge taken", till.apply(poor).is(Blocks.DIRT) && ElectricItem.manager.getCharge(hoe) == 49, ElectricItem.manager.getCharge(hoe));
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }
}
