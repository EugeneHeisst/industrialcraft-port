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
            check("enet registered", EnergyNet.instance.getTileEntity(level, base) != null, "");
        }
        if (tick % 50 == 0 && tick < 700) {
            var mac = (TileEntityMacerator)level.getBlockEntity(base.east());
            var iron = (TileEntityIronFurnace)level.getBlockEntity(base.offset(8, 0, 0));
            IC2.LOG.info("[audit] t={} mac energy={} progress={} op={} in={} out={} | iron fuel={} progress={} in={} out={}", tick, mac.energy, mac.getProgress(), mac.operationLength, mac.inputSlot.get(), mac.outputSlot.get(), iron.fuel, iron.progress, iron.inputSlot.get(), iron.outputSlot.get());
        }
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
            check("active state", !level.getBlockState(base.east()).getValue(BlockTileEntity.ACTIVE), "idle after work");
        }
        if (tick == 710) {
            IC2.LOG.info("[audit] DONE {} checks, {} failed", results.size(), results.stream().filter(s -> s.startsWith("FAIL")).count());
            server.halt(false);
        }
    }
}
