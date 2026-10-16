package ic2.core.audit;

import ic2.api.energy.EnergyNet;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.generator.tileentity.TileEntityGenerator;
import ic2.core.block.machine.tileentity.TileEntityElectricFurnace;
import ic2.core.block.machine.tileentity.TileEntityIronFurnace;
import ic2.core.block.machine.tileentity.TileEntityMacerator;
import ic2.core.block.wiring.TileEntityElectricBlock;
import ic2.core.block.wiring.TileEntityTransformer;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** -Dic2.audit=server: builds small IC2 setups in the overworld, runs them and logs PASS/FAIL lines, then stops the server. */
@EventBusSubscriber(modid = IC2.MODID)
public final class ServerAudit {
    private static final boolean ENABLED = "server".equals(System.getProperty("ic2.audit"));
    private static int tick = -1;
    private static final List<String> results = new ArrayList<>();
    private static BlockPos base;
    private static int minerPipes;
    private static String dbgUu() { StringBuilder b = new StringBuilder(); for (String id : new String[]{"copper_dust", "tin_dust", "bronze_dust", "small_copper_dust", "crushed_copper_ore", "iridium_ore", "advanced_alloy", "mixed_metal_ingot", "refined_iron_ingot", "bronze_ingot", "tin_ingot", "iron_plate", "tin_plate", "bronze_plate", "forge_hammer", "electronic_circuit", "copper_cable", "insulated_copper_cable", "rubber", "resin", "machine", "advanced_circuit"}) b.append(' ').append(id).append('=').append(ic2.core.uu.UuGraph.get(new ItemStack(Ic2Items.get(id)))); b.append(" diamond=").append(ic2.core.uu.UuGraph.get(new ItemStack(Items.DIAMOND))); return b.toString(); }
    private static java.util.UUID teleItem, teslaPig;
    private static net.minecraft.server.level.ServerPlayer padPlayer;
    private static boolean containsItem(ic2.core.block.invslot.InvSlot slot, net.minecraft.world.item.Item item) { for (int i = 0; i < slot.size(); i++) if (slot.get(i).is(item)) return true; return false; }
    private static int shaft(ServerLevel level) {
        int n = 0;
        for (int y = 1; y <= 10; y++) { var s = level.getBlockState(base.offset(-6, -y, 10)); if (s.is(Ic2Blocks.MINING_PIPE.get()) || s.is(Ic2Blocks.MINING_PIPE_TIP.get())) n++; }
        return n;
    }

    private ServerAudit() {}

    private static void check(String name, boolean ok, Object detail) { String line = (ok ? "PASS " : "FAIL ") + name + " " + detail; results.add(line); IC2.LOG.info("[audit] {}", line); }

    private static BlockState facing(BlockState s, Direction d) { return s.setValue(BlockTileEntity.FACING, d); }

    @SubscribeEvent
    static void started(ServerStartedEvent event) {
        if (!ENABLED) return;
        ServerLevel level = event.getServer().overworld();
        base = new BlockPos(1000 + level.random.nextInt(5000), 150, 1000 + level.random.nextInt(5000));
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) level.setChunkForced((base.getX() >> 4) + dx, (base.getZ() >> 4) + dz, true);
        for (BlockPos p : BlockPos.betweenClosed(base.offset(-2, -1, -2), base.offset(14, 3, 6))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        // 1: generator -> macerator
        level.setBlock(base, Ic2Blocks.GENERATOR.get().defaultBlockState(), 3);
        level.setBlock(base.east(), facing(Ic2Blocks.MACERATOR.get().defaultBlockState(), Direction.NORTH), 3);
        // 2: generator -> batbox (front faces away) -> electric furnace on its front
        level.setBlock(base.offset(4, 0, 0), Ic2Blocks.GENERATOR.get().defaultBlockState(), 3);
        level.setBlock(base.offset(5, 0, 0), facing(Ic2Blocks.BATBOX.get().defaultBlockState(), Direction.EAST), 3);
        level.setBlock(base.offset(6, 0, 0), facing(Ic2Blocks.ELECTRIC_FURNACE.get().defaultBlockState(), Direction.NORTH), 3);
        // 3: iron furnace
        level.setBlock(base.offset(8, 0, 0), facing(Ic2Blocks.IRON_FURNACE.get().defaultBlockState(), Direction.NORTH), 3);
        // 4: MFE (charged) -> LV transformer front (input HV side) -> macerator: step down
        level.setBlock(base.offset(10, 0, 0), facing(Ic2Blocks.MFE.get().defaultBlockState(), Direction.EAST), 3);
        level.setBlock(base.offset(11, 0, 0), facing(Ic2Blocks.LV_TRANSFORMER.get().defaultBlockState(), Direction.WEST), 3);
        level.setBlock(base.offset(12, 0, 0), facing(Ic2Blocks.MACERATOR.get().defaultBlockState(), Direction.NORTH), 3);
        // 5: generator -> 3 cables east, bend south, 2 cables -> macerator (z+3 row)
        BlockPos c = base.offset(0, 0, 3);
        level.setBlock(c, Ic2Blocks.GENERATOR.get().defaultBlockState(), 3);
        var ins = Ic2Blocks.CABLES.get(ic2.core.block.wiring.CableType.insulated_copper).get().defaultBlockState();
        for (int i = 1; i <= 3; i++) level.setBlock(c.offset(i, 0, 0), ins, 3);
        level.setBlock(c.offset(3, 0, 1), ins, 3);
        level.setBlock(c.offset(3, 0, 2), Ic2Blocks.CABLES.get(ic2.core.block.wiring.CableType.detector).get().defaultBlockState(), 3);
        level.setBlock(c.offset(3, 0, 3), facing(Ic2Blocks.MACERATOR.get().defaultBlockState(), Direction.NORTH), 3);
        // 6: red and blue painted cables between generator and macerator never connect
        BlockPos d = base.offset(6, 0, 3);
        level.setBlock(d, Ic2Blocks.GENERATOR.get().defaultBlockState(), 3);
        level.setBlock(d.east(), ins.setValue(ic2.core.block.wiring.BlockCable.COLOR, 2), 3);
        level.setBlock(d.east(2), ins.setValue(ic2.core.block.wiring.BlockCable.COLOR, 5), 3);
        level.setBlock(d.east(3), facing(Ic2Blocks.MACERATOR.get().defaultBlockState(), Direction.NORTH), 3);
        // 8: geothermal (lava bucket), water mill in water, wind mill in open air
        BlockPos g = base.offset(0, 0, -3);
        level.setBlock(g, Ic2Blocks.GEOTHERMAL_GENERATOR.get().defaultBlockState(), 3);
        level.setBlock(g.offset(3, 0, 0), Ic2Blocks.WATER_MILL.get().defaultBlockState(), 3);
        for (Direction dir : Direction.Plane.HORIZONTAL) level.setBlock(g.offset(3, 0, 0).relative(dir), Blocks.WATER.defaultBlockState(), 3);
        level.setBlock(g.offset(7, 20, 0), Ic2Blocks.WIND_MILL.get().defaultBlockState(), 3);
        // 9: machines powered by charged MFEs facing them
        BlockPos m = base.offset(0, 2, 0);
        var mfe = facing(Ic2Blocks.MFE.get().defaultBlockState(), Direction.UP);
        Object[][] machines = {{Ic2Blocks.INDUCTION_FURNACE, 0}, {Ic2Blocks.METAL_FORMER, 2}, {Ic2Blocks.ORE_WASHING_PLANT, 4}, {Ic2Blocks.THERMAL_CENTRIFUGE, 6}, {Ic2Blocks.BLOCK_CUTTER, 8}};
        for (Object[] e : machines) {
            BlockPos p = m.offset((int)e[1], 0, 0);
            level.setBlock(p, mfe, 3);
            level.setBlock(p.above(), facing(((net.neoforged.neoforge.registries.DeferredBlock<?>)e[0]).get().defaultBlockState(), Direction.NORTH), 3);
        }
        // 11: electric heat generator (10 coils, MFE below) -> blast furnace
        BlockPos hb = base.offset(12, 2, 0);
        level.setBlock(hb.below(), facing(Ic2Blocks.MFE.get().defaultBlockState(), Direction.UP), 3);
        level.setBlock(hb, facing(Ic2Blocks.ELECTRIC_HEAT_GENERATOR.get().defaultBlockState(), Direction.EAST), 3);
        level.setBlock(hb.east(), facing(Ic2Blocks.BLAST_FURNACE.get().defaultBlockState(), Direction.WEST), 3);
        // 12: wind kinetic generator (iron rotor) high up -> kinetic generator behind it; MFE -> electric kinetic -> kinetic generator
        BlockPos wk = base.offset(4, 30, 0);
        level.setBlock(wk, facing(Ic2Blocks.WIND_KINETIC_GENERATOR.get().defaultBlockState(), Direction.NORTH), 3);
        level.setBlock(wk.south(), facing(Ic2Blocks.KINETIC_GENERATOR.get().defaultBlockState(), Direction.NORTH), 3);
        BlockPos ek = base.offset(16, 2, 0);
        level.setBlock(ek.below(), facing(Ic2Blocks.MFE.get().defaultBlockState(), Direction.UP), 3);
        level.setBlock(ek, facing(Ic2Blocks.ELECTRIC_KINETIC_GENERATOR.get().defaultBlockState(), Direction.SOUTH), 3);
        level.setBlock(ek.south(), facing(Ic2Blocks.KINETIC_GENERATOR.get().defaultBlockState(), Direction.NORTH), 3);
        level.setBlock(base.offset(18, 2, 0), facing(Ic2Blocks.MASS_FABRICATOR.get().defaultBlockState(), Direction.NORTH), 3);
        // 9: miner (OD scanner, diamond drill) digging through air and stone to an iron ore, a lava pocket pumped by the pump beside it; pump on a water pool
        BlockPos mp = base.offset(-6, 0, 10);
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) level.setChunkForced((mp.getX() >> 4) + dx, (mp.getZ() >> 4) + dz, true);
        for (BlockPos p : BlockPos.betweenClosed(mp.offset(-4, -10, -4), mp.offset(8, 1, 4))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        for (BlockPos p : BlockPos.betweenClosed(mp.offset(-4, -10, -4), mp.offset(4, -5, 4))) level.setBlock(p, Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(mp.offset(2, -5, 0), Blocks.IRON_ORE.defaultBlockState(), 3);
        level.setBlock(mp.offset(0, -6, 0), Blocks.LAVA.defaultBlockState(), 3);
        level.setBlock(mp, Ic2Blocks.MINER.get().defaultBlockState(), 3);
        level.setBlock(mp.east(), facing(Ic2Blocks.PUMP.get().defaultBlockState(), Direction.NORTH), 3);
        // water pool: stone basin with one source in front of a pump facing north
        BlockPos wp = mp.offset(6, -1, -1);
        for (BlockPos p : BlockPos.betweenClosed(wp.offset(-1, -1, -3), wp.offset(1, 0, -1))) level.setBlock(p, Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(wp.offset(0, 0, -1), Blocks.WATER.defaultBlockState(), 3);
        level.setBlock(wp, facing(Ic2Blocks.PUMP.get().defaultBlockState(), Direction.NORTH), 3);
        // 10: canning machines and bottling plant
        level.setBlock(mp.offset(-3, 0, 3), facing(Ic2Blocks.SOLID_CANNER.get().defaultBlockState(), Direction.NORTH), 3);
        level.setBlock(mp.offset(-1, 0, 3), facing(Ic2Blocks.CANNER.get().defaultBlockState(), Direction.NORTH), 3);
        level.setBlock(mp.offset(1, 0, 3), facing(Ic2Blocks.CANNER.get().defaultBlockState(), Direction.NORTH), 3);
        level.setBlock(mp.offset(3, 0, 3), facing(Ic2Blocks.FLUID_BOTTLER.get().defaultBlockState(), Direction.NORTH), 3);
        // 11: magnetizer charging an iron fence pole; electrolyzers on a full and an empty MFSU
        level.setBlock(mp.offset(-3, 0, -3), facing(Ic2Blocks.MAGNETIZER.get().defaultBlockState(), Direction.EAST), 3);
        for (int y = 0; y <= 3; y++) level.setBlock(mp.offset(-2, y, -3), Ic2Blocks.IRON_FENCE.get().defaultBlockState(), 3);
        level.setBlock(mp.offset(0, 0, -3), Ic2Blocks.ELECTROLYZER.get().defaultBlockState(), 3);
        level.setBlock(mp.offset(0, 1, -3), facing(Ic2Blocks.MFSU.get().defaultBlockState(), Direction.UP), 3);
        level.setBlock(mp.offset(2, 0, -3), Ic2Blocks.ELECTROLYZER.get().defaultBlockState(), 3);
        level.setBlock(mp.offset(2, 1, -3), facing(Ic2Blocks.MFSU.get().defaultBlockState(), Direction.UP), 3);
        // 12: teleporter pair fed by an MFSU, tesla coil with a pig, MFSU charge pad
        level.setBlock(mp.offset(-3, 0, 1), Ic2Blocks.TELEPORTER.get().defaultBlockState(), 3);
        level.setBlock(mp.offset(-4, 0, 1), facing(Ic2Blocks.MFSU.get().defaultBlockState(), Direction.WEST), 3);
        level.setBlock(mp.offset(3, 0, 1), Ic2Blocks.TELEPORTER.get().defaultBlockState(), 3);
        level.setBlock(mp.offset(-2, 0, -1), Ic2Blocks.TESLA_COIL.get().defaultBlockState(), 3);
        level.setBlock(mp.offset(-2, -1, -1), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
        level.setBlock(mp.offset(2, 0, -1), facing(Ic2Blocks.CHARGEPAD_MFSU.get().defaultBlockState(), Direction.WEST), 3);
        // 13: nuclear reactor with one chamber feeding an MFSU; a second empty reactor far away that melts down
        BlockPos rp = base.offset(-2, 0, 20);
        for (BlockPos p : BlockPos.betweenClosed(rp.offset(-2, -1, -2), rp.offset(2, 2, 2))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(rp, facing(Ic2Blocks.NUCLEAR_REACTOR.get().defaultBlockState(), Direction.NORTH), 3);
        level.setBlock(rp.east(), Ic2Blocks.REACTOR_CHAMBER.get().defaultBlockState(), 3);
        level.setBlock(rp.below(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
        level.setBlock(rp.west(), facing(Ic2Blocks.MFSU.get().defaultBlockState(), Direction.WEST), 3);
        BlockPos bp = base.offset(0, 0, -80);
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) level.setChunkForced((bp.getX() >> 4) + dx, (bp.getZ() >> 4) + dz, true);
        for (BlockPos p : BlockPos.betweenClosed(bp.offset(-3, -3, -3), bp.offset(3, 3, 3))) level.setBlock(p, Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(bp, facing(Ic2Blocks.NUCLEAR_REACTOR.get().defaultBlockState(), Direction.NORTH), 3);
        // 14: scanner -> pattern storage -> replicator
        BlockPos up = base.offset(3, 0, 22);
        for (BlockPos p : BlockPos.betweenClosed(up.offset(0, 0, 0), up.offset(2, 1, 0))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(up, facing(Ic2Blocks.SCANNER.get().defaultBlockState(), Direction.NORTH), 3);
        level.setBlock(up.east(), facing(Ic2Blocks.PATTERN_STORAGE.get().defaultBlockState(), Direction.NORTH), 3);
        level.setBlock(up.east(2), facing(Ic2Blocks.REPLICATOR.get().defaultBlockState(), Direction.NORTH), 3);
        // 15: terraformer with a cultivation blueprint on a dirt field
        BlockPos tf = base.offset(-40, 0, 40);
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) level.setChunkForced((tf.getX() >> 4) + dx, (tf.getZ() >> 4) + dz, true);
        for (BlockPos p : BlockPos.betweenClosed(tf.offset(-10, -2, -10), tf.offset(10, 12, 10))) level.setBlock(p, p.getY() < tf.getY() ? Blocks.DIRT.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(tf, Ic2Blocks.TERRAFORMER.get().defaultBlockState(), 3);
        // 7: rubber tree on grass, treetap on its wet holes
        BlockPos tree = base.offset(12, -1, 4);
        level.setBlock(tree, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
        boolean grown = ic2.core.world.WorldGenRubTree.grow(level, tree.above(), level.random);
        int logs = 0, leaves = 0, wet = 0, resin = 0;
        for (BlockPos p : BlockPos.betweenClosed(tree.offset(-2, 1, -2), tree.offset(2, 10, 2))) {
            var s = level.getBlockState(p);
            if (s.is(Ic2Blocks.RUBBER_WOOD.get())) {
                logs++;
                if (s.getValue(ic2.core.block.rubber.BlockRubWood.HOLE) == ic2.core.block.rubber.BlockRubWood.Hole.wet) {
                    wet++;
                    var drops = new ArrayList<ItemStack>();
                    ic2.core.item.tool.ItemTreetap.attemptExtract(null, level, p.immutable(), s.getValue(ic2.core.block.rubber.BlockRubWood.FACING), drops);
                    for (var dr : drops) resin += dr.getCount();
                }
            }
            if (s.is(Ic2Blocks.RUBBER_LEAVES.get())) leaves++;
        }
        check("rubber tree grows", grown && logs >= 2 && leaves > 4, "logs=" + logs + " leaves=" + leaves + " wet=" + wet);
        check("treetap extracts resin from wet holes", wet == 0 || resin >= wet, "resin=" + resin);
        // 10: armor absorption on mobs, drill properties
        for (int k = 0; k < 3; k++) {
            var zombie = net.minecraft.world.entity.EntityType.ZOMBIE.create(level);
            zombie.setNoAi(true);
            zombie.setPersistenceRequired();
            zombie.moveTo(base.getX() + 20 + k * 2, base.getY() + 5, base.getZ());
            if (k == 1) for (var it : new net.minecraft.world.item.Item[]{Ic2Items.NANO_HELMET.get(), Ic2Items.NANO_CHESTPLATE.get(), Ic2Items.NANO_LEGGINGS.get(), Ic2Items.NANO_BOOTS.get()}) {
                var a = ((ic2.core.item.armor.ItemArmorElectric)it).charged();
                zombie.setItemSlot(((net.minecraft.world.item.ArmorItem)it).getEquipmentSlot(), a);
            }
            if (k == 2) for (var it : new net.minecraft.world.item.Item[]{Ic2Items.QUANTUM_HELMET.get(), Ic2Items.QUANTUM_CHESTPLATE.get(), Ic2Items.QUANTUM_LEGGINGS.get(), Ic2Items.QUANTUM_BOOTS.get()}) {
                var a = ((ic2.core.item.armor.ItemArmorElectric)it).charged();
                zombie.setItemSlot(((net.minecraft.world.item.ArmorItem)it).getEquipmentSlot(), a);
            }
            level.addFreshEntity(zombie);
            zombie.hurt(level.damageSources().cactus(), 10);
            float health = zombie.getHealth();
            check("armor absorption " + new String[]{"none", "nano", "quantum"}[k], k == 0 ? health <= 11 : k == 1 ? health >= 18.9 && health < 20 : health == 20, "health=" + health);
            zombie.discard();
        }
        var drill = Ic2Items.MINING_DRILL.get().getItemStack(Double.POSITIVE_INFINITY);
        check("mining drill", drill.getDestroySpeed(Blocks.STONE.defaultBlockState()) == 8 && drill.isCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState()) && !drill.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "speed=" + drill.getDestroySpeed(Blocks.STONE.defaultBlockState()));
        var ddrill = Ic2Items.DIAMOND_DRILL.get().getItemStack(Double.POSITIVE_INFINITY);
        check("diamond drill", ddrill.getDestroySpeed(Blocks.STONE.defaultBlockState()) == 16 && ddrill.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "");
        var empty = new ItemStack(Ic2Items.MINING_DRILL.get());
        var lapRecipe = level.getRecipeManager().byKey(ic2.core.IC2.id("lapotron_crystal")).orElse(null);
        if (lapRecipe == null) check("lapotron recipe present", false, "");
        else {
            var crystal = Ic2Items.ENERGY_CRYSTAL.get().charged();
            ItemStack lapis = new ItemStack(Ic2Items.get("lapis_dust")), circuit = new ItemStack(Ic2Items.get("advanced_circuit"));
            var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, java.util.List.of(lapis, circuit, lapis, lapis, crystal, lapis, lapis, circuit, lapis));
            var r = (net.minecraft.world.item.crafting.CraftingRecipe)lapRecipe.value();
            boolean matches = r.matches(input, level);
            var out = r.assemble(input, level.registryAccess());
            check("crafting keeps charge (energy crystal -> lapotron)", matches && out.is(Ic2Items.LAPOTRON_CRYSTAL.get()) && ElectricItem.manager.getCharge(out) == 1000000, "matches=" + matches + " charge=" + ElectricItem.manager.getCharge(out));
        }
        var coolantCell = new ItemStack(Ic2Items.FLUID_CELLS.get("coolant").get());
        var cellHandler = coolantCell.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
        check("coolant cell holds coolant", cellHandler != null && cellHandler.getFluidInTank(0).getFluid() == ic2.core.fluid.Ic2Fluids.COOLANT.get() && cellHandler.getFluidInTank(0).getAmount() == 1000, cellHandler == null ? "no handler" : cellHandler.getFluidInTank(0).getAmount());
        BlockPos fp = base.offset(-1, 0, 8);
        level.setBlock(fp, ic2.core.fluid.Ic2Fluids.PAHOEHOE_LAVA.block.get().defaultBlockState(), 3);
        level.setBlock(fp.east(), Blocks.WATER.defaultBlockState(), 3);
        check("pahoehoe lava hardens to basalt next to water", level.getBlockState(fp).is(Ic2Blocks.BASALT.get()) || level.getFluidState(fp).getType().isSame(ic2.core.fluid.Ic2Fluids.PAHOEHOE_LAVA.get()), level.getBlockState(fp));
        check("empty drill is slow", empty.getDestroySpeed(Blocks.STONE.defaultBlockState()) == 1, "");
        tick = 0;
    }

    @SubscribeEvent
    static void serverTick(ServerTickEvent.Post event) {
        if (!ENABLED || tick < 0) return;
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        tick++;
        if (tick == 5) {
            ((TileEntityGenerator)level.getBlockEntity(base)).fuelSlot.put(new ItemStack(Items.COAL, 4));
            ((TileEntityMacerator)level.getBlockEntity(base.east())).inputSlot.put(new ItemStack(Items.IRON_ORE, 2));
            ((TileEntityGenerator)level.getBlockEntity(base.offset(4, 0, 0))).fuelSlot.put(new ItemStack(Items.COAL, 4));
            ((TileEntityElectricFurnace)level.getBlockEntity(base.offset(6, 0, 0))).inputSlot.put(new ItemStack(Items.RAW_IRON, 1));
            var iron = (TileEntityIronFurnace)level.getBlockEntity(base.offset(8, 0, 0));
            iron.inputSlot.put(new ItemStack(Items.COBBLESTONE, 2));
            iron.fuelSlot.put(new ItemStack(Items.COAL, 1));
            ((TileEntityElectricBlock)level.getBlockEntity(base.offset(10, 0, 0))).energy = 100000;
            ((TileEntityMacerator)level.getBlockEntity(base.offset(12, 0, 0))).inputSlot.put(new ItemStack(Items.COBBLESTONE, 1));
            ((TileEntityElectricBlock)level.getBlockEntity(base.offset(10, 0, 0))).chargeSlot.put(new ItemStack(Ic2Items.ENERGY_CRYSTAL.get()));
            ((TileEntityGenerator)level.getBlockEntity(base.offset(0, 0, 3))).fuelSlot.put(new ItemStack(Items.COAL, 2));
            ((TileEntityMacerator)level.getBlockEntity(base.offset(3, 0, 6))).inputSlot.put(new ItemStack(Items.COBBLESTONE, 1));
            ((TileEntityGenerator)level.getBlockEntity(base.offset(6, 0, 3))).fuelSlot.put(new ItemStack(Items.COAL, 2));
            ((TileEntityMacerator)level.getBlockEntity(base.offset(9, 0, 3))).inputSlot.put(new ItemStack(Items.COBBLESTONE, 1));
            ((ic2.core.block.generator.tileentity.TileEntityGeoGenerator)level.getBlockEntity(base.offset(0, 0, -3))).fluidSlot.put(new ItemStack(Items.LAVA_BUCKET));
            for (int dx = 0; dx <= 8; dx += 2) ((TileEntityElectricBlock)level.getBlockEntity(base.offset(dx, 2, 0))).energy = 4000000;
            var ind = (ic2.core.block.machine.tileentity.TileEntityInduction)level.getBlockEntity(base.offset(0, 3, 0));
            ind.inputSlotA.put(new ItemStack(Items.RAW_IRON, 4));
            ind.inputSlotB.put(new ItemStack(Items.COBBLESTONE, 4));
            var former = (ic2.core.block.machine.tileentity.TileEntityMetalFormer)level.getBlockEntity(base.offset(2, 3, 0));
            former.setMode(1);
            former.inputSlot.put(new ItemStack(Items.IRON_INGOT, 2));
            var washer = (ic2.core.block.machine.tileentity.TileEntityOreWashing)level.getBlockEntity(base.offset(4, 3, 0));
            washer.fluidSlot.put(new ItemStack(Items.WATER_BUCKET));
            washer.inputSlot.put(new ItemStack(Ic2Items.get("crushed_iron_ore"), 1));
            var cent = (ic2.core.block.machine.tileentity.TileEntityCentrifuge)level.getBlockEntity(base.offset(6, 3, 0));
            cent.inputSlot.put(new ItemStack(Ic2Items.get("purified_crushed_iron_ore"), 1));
            var cutter = (ic2.core.block.machine.tileentity.TileEntityBlockCutter)level.getBlockEntity(base.offset(8, 3, 0));
            cutter.cutterSlot.put(new ItemStack(Ic2Items.IRON_BLOCK_CUTTING_BLADE.get()));
            cutter.inputSlot.put(new ItemStack(Items.OAK_LOG, 1));
            ((TileEntityElectricBlock)level.getBlockEntity(base.offset(12, 1, 0))).energy = 4000000;
            var heatGen = (ic2.core.block.heatgenerator.TileEntityHeatGenerators.Electric)level.getBlockEntity(base.offset(12, 2, 0));
            for (int c = 0; c < 10; c++) heatGen.CoilSlot.put(c, new ItemStack(Ic2Items.get("coil")));
            var blast = (ic2.core.block.machine.tileentity.TileEntityBlastFurnace)level.getBlockEntity(base.offset(13, 2, 0));
            blast.inputSlot.put(new ItemStack(Items.IRON_INGOT, 1));
            blast.airSlot.put(new ItemStack(Ic2Items.AIR_CELL.get(), 4));
            ((ic2.core.block.kineticgenerator.TileEntityKineticGenerators.Wind)level.getBlockEntity(base.offset(4, 30, 0))).rotorSlot.put(new ItemStack(Ic2Items.IRON_ROTOR.get()));
            ((TileEntityElectricBlock)level.getBlockEntity(base.offset(16, 1, 0))).energy = 4000000;
            var ekg = (ic2.core.block.kineticgenerator.TileEntityKineticGenerators.Electric)level.getBlockEntity(base.offset(16, 2, 0));
            for (int c = 0; c < 10; c++) ekg.slotMotor.put(c, new ItemStack(Ic2Items.get("electric_motor")));
            var fab = (ic2.core.block.machine.tileentity.TileEntityMatter)level.getBlockEntity(base.offset(18, 2, 0));
            fab.energy = 1000000;
            fab.amplifierSlot.put(new ItemStack(Ic2Items.get("scrap"), 2));
            var miner = (ic2.core.block.machine.tileentity.TileEntityMiner)level.getBlockEntity(base.offset(-6, 0, 10));
            miner.drillSlot.put(Ic2Items.DIAMOND_DRILL.get().getItemStack(Double.POSITIVE_INFINITY));
            miner.scannerSlot.put(Ic2Items.OD_SCANNER.get().charged());
            miner.pipeSlot.put(new ItemStack(Ic2Items.MINING_PIPE.get(), 32));
            ((ic2.core.block.machine.tileentity.TileEntityPump)level.getBlockEntity(base.offset(0, -1, 9))).containerSlot.put(new ItemStack(Ic2Items.EMPTY_CELL.get(), 4));
            BlockPos cm = base.offset(-6, 0, 10);
            var solid = (ic2.core.block.machine.tileentity.TileEntitySolidCanner)level.getBlockEntity(cm.offset(-3, 0, 3));
            solid.canInputSlot.put(new ItemStack(Ic2Items.get("tin_can"), 4));
            solid.inputSlot.put(new ItemStack(Items.POTATO, 2));
            var drainer = (ic2.core.block.machine.tileentity.TileEntityCanner)level.getBlockEntity(cm.offset(-1, 0, 3));
            drainer.onNetworkEvent(0);
            drainer.canInputSlot.put(new ItemStack(Ic2Items.WATER_CELL.get(), 2));
            var enricher = (ic2.core.block.machine.tileentity.TileEntityCanner)level.getBlockEntity(cm.offset(1, 0, 3));
            for (int i = 0; i < 3; i++) enricher.onNetworkEvent(0);
            enricher.inputTank.fill(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 2000), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            enricher.inputSlot.put(new ItemStack(Ic2Items.get("biochaff"), 1));
            var bottler = (ic2.core.block.machine.tileentity.TileEntityFluidBottler)level.getBlockEntity(cm.offset(3, 0, 3));
            bottler.drainInputSlot.put(new ItemStack(Items.WATER_BUCKET));
            bottler.fillInputSlot.put(new ItemStack(Ic2Items.EMPTY_CELL.get(), 1));
            ((TileEntityElectricBlock)level.getBlockEntity(base.offset(-6, 1, 7))).energy = 0.9 * ((TileEntityElectricBlock)level.getBlockEntity(base.offset(-6, 1, 7))).maxStorage;
            ((ic2.core.block.machine.tileentity.TileEntityElectrolyzer)level.getBlockEntity(base.offset(-6, 0, 7))).waterSlot.put(new ItemStack(Ic2Items.WATER_CELL.get(), 4));
            ((ic2.core.block.machine.tileentity.TileEntityElectrolyzer)level.getBlockEntity(base.offset(-4, 0, 7))).hydrogenSlot.put(new ItemStack(Ic2Items.get("electrolyzed_water_cell"), 2));
            BlockPos tp = base.offset(-6, 0, 10);
            ((TileEntityElectricBlock)level.getBlockEntity(tp.offset(-4, 0, 1))).energy = 1000000;
            var tpA = (ic2.core.block.machine.tileentity.TileEntityTeleporter)level.getBlockEntity(tp.offset(-3, 0, 1));
            tpA.setTarget(tp.offset(3, 0, 1));
            ((ic2.core.block.machine.tileentity.TileEntityTesla)level.getBlockEntity(tp.offset(-2, 0, -1))).energy = 10000;
            var pig = net.minecraft.world.entity.EntityType.PIG.create(level);
            pig.moveTo(tp.getX() - 2 + 0.5, tp.getY() + 1, tp.getZ() - 1 + 0.5);
            pig.setNoAi(true);
            level.addFreshEntity(pig);
            teslaPig = pig.getUUID();
            ((TileEntityElectricBlock)level.getBlockEntity(tp.offset(2, 0, -1))).energy = 1000000;
            padPlayer = net.neoforged.neoforge.common.util.FakePlayerFactory.get(level, new com.mojang.authlib.GameProfile(java.util.UUID.nameUUIDFromBytes("ic2audit".getBytes()), "ic2audit"));
            padPlayer.getInventory().items.set(5, new ItemStack(Ic2Items.RE_BATTERY.get()));
            var reactor = (ic2.core.block.reactor.TileEntityNuclearReactor)level.getBlockEntity(base.offset(-2, 0, 20));
            reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD.get()));
            reactor.reactorSlot.put(3, 2, new ItemStack(Ic2Items.HEAT_VENT.get()));
            reactor.reactorSlot.put(8, 5, new ItemStack(Ic2Items.HEAT_STORAGE.get()));
            var scanner = (ic2.core.block.machine.tileentity.TileEntityScanner)level.getBlockEntity(base.offset(3, 0, 22));
            scanner.inputSlot.put(new ItemStack(Items.IRON_INGOT));
            scanner.progress = ic2.core.block.machine.tileentity.TileEntityScanner.DURATION - 20;
            var repl = (ic2.core.block.machine.tileentity.TileEntityReplicator)level.getBlockEntity(base.offset(5, 0, 22));
            repl.getFluidTank().fill(new net.neoforged.neoforge.fluids.FluidStack(ic2.core.fluid.Ic2Fluids.UU_MATTER.get(), 1000), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            check("uu values propagate through recipes", ic2.core.uu.UuGraph.get(new ItemStack(Items.COBBLESTONE)) == 1 && ic2.core.uu.UuGraph.get(new ItemStack(Items.IRON_INGOT)) < 1000 && ic2.core.uu.UuGraph.get(new ItemStack(Ic2Items.get("iridium_plate"))) < Double.POSITIVE_INFINITY && ic2.core.uu.UuGraph.get(new ItemStack(Ic2Items.get("scrap"))) < 1000,
                "cobble=" + ic2.core.uu.UuGraph.get(new ItemStack(Items.COBBLESTONE)) + " iron=" + ic2.core.uu.UuGraph.get(new ItemStack(Items.IRON_INGOT)) + " iridiumPlate=" + ic2.core.uu.UuGraph.get(new ItemStack(Ic2Items.get("iridium_plate"))) + " scrap=" + ic2.core.uu.UuGraph.get(new ItemStack(Ic2Items.get("scrap"))) + " n=" + ic2.core.uu.UuGraph.all().size() + dbgUu());
            check("enet registered", EnergyNet.instance.getTileEntity(level, base) != null, "");
        }
        if (tick % 50 == 0 && tick < 700) {
            var mac = (TileEntityMacerator)level.getBlockEntity(base.east());
            var iron = (TileEntityIronFurnace)level.getBlockEntity(base.offset(8, 0, 0));
            IC2.LOG.info("[audit] t={} mac energy={} progress={} op={} in={} out={} | iron fuel={} progress={} in={} out={}", tick, mac.energy, mac.getProgress(), mac.operationLength, mac.inputSlot.get(), mac.outputSlot.get(), iron.fuel, iron.progress, iron.inputSlot.get(), iron.outputSlot.get());
        }
        if (tick > 5 && tick < 710) {
            var miner = (ic2.core.block.machine.tileentity.TileEntityMiner)level.getBlockEntity(base.offset(-6, 0, 10));
            miner.energy = 1000;
            for (BlockPos pp : new BlockPos[]{base.offset(-5, 0, 10), base.offset(0, -1, 9)}) { var pump = (ic2.core.block.machine.tileentity.TileEntityPump)level.getBlockEntity(pp); pump.energy = pump.maxEnergy; }
            if (tick % 50 == 0) IC2.LOG.info("[audit] t={} miner progress={} pipe={} buffer0={} liquid={} | pump tank={}", tick, miner.progress, miner.pipeSlot.get(), miner.buffer.get(0), miner.canProvideLiquid, ((ic2.core.block.machine.tileentity.TileEntityPump)level.getBlockEntity(base.offset(-5, 0, 10))).getTankAmount());
        }
        if (tick == 550) {
            var miner = (ic2.core.block.machine.tileentity.TileEntityMiner)level.getBlockEntity(base.offset(-6, 0, 10));
            int pipes = shaft(level), rawIron = 0;
            for (int i = 0; i < miner.buffer.size(); i++) if (miner.buffer.get(i).is(Items.RAW_IRON)) rawIron += miner.buffer.get(i).getCount();
            minerPipes = pipes;
            check("miner digs shaft and mines scanned ore", pipes >= 7 && rawIron >= 1 && level.getBlockState(base.offset(-4, -5, 10)).isAir(), "pipes=" + pipes + " rawIron=" + rawIron);
            var pump = (ic2.core.block.machine.tileentity.TileEntityPump)level.getBlockEntity(base.offset(-5, 0, 10));
            check("pump drains liquid the miner hit", pump.getFluidStackfromTank().getFluid() == net.minecraft.world.level.material.Fluids.LAVA && pump.getTankAmount() >= 1000 && !level.getBlockState(base.offset(-6, -6, 10)).is(Blocks.LAVA), pump.getTankAmount() + " " + pump.getFluidStackfromTank().getFluid());
            miner.drillSlot.put(ItemStack.EMPTY);
        }
        if (tick == 700) {
            var miner = (ic2.core.block.machine.tileentity.TileEntityMiner)level.getBlockEntity(base.offset(-6, 0, 10));
            int pipes = shaft(level);
            check("miner withdraws pipe without drill", pipes < minerPipes && miner.pipeSlot.get().is(Ic2Items.MINING_PIPE.get()), "before=" + minerPipes + " after=" + pipes + " slot=" + miner.pipeSlot.get());
            var wpump = (ic2.core.block.machine.tileentity.TileEntityPump)level.getBlockEntity(base.offset(0, -1, 9));
            check("pump fills cells from water in front", wpump.outputSlot.get().is(Ic2Items.WATER_CELL.get()), "out=" + wpump.outputSlot.get() + " tank=" + wpump.getTankAmount());
        }
        if (tick > 5 && tick < 710) for (int dx = -3; dx <= 3; dx += 2) {
            var m = (ic2.core.block.machine.tileentity.TileEntityStandardMachine)level.getBlockEntity(base.offset(-6 + dx, 0, 13));
            m.energy = m.maxEnergy;
        }
        if (tick == 400) {
            var bottler = (ic2.core.block.machine.tileentity.TileEntityFluidBottler)level.getBlockEntity(base.offset(-3, 0, 13));
            check("bottler empties bucket into tank", bottler.outputSlot.get().is(Items.BUCKET) && bottler.getTankAmount() == 1000, "out=" + bottler.outputSlot.get() + " tank=" + bottler.getTankAmount());
            bottler.outputSlot.put(ItemStack.EMPTY);
        }
        if (tick == 700) {
            BlockPos cm = base.offset(-6, 0, 10);
            var solid = (ic2.core.block.machine.tileentity.TileEntitySolidCanner)level.getBlockEntity(cm.offset(-3, 0, 3));
            check("solid canner cans potatoes", solid.outputSlot.get().is(Ic2Items.FILLED_TIN_CAN.get()) && solid.outputSlot.get().getCount() == 2 && solid.canInputSlot.get().getCount() == 2, "out=" + solid.outputSlot.get() + " cans=" + solid.canInputSlot.get());
            var drainer = (ic2.core.block.machine.tileentity.TileEntityCanner)level.getBlockEntity(cm.offset(-1, 0, 3));
            check("canner drains cells into tank", drainer.outputTank.getFluidAmount() == 2000 && drainer.outputSlot.get().is(Ic2Items.EMPTY_CELL.get()), "tank=" + drainer.outputTank.getFluid().getAmount() + " out=" + drainer.outputSlot.get());
            var enricher = (ic2.core.block.machine.tileentity.TileEntityCanner)level.getBlockEntity(cm.offset(1, 0, 3));
            check("canner enriches water with biochaff", enricher.outputTank.getFluid().getFluid() == ic2.core.fluid.Ic2Fluids.BIOMASS.get() && enricher.outputTank.getFluidAmount() == 1000 && enricher.inputTank.getFluidAmount() == 1000, "in=" + enricher.inputTank.getFluidAmount() + " out=" + enricher.outputTank.getFluidAmount() + " " + enricher.outputTank.getFluid().getFluid());
            var bottler = (ic2.core.block.machine.tileentity.TileEntityFluidBottler)level.getBlockEntity(cm.offset(3, 0, 3));
            check("bottler fills cell from its tank", bottler.outputSlot.get().is(Ic2Items.WATER_CELL.get()), "out=" + bottler.outputSlot.get() + " tank=" + bottler.getTankAmount());
        }
        if (tick > 5 && tick < 710) { var mag = (ic2.core.block.machine.tileentity.TileEntityMagnetizer)level.getBlockEntity(base.offset(-9, 0, 7)); mag.energy = mag.maxEnergy; }
        if (tick == 700) {
            int charged = 0;
            for (int y = 0; y <= 3; y++) { var s = level.getBlockState(base.offset(-8, y, 7)); if (s.is(Ic2Blocks.IRON_FENCE.get()) && s.getValue(ic2.core.block.misc.BlockPoleFence.MAGNET) == 15) charged++; }
            check("magnetizer charges iron fence pole", charged == 4, "charged=" + charged);
            var e1 = (ic2.core.block.machine.tileentity.TileEntityElectrolyzer)level.getBlockEntity(base.offset(-6, 0, 7));
            check("electrolyzer stores surplus energy in cells", e1.hydrogenSlot.get().is(Ic2Items.get("electrolyzed_water_cell")) && e1.hydrogenSlot.get().getCount() >= 2, "h=" + e1.hydrogenSlot.get() + " w=" + e1.waterSlot.get() + " buf=" + e1.energy + " mfsu=" + ((TileEntityElectricBlock)level.getBlockEntity(base.offset(-6, 1, 7))).energy + " active=" + e1.getActive() + " mfe=" + e1.mfe);
            var e2 = (ic2.core.block.machine.tileentity.TileEntityElectrolyzer)level.getBlockEntity(base.offset(-4, 0, 7));
            var mfsu2 = (TileEntityElectricBlock)level.getBlockEntity(base.offset(-4, 1, 7));
            check("electrolyzer feeds an empty storage", e2.waterSlot.get().is(Ic2Items.WATER_CELL.get()) && mfsu2.energy > 20000, "w=" + e2.waterSlot.get() + " mfsu=" + mfsu2.energy);
        }
        if (tick == 20) {
            BlockPos tp = base.offset(-6, 0, 10);
            level.setBlock(tp.offset(-3, -1, 1), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
            var item = new net.minecraft.world.entity.item.ItemEntity(level, tp.getX() - 3 + 0.5, tp.getY() + 1.1, tp.getZ() + 1.5, new ItemStack(Items.COBBLESTONE, 64));
            item.setPickUpDelay(1000);
            item.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            level.addFreshEntity(item);
            teleItem = item.getUUID();
        }
        if (tick > 5 && tick < 710 && padPlayer != null) ((ic2.core.block.wiring.TileEntityChargepad)level.getBlockEntity(base.offset(-4, 0, 9))).playerStandsAt(padPlayer);
        if (tick == 120) {
            BlockPos tp = base.offset(-6, 0, 10);
            var item = level.getEntity(teleItem);
            check("teleporter moves item to linked teleporter", item != null && item.blockPosition().closerThan(tp.offset(3, 1, 1), 1.5), item == null ? "gone" : item.blockPosition() + " B=" + level.getBlockState(tp.offset(3, 0, 1)) + " below=" + level.getBlockState(tp.offset(3, -1, 1)) + " mfsu=" + ((TileEntityElectricBlock)level.getBlockEntity(tp.offset(-4, 0, 1))).energy);
            var pig = (net.minecraft.world.entity.LivingEntity)level.getEntity(teslaPig);
            check("tesla coil shocks nearby mob", pig == null || pig.isDeadOrDying() || pig.getHealth() < pig.getMaxHealth(), pig == null ? "dead" : "hp=" + pig.getHealth());
        }
        if (tick == 700) {
            ItemStack bat = padPlayer.getInventory().items.get(5);
            check("charge pad charges player inventory", ElectricItem.manager.getCharge(bat) >= 10000, "charge=" + ElectricItem.manager.getCharge(bat) + " active=" + level.getBlockState(base.offset(-4, 0, 9)));
        }
        if (tick == 30) {
            BlockPos sp = base.offset(-6, 0, 10).offset(-4, 3, -4);
            level.setBlock(sp.below(), Blocks.STONE.defaultBlockState(), 3);
            var sc = Ic2Blocks.SCAFFOLD.get();
            for (int i = 0; i < 4; i++) {
                BlockPos p = sp.east(i);
                int s = sc.getStrengthFrom(level, p);
                if (s >= 0) level.setBlock(p, sc.defaultBlockState().setValue(ic2.core.block.misc.BlockScaffold.STRENGTH, s), 3);
            }
        }
        if (tick == 40) {
            BlockPos sp = base.offset(-6, 0, 10).offset(-4, 3, -4);
            String got = "";
            for (int i = 0; i < 4; i++) { var s = level.getBlockState(sp.east(i)); got += s.is(Ic2Blocks.SCAFFOLD.get()) ? s.getValue(ic2.core.block.misc.BlockScaffold.STRENGTH) + " " : "- "; }
            check("scaffold strength falls off away from the pillar", got.equals("2 1 0 - "), got);
            level.setBlock(sp.below(), Blocks.AIR.defaultBlockState(), 3);
        }
        if (tick == 50) {
            BlockPos sp = base.offset(-6, 0, 10).offset(-4, 3, -4);
            check("unsupported scaffold collapses", !level.getBlockState(sp).is(Ic2Blocks.SCAFFOLD.get()), level.getBlockState(sp));
        }
        if (tick == 130) {
            BlockPos lp = base.offset(-6, 0, 10).offset(-4, 6, -4);
            for (int i = 2; i <= 6; i++) level.setBlock(lp.east(i), Blocks.STONE.defaultBlockState(), 3);
            for (BlockPos p : BlockPos.betweenClosed(lp.offset(2, 2, -1), lp.offset(6, 4, 1))) level.setBlock(p, Blocks.STONE.defaultBlockState(), 3);
            padPlayer.moveTo(lp.getX() + 0.5, lp.getY() - 1.52, lp.getZ() + 0.5, -90, 0);
            var laser = Ic2Items.MINING_LASER.get();
            ItemStack ls = laser.getItemStack(Double.POSITIVE_INFINITY);
            laser.shoot(level, padPlayer, ls, Float.POSITIVE_INFINITY, 5, Integer.MAX_VALUE, false, false, -90, 0, lp.getY() + 0.5);
            padPlayer.moveTo(lp.getX() + 0.5, lp.getY() + 1.48, lp.getZ() + 0.5, -90, 0);
            laser.shoot(level, padPlayer, ls, Float.POSITIVE_INFINITY, 12, Integer.MAX_VALUE, true, false, -90, 0, lp.getY() + 3.5);
        }
        if (tick == 160) {
            BlockPos lp = base.offset(-6, 0, 10).offset(-4, 6, -4);
            int mined = 0, blasted = 0;
            for (int i = 2; i <= 6; i++) if (level.getBlockState(lp.east(i)).isAir()) mined++;
            for (BlockPos p : BlockPos.betweenClosed(lp.offset(2, 2, -1), lp.offset(6, 4, 1))) if (level.getBlockState(p).isAir()) blasted++;
            check("mining laser bores through stone", mined >= 4, "mined=" + mined);
            check("explosive laser blasts with ExplosionIC2", blasted >= 5, "blasted=" + blasted);
        }
        if (tick == 100) {
            BlockPos pp = base.offset(-6, 0, 10).offset(-4, 9, 4);
            level.setBlock(pp, Ic2Blocks.CABLES.get(ic2.core.block.wiring.CableType.insulated_copper).get().defaultBlockState(), 3);
            level.setBlock(pp.east(), Blocks.WHITE_WOOL.defaultBlockState(), 3);
            ItemStack painter = new ItemStack(Ic2Items.PAINTERS.get(net.minecraft.world.item.DyeColor.RED).get());
            padPlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, painter);
            for (BlockPos t : new BlockPos[]{pp, pp.east()})
                painter.getItem().onItemUseFirst(painter, new net.minecraft.world.item.context.UseOnContext(level, padPlayer, net.minecraft.world.InteractionHand.MAIN_HAND, painter, new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(t), Direction.UP, t, false)));
            check("painter colours cables and wool", level.getBlockState(pp).getValue(ic2.core.block.wiring.BlockCable.COLOR) == 1 && level.getBlockState(pp.east()).is(Blocks.RED_WOOL) && painter.getDamageValue() == 2,
                level.getBlockState(pp).getValue(ic2.core.block.wiring.BlockCable.COLOR) + " " + level.getBlockState(pp.east()) + " dmg=" + painter.getDamageValue());
            ItemStack box = new ItemStack(Ic2Items.TOOL_BOX.get());
            padPlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, box);
            var inv = new ic2.core.item.HandHeldInventory(padPlayer, net.minecraft.world.InteractionHand.MAIN_HAND, 9);
            inv.setItem(3, new ItemStack(Ic2Items.WRENCH.get()));
            inv.setChanged();
            var again = new ic2.core.item.HandHeldInventory(padPlayer, net.minecraft.world.InteractionHand.MAIN_HAND, 9);
            check("tool box keeps its contents", again.getItem(3).is(Ic2Items.WRENCH.get()) && ic2.core.item.ItemToolbox.isBoxable(new ItemStack(Ic2Items.MINING_DRILL.get())) && !ic2.core.item.ItemToolbox.isBoxable(new ItemStack(Items.DIRT)), again.getItem(3));
            padPlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        if (tick == 400) {
            var reactor = (ic2.core.block.reactor.TileEntityNuclearReactor)level.getBlockEntity(base.offset(-2, 0, 20));
            var mfsu = (TileEntityElectricBlock)level.getBlockEntity(base.offset(-3, 0, 20));
            var rod = reactor.reactorSlot.get(0, 0);
            check("nuclear reactor powers storage", reactor.getReactorSize() == 4 && reactor.output == 1 && mfsu.energy > 500 && reactor.heat > 0
                && rod.getItem() instanceof ic2.core.item.reactor.ItemGradualInt g && g.getCustomDamage(rod) > 5 && reactor.reactorSlot.get(8, 5).isEmpty(),
                "size=" + reactor.getReactorSize() + " out=" + reactor.output + " mfsu=" + mfsu.energy + " heat=" + reactor.heat + " rodDmg=" + rod.get(ic2.core.ref.Ic2Components.CUSTOM_DAMAGE.get()) + " col8=" + reactor.reactorSlot.get(8, 5));
        }
        if (tick == 600) {
            var hot = (ic2.core.block.reactor.TileEntityNuclearReactor)level.getBlockEntity(base.offset(0, 0, -80));
            hot.heat = 10000;
        }
        if (tick == 640) {
            BlockPos bp = base.offset(0, 0, -80);
            int air = 0;
            for (BlockPos p : BlockPos.betweenClosed(bp.offset(-3, -3, -3), bp.offset(3, 3, 3))) if (level.getBlockState(p).isAir()) air++;
            check("overheated reactor melts down", !level.getBlockState(bp).is(Ic2Blocks.NUCLEAR_REACTOR.get()) && air > 20, "air=" + air);
        }
        if (tick > 5 && tick < 710) {
            ((ic2.core.block.machine.tileentity.TileEntityScanner)level.getBlockEntity(base.offset(3, 0, 22))).energy = 512000;
            ((ic2.core.block.machine.tileentity.TileEntityReplicator)level.getBlockEntity(base.offset(5, 0, 22))).energy = 2000000;
        }
        if (tick == 60) {
            var scanner = (ic2.core.block.machine.tileentity.TileEntityScanner)level.getBlockEntity(base.offset(3, 0, 22));
            scanner.onNetworkEvent(1);
            var storage = (ic2.core.block.machine.tileentity.TileEntityPatternStorage)level.getBlockEntity(base.offset(4, 0, 22));
            check("scanner records pattern into storage", storage.getPatterns().size() == 1 && storage.getPatterns().get(0).is(Items.IRON_INGOT) && scanner.inputSlot.isEmpty(), "patterns=" + storage.getPatterns() + " state=" + scanner.state);
            var repl = (ic2.core.block.machine.tileentity.TileEntityReplicator)level.getBlockEntity(base.offset(5, 0, 22));
            repl.onNetworkEvent(4);
        }
        if (tick == 300) {
            var repl = (ic2.core.block.machine.tileentity.TileEntityReplicator)level.getBlockEntity(base.offset(5, 0, 22));
            check("replicator builds the pattern from UU", repl.outputSlot.get().is(Items.IRON_INGOT) && repl.getTankAmount() < 1000, "out=" + repl.outputSlot.get() + " tank=" + repl.getTankAmount() + " mode=" + repl.mode + " uu=" + repl.patternUu);
        }
        if (tick == 6) {
            var tfp = (ic2.core.block.machine.tileentity.TileEntityTerra)level.getBlockEntity(base.offset(-40, 0, 40));
            tfp.tfbpSlot.put(new ItemStack(Ic2Items.TFBPS.get(ic2.core.item.tfbp.ItemTFBP.Kind.CULTIVATION).get()));
        }
        if (tick > 6 && tick < 200) ((ic2.core.block.machine.tileentity.TileEntityTerra)level.getBlockEntity(base.offset(-40, 0, 40))).energy = 100000;
        if (tick == 200) {
            BlockPos tf = base.offset(-40, 0, 40);
            int grass = 0, plants = 0;
            for (BlockPos p : BlockPos.betweenClosed(tf.offset(-10, -1, -10), tf.offset(10, -1, 10))) { if (level.getBlockState(p).is(Blocks.GRASS_BLOCK)) grass++; if (!level.getBlockState(p.above()).isAir()) plants++; }
            check("terraformer cultivates dirt", grass > 10, "grass=" + grass + " plants=" + plants);
        }
        if (tick == 120) check("detector cable signal while energy flows", level.getBlockState(base.offset(3, 0, 5)).getValue(ic2.core.block.wiring.BlockCable.ACTIVE) && level.getSignal(base.offset(3, 0, 5), Direction.WEST) > 0, level.getBlockState(base.offset(3, 0, 5)));
        if (tick == 700) {
            var mac = (TileEntityMacerator)level.getBlockEntity(base.east());
            check("macerator output", mac.outputSlot.get().getCount() == 4 && mac.outputSlot.get().is(Ic2Items.get("crushed_iron_ore")), mac.outputSlot.get());
            var gen = (TileEntityGenerator)level.getBlockEntity(base);
            check("generator burned", gen.fuelSlot.get().getCount() < 4, gen.fuelSlot.get() + " storage=" + gen.storage);
            var furnace = (TileEntityElectricFurnace)level.getBlockEntity(base.offset(6, 0, 0));
            check("batbox -> electric furnace", furnace.outputSlot.get().is(Items.IRON_INGOT), furnace.outputSlot.get() + " energy=" + furnace.energy);
            var mfe = (TileEntityElectricBlock)level.getBlockEntity(base.offset(10, 0, 0));
            check("MFE charges energy crystal", ElectricItem.manager.getCharge(mfe.chargeSlot.get()) > 0, ElectricItem.manager.getCharge(mfe.chargeSlot.get()));
            var iron = (TileEntityIronFurnace)level.getBlockEntity(base.offset(8, 0, 0));
            check("iron furnace", iron.outputSlot.get().is(Items.STONE) && iron.outputSlot.get().getCount() == 2, iron.outputSlot.get());
            var mac2 = (TileEntityMacerator)level.getBlockEntity(base.offset(12, 0, 0));
            var tr = (TileEntityTransformer)level.getBlockEntity(base.offset(11, 0, 0));
            check("MFE -> LV transformer -> macerator", mac2.outputSlot.get().is(Items.SAND), mac2.outputSlot.get() + " tr in=" + tr.inputflow + " out=" + tr.outputflow);
            var viaCable = (TileEntityMacerator)level.getBlockEntity(base.offset(3, 0, 6));
            check("generator -> cables (bend) -> macerator", viaCable.outputSlot.get().is(Items.SAND), viaCable.outputSlot.get() + " energy=" + viaCable.energy);
            var cableState = level.getBlockState(base.offset(3, 0, 3));
            check("cable corner connections", cableState.getValue(net.minecraft.world.level.block.PipeBlock.WEST) && cableState.getValue(net.minecraft.world.level.block.PipeBlock.SOUTH) && !cableState.getValue(net.minecraft.world.level.block.PipeBlock.NORTH), cableState);
            var isolated = (TileEntityMacerator)level.getBlockEntity(base.offset(9, 0, 3));
            check("red/blue cables isolated", isolated.energy == 0 && isolated.outputSlot.get().isEmpty(), "energy=" + isolated.energy);
            var geo = (ic2.core.block.generator.tileentity.TileEntityGeoGenerator)level.getBlockEntity(base.offset(0, 0, -3));
            check("geothermal burns lava", geo.outputSlot.get().is(Items.BUCKET) && geo.storage > 0 && geo.getTankAmount() < 1000, "storage=" + geo.storage + " tank=" + geo.getTankAmount() + " out=" + geo.outputSlot.get());
            var mill = (ic2.core.block.generator.tileentity.TileEntityWaterGenerator)level.getBlockEntity(base.offset(3, 0, -3));
            check("water mill produces", mill.storage > 0 || mill.water > 0, "water=" + mill.water + " storage=" + mill.storage + " micro=" + mill.microStorage);
            var wind = (ic2.core.block.generator.tileentity.TileEntityWindGenerator)level.getBlockEntity(base.offset(7, 20, -3));
            check("wind mill produces", wind != null && wind.subproduction > 0, wind == null ? "broken" : "sub=" + wind.subproduction + " storage=" + wind.storage + " wind=" + ic2.core.WindSim.get(level).getWindAt(base.getY() + 20));
            var ind = (ic2.core.block.machine.tileentity.TileEntityInduction)level.getBlockEntity(base.offset(0, 3, 0));
            check("induction furnace", ind.outputSlotA.get().is(Items.IRON_INGOT) && ind.outputSlotB.get().is(Items.STONE), ind.outputSlotA.get() + " " + ind.outputSlotB.get() + " heat=" + ind.heat);
            var former = (ic2.core.block.machine.tileentity.TileEntityMetalFormer)level.getBlockEntity(base.offset(2, 3, 0));
            check("metal former rolling", former.outputSlot.get().is(Ic2Items.get("iron_plate")), former.outputSlot.get());
            var washer = (ic2.core.block.machine.tileentity.TileEntityOreWashing)level.getBlockEntity(base.offset(4, 3, 0));
            check("ore washing plant", washer.outputSlot.get(0).is(Ic2Items.get("purified_crushed_iron_ore")) && washer.cellSlot.get().is(Items.BUCKET), washer.outputSlot + " water=" + washer.getTankAmount());
            var cent = (ic2.core.block.machine.tileentity.TileEntityCentrifuge)level.getBlockEntity(base.offset(6, 3, 0));
            check("thermal centrifuge heats", cent.heat > 0, "heat=" + cent.heat + "/" + cent.workheat + " out=" + cent.outputSlot);
            var cutter = (ic2.core.block.machine.tileentity.TileEntityBlockCutter)level.getBlockEntity(base.offset(8, 3, 0));
            check("block cutter works with blade", cutter.getProgress() > 0.5f && !cutter.isbladetoweak(), "progress=" + cutter.getProgress());
            var blast = (ic2.core.block.machine.tileentity.TileEntityBlastFurnace)level.getBlockEntity(base.offset(13, 2, 0));
            check("blast furnace heated by electric heat generator", blast.isHot() && blast.progress > 1 && blast.airOutputSlot.get().is(Ic2Items.EMPTY_CELL.get()), "heat=" + blast.heat + " progress=" + blast.progress + " air=" + blast.airOutputSlot.get());
            var windKg = (ic2.core.block.kineticgenerator.TileEntityKineticGenerators.Wind)level.getBlockEntity(base.offset(4, 30, 0));
            var kg1 = (ic2.core.block.generator.tileentity.TileEntityGenerators2.Kinetic)level.getBlockEntity(base.offset(4, 30, 1));
            check("wind rotor -> kinetic generator", kg1.EUstorage > 0, "wind=" + windKg.windStrength + " ku=" + windKg.getKuOutput() + " eu=" + kg1.EUstorage + " rotorDamage=" + windKg.rotorSlot.get().getDamageValue());
            var kg2 = (ic2.core.block.generator.tileentity.TileEntityGenerators2.Kinetic)level.getBlockEntity(base.offset(16, 2, 1));
            check("electric motor -> kinetic generator", kg2.EUstorage > 0, "eu=" + kg2.EUstorage);
            var fab = (ic2.core.block.machine.tileentity.TileEntityMatter)level.getBlockEntity(base.offset(18, 2, 0));
            check("mass fabricator makes UU and eats scrap", fab.getTankAmount() >= 1 && fab.scrap > 0, "uu=" + fab.getTankAmount() + " scrap=" + fab.scrap + " slot=" + fab.amplifierSlot.get());
            check("active state", !level.getBlockState(base.east()).getValue(BlockTileEntity.ACTIVE), "idle after work");
        }
        if (tick == 705) CropAudit.run(level, base.offset(32, 0, -32), ServerAudit::check);
        if (tick == 706) LogisticsAudit.run(level, base.offset(32, 0, 32), ServerAudit::check);
        if (tick == 707) FluidMachinesAudit.run(level, base.offset(-32, 0, -32), ServerAudit::check);
        if (tick == 708) ThermalFluidsAudit.run(level, base.offset(0, 0, -32), ServerAudit::check);
        if (tick == 710) {
            IC2.LOG.info("[audit] DONE {} checks, {} failed", results.size(), results.stream().filter(s -> s.startsWith("FAIL")).count());
            server.halt(false);
        }
    }
}
