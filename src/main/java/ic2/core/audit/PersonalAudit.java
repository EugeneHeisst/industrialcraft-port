package ic2.core.audit;

import com.mojang.authlib.GameProfile;
import ic2.api.item.ElectricItem;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.personal.ContainerEnergyOMat;
import ic2.core.block.personal.ContainerTradeOMat;
import ic2.core.block.personal.TileEntityEnergyOMat;
import ic2.core.block.personal.TileEntityPersonalChest;
import ic2.core.block.personal.TileEntityTradeOMat;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** Personal safe, trade-o-mat and energy-o-mat: ownership, protection, trading through the owner's safe, selling energy. */
public final class PersonalAudit {
    private PersonalAudit() {}

    private static FakePlayer player(ServerLevel l, String name) { return FakePlayerFactory.get(l, new GameProfile(UUID.nameUUIDFromBytes(name.getBytes()), name)); }

    private static <T> T place(ServerLevel l, BlockPos pos, Block block, Direction facing, FakePlayer placer) {
        var state = block.defaultBlockState().setValue(BlockTileEntity.FACING, facing);
        l.setBlock(pos, state, 3);
        block.setPlacedBy(l, pos, state, placer, new ItemStack(block));
        var be = (ic2.core.block.TileEntityBlock)l.getBlockEntity(pos);
        be.onLoaded();
        @SuppressWarnings("unchecked") T t = (T)be;
        return t;
    }

    private static int count(TileEntityPersonalChest safe, net.minecraft.world.item.Item item) {
        int n = 0;
        for (int i = 0; i < safe.contentSlot.size(); i++) if (safe.contentSlot.get(i).is(item)) n += safe.contentSlot.get(i).getCount();
        return n;
    }

    public static void run(ServerLevel l, BlockPos p, CropAudit.Check check) {
        for (var pos : BlockPos.betweenClosed(p.offset(-3, -1, -3), p.offset(6, 2, 9))) l.setBlock(pos, pos.getY() < p.getY() ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
        FakePlayer owner = player(l, "ic2safeowner"), guest = player(l, "ic2safeguest");

        // safe: owned by the placer, closed to others, untouchable for automation, explosions, mobs and other players' wrenches
        TileEntityPersonalChest safe = place(l, p, Ic2Blocks.PERSONAL_CHEST.get(), Direction.SOUTH, owner);
        check.test("safe belongs to its placer", safe.getOwner() != null && safe.getOwner().getId().equals(owner.getUUID()) && safe.permitsAccess(owner.getGameProfile()) && !safe.permitsAccess(guest.getGameProfile()), safe.getOwner());
        var refused = safe.getBlockState().useWithoutItem(l, guest, new BlockHitResult(Vec3.atCenterOf(p), Direction.UP, p, false));
        check.test("another player cannot open the safe", refused == InteractionResult.FAIL, refused);
        var up = l.getCapability(Capabilities.ItemHandler.BLOCK, p, Direction.UP);
        boolean sealed = l.getCapability(Capabilities.ItemHandler.BLOCK, p, null) == null && (up == null || up.insertItem(0, new ItemStack(Items.DIAMOND), true).getCount() == 1);
        safe.contentSlot.put(0, new ItemStack(Items.DIAMOND, 5));
        sealed &= up == null || up.extractItem(0, 5, true).isEmpty();
        check.test("hoppers and pipes cannot reach into the safe", sealed, up);
        var wither = EntityType.WITHER.create(l);
        check.test("safe is unbreakable and blast proof", safe.getBlockState().getDestroySpeed(l, p) < 0 && safe.getBlockState().getBlock().getExplosionResistance() >= 6000000
            && !safe.getBlockState().canEntityDestroy(l, p, wither), safe.getBlockState().getDestroySpeed(l, p));
        check.test("only the owner may wrench the safe, and only once it is empty", !safe.wrenchCanRemove(guest) && !safe.wrenchCanRemove(owner), "full");
        safe.contentSlot.clear();
        check.test("owner wrenches an empty safe", safe.wrenchCanRemove(owner), "empty");
        CompoundTag saved = safe.saveWithoutMetadata(l.registryAccess());
        var copy = new TileEntityPersonalChest(p, safe.getBlockState());
        copy.loadWithComponents(saved, l.registryAccess());
        check.test("safe owner persists", copy.getOwner() != null && copy.getOwner().getId().equals(owner.getUUID()) && copy.getOwner().getName().equals("ic2safeowner"), saved);

        // trade-o-mat next to the owner's safe: 2 emeralds buy 3 apples from the safe, the emeralds go into the safe
        TileEntityTradeOMat trade = place(l, p.east(), Ic2Blocks.TRADE_O_MAT.get(), Direction.SOUTH, owner);
        trade.demandSlot.put(new ItemStack(Items.EMERALD, 2));
        trade.offerSlot.put(new ItemStack(Items.APPLE, 3));
        safe.contentSlot.put(0, new ItemStack(Items.APPLE, 10));
        trade.updateStock();
        check.test("trade-o-mat counts the stock in the owner's safe", trade.stock == 10, trade.stock);
        trade.inputSlot.put(new ItemStack(Items.EMERALD, 2));
        trade.tick();
        check.test("trade-o-mat sells the offer for the demand", trade.outputSlot.get().is(Items.APPLE) && trade.outputSlot.get().getCount() == 3 && trade.inputSlot.isEmpty()
            && count(safe, Items.APPLE) == 7 && count(safe, Items.EMERALD) == 2 && trade.totalTradeCount == 1 && trade.stock == 7,
            trade.outputSlot.get() + " safe apples=" + count(safe, Items.APPLE) + " emeralds=" + count(safe, Items.EMERALD));
        trade.outputSlot.clear();
        trade.inputSlot.put(new ItemStack(Items.EMERALD, 1));
        trade.tick();
        check.test("trade-o-mat waits for a full demand stack", trade.outputSlot.isEmpty() && trade.inputSlot.get().getCount() == 1, trade.inputSlot.get());
        trade.inputSlot.put(new ItemStack(Items.EMERALD, 2));
        safe.contentSlot.clear();
        safe.contentSlot.put(0, new ItemStack(Items.APPLE, 2));
        trade.tick();
        check.test("trade-o-mat refuses to trade without enough stock", trade.outputSlot.isEmpty() && trade.inputSlot.get().getCount() == 2 && count(safe, Items.APPLE) == 2, count(safe, Items.APPLE));
        safe.contentSlot.clear();
        TileEntityPersonalChest foreign = place(l, p.east().south(), Ic2Blocks.PERSONAL_CHEST.get(), Direction.SOUTH, guest);
        foreign.contentSlot.put(0, new ItemStack(Items.APPLE, 64));
        trade.updateStock();
        trade.tick();
        check.test("trade-o-mat never takes stock from another player's safe", trade.outputSlot.isEmpty() && count(foreign, Items.APPLE) == 64 && trade.stock == 0, trade.stock);
        trade.onNetworkEvent(owner, 0);
        check.test("only operators can make a trade-o-mat infinite", !trade.infinite, trade.infinite);
        trade.infinite = true;
        trade.tick();
        check.test("infinite trade-o-mat sells without stock and consumes the payment", trade.outputSlot.get().getCount() == 3 && trade.inputSlot.isEmpty() && count(foreign, Items.APPLE) == 64 && count(safe, Items.EMERALD) == 0 && trade.stock == -1, trade.outputSlot.get());
        var ownerView = (ContainerTradeOMat)trade.getGuiContainer(1, owner.getInventory());
        var guestView = (ContainerTradeOMat)trade.getGuiContainer(2, guest.getInventory());
        check.test("trade-o-mat shows the owner view only to its owner", ownerView.open && !guestView.open && !guestView.slots.get(0).mayPickup(guest), "owner=" + ownerView.open + " guest=" + guestView.open);
        CompoundTag tradeTag = trade.saveWithoutMetadata(l.registryAccess());
        var tradeCopy = new TileEntityTradeOMat(p.east(), trade.getBlockState());
        tradeCopy.loadWithComponents(tradeTag, l.registryAccess());
        check.test("trade-o-mat owner, trade count, infinity and templates persist", tradeCopy.totalTradeCount == 2 && tradeCopy.infinite && tradeCopy.getOwner().getId().equals(owner.getUUID())
            && tradeCopy.demandSlot.get().getCount() == 2 && tradeCopy.offerSlot.get().is(Items.APPLE), tradeTag);
        check.test("guests cannot wrench a trade-o-mat", !trade.wrenchCanRemove(guest) && trade.wrenchCanRemove(owner), "wrench");

        // energy-o-mat: one redstone buys 1000 EU, paid into a chest, taken from cables on any side but the front and handed out through the front or charge slot
        BlockPos e = p.offset(0, 0, 5);
        TileEntityEnergyOMat omat = place(l, e, Ic2Blocks.ENERGY_O_MAT.get(), Direction.NORTH, owner);
        l.setBlock(e.east(), Blocks.CHEST.defaultBlockState(), 3);
        omat.demandSlot.put(new ItemStack(Items.REDSTONE));
        check.test("energy-o-mat takes no energy before it is paid", omat.getDemandedEnergy() == 0 && omat.injectEnergy(Direction.WEST, 32, 32) == 32, omat.getDemandedEnergy());
        omat.inputSlot.put(new ItemStack(Items.REDSTONE, 2));
        omat.tick();
        var chest = (ChestBlockEntity)l.getBlockEntity(e.east());
        check.test("energy-o-mat payment buys euOffer EU and goes into the adjacent chest", omat.paidFor == 1000 && omat.inputSlot.get().getCount() == 1 && chest.getItem(0).is(Items.REDSTONE), omat.paidFor);
        double left = omat.injectEnergy(Direction.WEST, 512, 128);
        check.test("energy-o-mat accepts only what was paid for", left == 0 && omat.paidFor == 488 && omat.euBuffer == 512 && omat.injectEnergy(Direction.WEST, 1000, 128) == 512 && omat.paidFor == 0, omat.paidFor + " " + omat.euBuffer);
        check.test("energy-o-mat emits only through its front at its tier", omat.emitsEnergyTo(null, Direction.NORTH) && !omat.emitsEnergyTo(null, Direction.WEST) && !omat.acceptsEnergyFrom(null, Direction.NORTH)
            && omat.acceptsEnergyFrom(null, Direction.EAST) && omat.getOfferedEnergy() == 32, omat.getOfferedEnergy());
        ItemStack battery = new ItemStack(Ic2Items.RE_BATTERY.get());
        omat.chargeSlot.put(battery);
        double before = omat.euBuffer;
        omat.tick();
        check.test("energy-o-mat charges the item in its charge slot", omat.euBuffer < before && ElectricItem.manager.getCharge(omat.chargeSlot.get()) > 0, before + " -> " + omat.euBuffer);
        omat.onNetworkEvent(guest, 6);
        int afterGuest = omat.euOffer;
        omat.onNetworkEvent(owner, 6);
        int raised = omat.euOffer;
        omat.onNetworkEvent(owner, 0);
        check.test("only the owner sets the energy price, never below 100 EU", afterGuest == 1000 && raised == 2000 && omat.euOffer == 100, afterGuest + "/" + raised + "/" + omat.euOffer);
        omat.upgradeSlot.put(new ItemStack(Ic2Items.ENERGY_STORAGE_UPGRADE.get()));
        omat.tick();
        int storage = omat.getEuBufferMax();
        omat.upgradeSlot.put(new ItemStack(Ic2Items.TRANSFORMER_UPGRADE.get()));
        omat.tick();
        check.test("energy-o-mat storage and transformer upgrades", storage == 20000 && omat.getSourceTier() == 2 && omat.chargeSlot.tier == 2, storage + " tier " + omat.getSourceTier());
        var guestOmat = (ContainerEnergyOMat)omat.getGuiContainer(3, guest.getInventory());
        check.test("energy-o-mat customer view hides upgrade and price controls", !guestOmat.open && guestOmat.slots.size() == 3 + 36, guestOmat.slots.size());
        omat.onUnloaded();
        trade.onUnloaded();

        boolean recipes = true;
        for (var block : new Block[]{Ic2Blocks.PERSONAL_CHEST.get(), Ic2Blocks.TRADE_O_MAT.get(), Ic2Blocks.ENERGY_O_MAT.get()})
            recipes &= l.getRecipeManager().getRecipes().stream().anyMatch(r -> r.value().getResultItem(l.registryAccess()).is(block.asItem()));
        check.test("personal safe, trade-o-mat and energy-o-mat recipes", recipes, "recipes");
        for (var pos : BlockPos.betweenClosed(p.offset(-3, 0, -3), p.offset(6, 2, 9))) { if (l.getBlockEntity(pos) instanceof net.minecraft.world.Container c) c.clearContent(); l.removeBlock(pos, false); }
    }
}
