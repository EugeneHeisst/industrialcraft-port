package ic2.core.audit;

import ic2.api.item.ElectricItem;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.misc.BlockFoamWall;
import ic2.core.block.misc.TileEntityWall;
import ic2.core.block.wiring.TileEntityCable;
import ic2.core.block.wiring.TileEntityElectricBlock;
import ic2.core.item.HandHeldInventory;
import ic2.core.item.ItemContainmentbox;
import ic2.core.item.tool.ItemObscurator;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Effects;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Obscurator retexturing foam walls and cable foam, containment box, MFSU upgrade kit, their recipes. */
public final class ObscuratorAudit {
    private ObscuratorAudit() {}

    private static void use(ServerLevel l, FakePlayer pl, ItemStack stack, BlockPos pos, Direction face) {
        pl.setItemInHand(InteractionHand.MAIN_HAND, stack);
        stack.getItem().onItemUseFirst(stack, new UseOnContext(l, pl, InteractionHand.MAIN_HAND, stack, new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false)));
    }

    public static void run(ServerLevel l, BlockPos p, CropAudit.Check check) {
        for (var pos : BlockPos.betweenClosed(p.offset(-2, -1, -2), p.offset(8, 2, 2))) l.setBlock(pos, pos.getY() < p.getY() ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
        FakePlayer pl = FakePlayerFactory.getMinecraft(l);
        pl.getInventory().clearContent();
        pl.removeAllEffects();

        ItemStack obs = Ic2Items.OBSCURATOR.get().charged();
        l.setBlock(p, Blocks.GLASS.defaultBlockState(), 3);
        pl.setShiftKeyDown(true);
        use(l, pl, obs, p, Direction.UP);
        check.test("obscurator cannot scan glass", ItemObscurator.referenced(obs, l) == null && ElectricItem.manager.getCharge(obs) == 100000, ElectricItem.manager.getCharge(obs));
        l.setBlock(p, Blocks.OAK_LOG.defaultBlockState(), 3);
        use(l, pl, obs, p, Direction.UP);
        check.test("sneak-click scans a block side for 20000 EU", Blocks.OAK_LOG.defaultBlockState().equals(ItemObscurator.referenced(obs, l)) && ItemObscurator.referencedSide(obs) == Direction.UP && ElectricItem.manager.getCharge(obs) == 80000, ItemObscurator.referenced(obs, l));
        pl.setShiftKeyDown(false);

        BlockPos w = p.east(2);
        l.setBlock(w, Ic2Blocks.CONSTRUCTION_FOAM_WALL.get().defaultBlockState(), 3);
        use(l, pl, obs, w, Direction.NORTH);
        var wall = l.getBlockEntity(w) instanceof TileEntityWall tw ? tw : null;
        check.test("obscurator gives a foam wall side the scanned look for 5000 EU", wall != null && Blocks.OAK_LOG.defaultBlockState().equals(wall.retexture.state(Direction.NORTH)) && wall.retexture.refSide(Direction.NORTH) == Direction.UP
            && wall.retexture.state(Direction.SOUTH) == null && l.getBlockState(w).getValue(BlockFoamWall.RETEXTURED) && ElectricItem.manager.getCharge(obs) == 75000, wall == null ? "no BE" : ElectricItem.manager.getCharge(obs));
        use(l, pl, obs, w, Direction.NORTH);
        check.test("retexturing the same side again costs nothing", ElectricItem.manager.getCharge(obs) == 75000, ElectricItem.manager.getCharge(obs));
        var saved = wall.saveWithoutMetadata(l.registryAccess());
        var copy = new TileEntityWall(w, l.getBlockState(w));
        copy.loadWithComponents(saved, l.registryAccess());
        check.test("foam wall retexture persists", Blocks.OAK_LOG.defaultBlockState().equals(copy.retexture.state(Direction.NORTH)) && copy.retexture.refSide(Direction.NORTH) == Direction.UP, saved);
        var paint = new ic2.api.event.PaintEvent(l, w, Direction.NORTH, 4);
        NeoForge.EVENT_BUS.post(paint);
        check.test("painting a retextured wall keeps its sides", paint.painted && l.getBlockState(w).getValue(BlockFoamWall.COLOR) == 4 && l.getBlockEntity(w) == wall && wall.retexture.state(Direction.NORTH) != null, l.getBlockState(w));
        l.setBlock(w.above(), Blocks.STONE.defaultBlockState(), 3);
        use(l, pl, obs, w.above(), Direction.NORTH);
        check.test("obscurator does nothing on other blocks", ElectricItem.manager.getCharge(obs) == 75000, ElectricItem.manager.getCharge(obs));

        BlockPos c = p.east(4);
        l.setBlock(c, Ic2Blocks.CABLES.get(ic2.core.block.wiring.CableType.insulated_copper).get().defaultBlockState(), 3);
        var cable = (TileEntityCable)l.getBlockEntity(c);
        cable.changeFoam(1);
        use(l, pl, obs, c, Direction.WEST);
        boolean softRefused = cable.retexture.isEmpty();
        cable.changeFoam(2);
        use(l, pl, obs, c, Direction.WEST);
        boolean applied = Blocks.OAK_LOG.defaultBlockState().equals(cable.retexture.state(Direction.WEST));
        cable.changeFoam(0);
        check.test("cable foam takes the look only when hardened and loses it with the foam", softRefused && applied && cable.retexture.isEmpty() && ElectricItem.manager.getCharge(obs) == 70000, ElectricItem.manager.getCharge(obs));

        // containment box: only radioactive items, and they stop irradiating the carrier
        pl.getInventory().clearContent();
        ItemStack box = new ItemStack(Ic2Items.CONTAINMENT_BOX.get());
        pl.setItemInHand(InteractionHand.MAIN_HAND, box);
        var menu = new ItemContainmentbox.Container(0, pl.getInventory(), new HandHeldInventory(pl, InteractionHand.MAIN_HAND, 12));
        ItemStack uranium = new ItemStack(Ic2Items.get("uranium_fuel_rod"));
        check.test("containment box takes radioactive items only", menu.getSlot(0).mayPlace(uranium) && menu.getSlot(11).mayPlace(new ItemStack(Ic2Items.get("uranium_238"))) && !menu.getSlot(0).mayPlace(new ItemStack(Items.DIRT)), "slots");
        menu.getSlot(0).set(uranium.copy());
        menu.removed(pl);
        boolean stored = pl.getMainHandItem().getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyOne().is(uranium.getItem());
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(pl));
        boolean safeInBox = !pl.hasEffect(Ic2Effects.RADIATION);
        pl.getInventory().setItem(5, uranium.copy());
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(pl));
        check.test("radioactive items irradiate in the inventory but not in the containment box", stored && safeInBox && pl.hasEffect(Ic2Effects.RADIATION), "stored=" + stored + " boxed=" + safeInBox);
        pl.removeAllEffects();
        pl.getInventory().clearContent();

        // MFSU upgrade kit
        BlockPos m = p.offset(6, 0, 0);
        l.setBlock(m, Ic2Blocks.MFE.get().defaultBlockState().setValue(BlockTileEntity.FACING, Direction.EAST), 3);
        var mfe = (TileEntityElectricBlock)l.getBlockEntity(m);
        mfe.energy = 123456;
        mfe.redstoneMode = 3;
        mfe.chargeSlot.put(new ItemStack(Ic2Items.RE_BATTERY.get()));
        ItemStack kit = new ItemStack(Ic2Items.MFSU_UPGRADE_KIT.get(), 2);
        use(l, pl, kit, m, Direction.UP);
        var mfsu = l.getBlockEntity(m) instanceof TileEntityElectricBlock b ? b : null;
        int drops = l.getEntitiesOfClass(ItemEntity.class, new AABB(m).inflate(2)).size();
        check.test("upgrade kit turns an MFE into an MFSU keeping energy, facing, mode and items", l.getBlockState(m).is(Ic2Blocks.MFSU.get()) && l.getBlockState(m).getValue(BlockTileEntity.FACING) == Direction.EAST
            && mfsu != null && mfsu.energy == 123456 && mfsu.redstoneMode == 3 && mfsu.chargeSlot.get().is(Ic2Items.RE_BATTERY.get()) && drops == 0 && kit.getCount() == 1, mfsu == null ? "none" : mfsu.energy + " drops " + drops);
        BlockPos cp = m.above();
        l.setBlock(cp, Ic2Blocks.CHARGEPAD_MFE.get().defaultBlockState(), 3);
        ((TileEntityElectricBlock)l.getBlockEntity(cp)).energy = 5000;
        use(l, pl, kit, cp, Direction.UP);
        check.test("upgrade kit turns an MFE charge pad into an MFSU charge pad", l.getBlockState(cp).is(Ic2Blocks.CHARGEPAD_MFSU.get()) && ((TileEntityElectricBlock)l.getBlockEntity(cp)).energy == 5000 && kit.isEmpty(), l.getBlockState(cp));
        use(l, pl, new ItemStack(Ic2Items.MFSU_UPGRADE_KIT.get()), m, Direction.UP);
        check.test("upgrade kit ignores an MFSU", l.getBlockState(m).is(Ic2Blocks.MFSU.get()), "mfsu");
        for (var pos : new BlockPos[]{m, cp}) { if (l.getBlockEntity(pos) instanceof net.minecraft.world.Container ct) ct.clearContent(); l.removeBlock(pos, false); }

        boolean recipes = true;
        for (var it : new Item[]{Ic2Items.CONTAINMENT_BOX.get(), Ic2Items.OBSCURATOR.get(), Ic2Items.MFSU_UPGRADE_KIT.get(), Ic2Blocks.SOLID_HEAT_GENERATOR.get().asItem()})
            recipes &= l.getRecipeManager().getRecipes().stream().anyMatch(r -> r.value().getResultItem(l.registryAccess()).is(it));
        long heat = l.getRecipeManager().getRecipes().stream().filter(r -> r.value().getResultItem(l.registryAccess()).is(Ic2Blocks.SOLID_HEAT_GENERATOR.get().asItem())).count();
        check.test("containment box, obscurator, upgrade kit recipes and both solid heat generator recipes", recipes && heat == 2, heat);
        pl.getInventory().clearContent();
    }
}
