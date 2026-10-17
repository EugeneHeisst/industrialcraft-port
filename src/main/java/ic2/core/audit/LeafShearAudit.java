package ic2.core.audit;

import ic2.api.item.ElectricItem;
import ic2.core.ref.Ic2Items;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Original chainsaw onBlockStartBreak: after the break event, ElectricItem.manager.use refilled the tool from charging armour,
 * paid the operation and made the sheared drop. An empty chainsaw worn with a charged batpack must shear the first leaf it
 * breaks natively (the drop snapshot is taken after the refill), pay exactly one operation, and refill nothing on a cancelled break.
 */
public final class LeafShearAudit {
    private LeafShearAudit() {}

    private static int leaves(ServerLevel l, BlockPos p) {
        int n = 0;
        for (var e : l.getEntitiesOfClass(ItemEntity.class, new AABB(p).inflate(3))) { if (e.getItem().is(Items.OAK_LEAVES)) n += e.getItem().getCount(); e.discard(); }
        return n;
    }

    public static void run(ServerLevel l, BlockPos pos, CropAudit.Check check) {
        var p = FakePlayerFactory.getMinecraft(l);
        var leaf = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        Consumer<BlockEvent.BreakEvent> protect = e -> { if (e.getPos().equals(pos)) e.setCanceled(true); };
        try {
            p.getInventory().clearContent();
            ItemStack pack = new ItemStack(Ic2Items.BATPACK.get());
            ElectricItem.manager.charge(pack, Double.MAX_VALUE, Integer.MAX_VALUE, true, false);
            double packFull = ElectricItem.manager.getCharge(pack);
            p.setItemSlot(EquipmentSlot.CHEST, pack);
            p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.CHAINSAW.get()));

            l.setBlock(pos, leaf, 3);
            NeoForge.EVENT_BUS.addListener(protect);
            boolean broke;
            try { broke = p.gameMode.destroyBlock(pos); } finally { NeoForge.EVENT_BUS.unregister(protect); }
            check.test("a protected leaf is kept and the empty chainsaw is not refilled", !broke && l.getBlockState(pos).is(Blocks.OAK_LEAVES)
                && ElectricItem.manager.getCharge(p.getMainHandItem()) == 0 && ElectricItem.manager.getCharge(p.getItemBySlot(EquipmentSlot.CHEST)) == packFull, ElectricItem.manager.getCharge(p.getMainHandItem()));

            p.gameMode.destroyBlock(pos);
            double total = ElectricItem.manager.getCharge(p.getMainHandItem()) + ElectricItem.manager.getCharge(p.getItemBySlot(EquipmentSlot.CHEST));
            int sheared = leaves(l, pos);
            check.test("empty chainsaw + charged batpack shears the first leaf for exactly 100 EU", l.getBlockState(pos).isAir() && sheared == 1 && Math.abs(total - (packFull - 100)) < 1e-6, "leaves=" + sheared + " total=" + total);
        } finally {
            NeoForge.EVENT_BUS.unregister(protect);
            l.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            p.getInventory().clearContent();
            for (var slot : EquipmentSlot.values()) p.setItemSlot(slot, ItemStack.EMPTY);
        }
    }
}
