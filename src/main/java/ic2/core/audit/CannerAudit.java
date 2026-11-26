package ic2.core.audit;

import ic2.core.IC2;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.fluid.Ic2Fluids;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

/** Standalone native regression scenarios: -Dic2.audit=canner. */
@EventBusSubscriber(modid = IC2.MODID)
public final class CannerAudit {
    private static int failures, checks;
    private static void check(String name, boolean ok, Object detail) {
        checks++;
        if (!ok) failures++;
        IC2.LOG.info("[canner-audit] {} {} {}", ok ? "PASS" : "FAIL", name, detail);
    }

    private static TileEntityCanner machine(ServerLevel level, BlockPos pos, TileEntityCanner.Mode mode) {
        level.removeBlock(pos, false);
        level.setBlock(pos, Ic2Blocks.CANNER.get().defaultBlockState(), 3);
        var machine = (TileEntityCanner)level.getBlockEntity(pos);
        machine.onLoaded();
        machine.setMode(mode);
        return machine;
    }

    private static void runMachine(TileEntityCanner machine) {
        for (int i = 0; i < 205; i++) { machine.energy = machine.maxEnergy; machine.tick(); }
    }

    private static boolean fluid(ItemStack stack, net.minecraft.world.level.material.Fluid type, int amount) {
        var fluid = InvSlotConsumableLiquid.contained(stack);
        return fluid.is(type) && fluid.getAmount() == amount;
    }

    @SubscribeEvent
    static void started(ServerStartedEvent event) {
        if (!"canner".equals(System.getProperty("ic2.audit"))) return;
        try { run(event.getServer().overworld()); }
        catch (Throwable error) { failures++; IC2.LOG.error("[canner-audit] FAIL exception", error); }
        IC2.LOG.info("[canner-audit] COMPLETE checks={} failures={}", checks, failures);
        event.getServer().halt(false);
    }

    private static void run(ServerLevel level) {
        var pos = new BlockPos(0, 150, 0);
        for (boolean distilled : new boolean[]{false, true}) {
            var type = distilled ? Ic2Fluids.DISTILLED_WATER.get() : Fluids.WATER;
            int dust = distilled ? 1 : 8;
            for (boolean universal : new boolean[]{false, true}) {
                var m = machine(level, pos, TileEntityCanner.Mode.EnrichLiquid);
                ItemStack cell;
                if (universal) {
                    cell = new ItemStack(Ic2Items.UNIVERSAL_FLUID_CELL.get());
                    ic2.core.item.resources.ItemFluidCell.handler(cell).fill(new FluidStack(type, 1000), FluidAction.EXECUTE);
                } else cell = new ItemStack(distilled ? Ic2Items.FLUID_CELLS.get("distilled_water").get() : Ic2Items.WATER_CELL.get());
                m.canInputSlot.put(cell);
                m.inputSlot.put(new ItemStack(Ic2Items.get("lapis_dust"), dust + 1));
                runMachine(m);
                String label = (distilled ? "distilled" : "water") + (universal ? " universal" : " fixed");
                check(label + " capsule produces 1000mB coolant", fluid(m.outputSlot.get(), Ic2Fluids.COOLANT.get(), 1000), m.outputSlot.get());
                check(label + " consumes exact water and lapis", m.inputTank.isEmpty() && m.outputTank.isEmpty() && m.inputSlot.get().getCount() == 1 && m.canInputSlot.isEmpty(), m.inputSlot.get());
            }
        }
        var m = machine(level, pos, TileEntityCanner.Mode.EnrichLiquid);
        m.inputTank.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE);
        m.inputSlot.put(new ItemStack(Ic2Items.get("lapis_dust"), 8));
        runMachine(m);
        check("pipe supplied water still produces coolant", m.inputTank.isEmpty() && m.outputTank.getFluid().is(Ic2Fluids.COOLANT.get()) && m.outputTank.getFluidAmount() == 1000, m.outputTank.getFluid());
        m = machine(level, pos, TileEntityCanner.Mode.EnrichLiquid);
        m.canInputSlot.put(new ItemStack(Ic2Items.WATER_CELL.get()));
        m.inputSlot.put(new ItemStack(Ic2Items.get("lapis_dust"), 7));
        runMachine(m);
        check("insufficient lapis preserves all water and dust", m.inputTank.getFluidAmount() + InvSlotConsumableLiquid.contained(m.canInputSlot.get()).getAmount() == 1000 && m.inputSlot.get().getCount() == 7 && m.outputTank.isEmpty(), m.inputTank.getFluid());
        m = machine(level, pos, TileEntityCanner.Mode.EnrichLiquid);
        m.inputTank.fill(new FluidStack(Fluids.WATER, 7999), FluidAction.EXECUTE);
        m.canInputSlot.put(new ItemStack(Ic2Items.WATER_CELL.get()));
        m.tick();
        check("atomic cell is retained with only 1mB tank space", m.inputTank.getFluidAmount() == 7999 && m.canInputSlot.get().is(Ic2Items.WATER_CELL.get()), m.inputTank.getFluid());
        m = machine(level, pos, TileEntityCanner.Mode.EnrichLiquid);
        m.outputTank.fill(new FluidStack(Ic2Fluids.COOLANT.get(), 8000), FluidAction.EXECUTE);
        m.canInputSlot.put(new ItemStack(Ic2Items.WATER_CELL.get()));
        m.inputSlot.put(new ItemStack(Ic2Items.get("lapis_dust"), 8));
        runMachine(m);
        check("full output preserves recipe inputs", m.inputSlot.get().getCount() == 8 && m.inputTank.getFluidAmount() + InvSlotConsumableLiquid.contained(m.canInputSlot.get()).getAmount() == 1000 && m.outputTank.getFluidAmount() == 8000, m.outputTank.getFluid());
        m = machine(level, pos, TileEntityCanner.Mode.BottleLiquid);
        m.inputTank.fill(new FluidStack(Ic2Fluids.DISTILLED_WATER.get(), 1000), FluidAction.EXECUTE);
        m.canInputSlot.put(new ItemStack(Ic2Items.UNIVERSAL_FLUID_CELL.get()));
        runMachine(m);
        check("bottle mode fills universal cell", fluid(m.outputSlot.get(), Ic2Fluids.DISTILLED_WATER.get(), 1000) && m.inputTank.isEmpty(), m.outputSlot.get());
        m = machine(level, pos, TileEntityCanner.Mode.EmptyLiquid);
        m.canInputSlot.put(new ItemStack(Ic2Items.WATER_CELL.get(), 2));
        runMachine(m);
        check("empty mode drains exactly one stacked cell per cycle", m.canInputSlot.get().getCount() == 1 && m.outputSlot.get().is(Ic2Items.EMPTY_CELL.get()) && m.outputTank.getFluidAmount() == 1000, m.outputTank.getFluid());
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(level);
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.getInventory().clearContent();
        var source = pos.east(4);
        level.setBlock(source, Blocks.WATER.defaultBlockState(), 3);
        player.moveTo(source.getX() + .5, source.getY() + 1, source.getZ() + .5, 0, 90);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.UNIVERSAL_FLUID_CELL.get()));
        var result = Ic2Items.UNIVERSAL_FLUID_CELL.get().use(level, player, InteractionHand.MAIN_HAND);
        player.setItemInHand(InteractionHand.MAIN_HAND, result.getObject());
        check("right click collects a real water source", fluid(player.getMainHandItem(), Fluids.WATER, 1000) && level.getFluidState(source).isEmpty(), result.getResult());
        m = machine(level, pos, TileEntityCanner.Mode.BottleSolid);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        var ctx = new UseOnContext(player, InteractionHand.MAIN_HAND, hit);
        var interaction = Ic2Items.UNIVERSAL_FLUID_CELL.get().onItemUseFirst(player.getMainHandItem(), ctx);
        check("right click transfers water into machine before GUI", interaction.consumesAction() && m.inputTank.getFluidAmount() == 1000 && InvSlotConsumableLiquid.contained(player.getMainHandItem()).isEmpty(), interaction);
        m.outputTank.fill(new FluidStack(Ic2Fluids.DISTILLED_WATER.get(), 1000), FluidAction.EXECUTE);
        interaction = Ic2Items.UNIVERSAL_FLUID_CELL.get().onItemUseFirst(player.getMainHandItem(), ctx);
        check("right click collects distilled water from machine", interaction.consumesAction() && fluid(player.getMainHandItem(), Ic2Fluids.DISTILLED_WATER.get(), 1000) && m.outputTank.isEmpty(), interaction);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.UNIVERSAL_FLUID_CELL.get()));
        ic2.core.item.resources.ItemFluidCell.handler(player.getMainHandItem()).fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE);
        interaction = player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
        check("normal block interaction transfers fluid before opening machine", interaction.consumesAction() && m.inputTank.getFluidAmount() == 2000 && InvSlotConsumableLiquid.contained(player.getMainHandItem()).isEmpty(), interaction);
        var saved = m.saveWithFullMetadata(level.registryAccess());
        var copy = (TileEntityCanner)net.minecraft.world.level.block.entity.BlockEntity.loadStatic(pos, m.getBlockState(), saved, level.registryAccess());
        check("tank fluid survives save and load", copy.inputTank.getFluidAmount() == 2000 && copy.inputTank.getFluid().is(Fluids.WATER), copy.inputTank.getFluid());
        FluidMachinesAudit.run(level, pos.offset(16, 0, 0), CannerAudit::check);
        ThermalFluidsAudit.run(level, pos.offset(32, 0, 0), CannerAudit::check);
        level.removeBlock(pos, false);
        level.removeBlock(source, false);
    }
    private CannerAudit() {}
}
