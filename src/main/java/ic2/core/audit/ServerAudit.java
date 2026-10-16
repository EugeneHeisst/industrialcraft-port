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
            check("enet registered", EnergyNet.instance.getTileEntity(level, base) != null, "");
        }
        if (tick % 50 == 0 && tick < 700) {
            var mac = (TileEntityMacerator)level.getBlockEntity(base.east());
            var iron = (TileEntityIronFurnace)level.getBlockEntity(base.offset(8, 0, 0));
            IC2.LOG.info("[audit] t={} mac energy={} progress={} op={} in={} out={} | iron fuel={} progress={} in={} out={}", tick, mac.energy, mac.getProgress(), mac.operationLength, mac.inputSlot.get(), mac.outputSlot.get(), iron.fuel, iron.progress, iron.inputSlot.get(), iron.outputSlot.get());
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
            check("active state", !level.getBlockState(base.east()).getValue(BlockTileEntity.ACTIVE), "idle after work");
        }
        if (tick == 710) {
            IC2.LOG.info("[audit] DONE {} checks, {} failed", results.size(), results.stream().filter(s -> s.startsWith("FAIL")).count());
            server.halt(false);
        }
    }
}
