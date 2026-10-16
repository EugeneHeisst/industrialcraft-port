package ic2.core.audit;

import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.BlockTileEntity;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** -Dic2.audit=client: places every block in front of the player, takes screenshots of the world, inventory and each GUI, then quits. */
@EventBusSubscriber(modid = IC2.MODID, value = Dist.CLIENT)
public final class ClientAudit {
    private static final boolean ENABLED = "client".equals(System.getProperty("ic2.audit"));
    private static int tick = -1;
    private static BlockPos origin;
    private static final List<BlockPos> guis = new ArrayList<>();
    private ClientAudit() {}

    private static void shot(Minecraft mc, String name) {
        try {
            Path out = Path.of("audit");
            Files.createDirectories(out);
            try (var im = Screenshot.takeScreenshot(mc.getMainRenderTarget())) { im.writeToFile(out.resolve(name)); }
        } catch (java.io.IOException e) { throw new java.io.UncheckedIOException(e); }
    }

    @SubscribeEvent
    static void clientTick(ClientTickEvent.Post event) {
        if (!ENABLED) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        tick++;
        if (tick % 100 == 1) IC2.LOG.info("[client-audit] tick {} screen {}", tick, mc.screen);
        if (mc.screen instanceof net.minecraft.client.gui.screens.DeathScreen) mc.player.respawn();
        var server = mc.getSingleplayerServer();
        if (tick == 40) server.execute(() -> {
            ServerLevel level = server.overworld();
            ServerPlayer player = server.getPlayerList().getPlayers().get(0);
            level.setDayTime(6000);
            level.setWeatherParameters(6000, 0, false, false);
            origin = new BlockPos(player.getBlockX(), 120, player.getBlockZ() + 4);
            for (BlockPos p : BlockPos.betweenClosed(origin.offset(-16, -1, -16), origin.offset(64, -1, 16))) level.setBlock(p, Blocks.STONE.defaultBlockState(), 3);
            for (BlockPos p : BlockPos.betweenClosed(origin.offset(-12, 0, -2), origin.offset(12, 24, 3))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            server.setDifficulty(net.minecraft.world.Difficulty.PEACEFUL, true);
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            player.getAbilities().invulnerable = true;
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.setHealth(player.getMaxHealth());
            int i = 0;
            for (var b : Ic2Blocks.REGISTRY.getEntries()) {
                if (b.get() instanceof net.minecraft.world.level.block.LiquidBlock || b.get() instanceof ic2.core.block.wiring.BlockLuminator) continue;
                BlockPos p = origin.offset(-9 + i % 19, i / 19 * 2, 0);
                var state = b.get().defaultBlockState();
                if (state.hasProperty(BlockTileEntity.FACING)) state = state.setValue(BlockTileEntity.FACING, Direction.NORTH).setValue(BlockTileEntity.ACTIVE, i % 2 == 0);
                level.setBlock(p, state, 3);
                if (level.getBlockEntity(p) instanceof IHasGui) guis.add(p);
                if (level.getBlockEntity(p) instanceof ic2.core.block.kineticgenerator.TileEntityKineticGenerators.Rotor rotorGen) { rotorGen.rotorSlot.put(new ItemStack(Ic2Items.CARBON_ROTOR.get())); rotorGen.setRotationSpeed(1); }
                if (level.getBlockEntity(p) instanceof ic2.core.block.machine.tileentity.TileEntityNuke n) { n.outsideSlot.put(new ItemStack(Ic2Blocks.INDUSTRIAL_TNT.get(),32));n.insideSlot.put(new ItemStack(Ic2Items.get("uranium_235"),4)); }
                if (level.getBlockEntity(p) instanceof ic2.core.block.machine.tileentity.TileEntityAdvMiner m) { m.scannerSlot.put(Ic2Items.OV_SCANNER.get().charged());m.ListSlot.put(new ItemStack(net.minecraft.world.item.Items.COBBLESTONE));m.energy=3000000;m.minelayer=119; }
                if (level.getBlockEntity(p) instanceof ic2.core.block.machine.tileentity.TileEntityLathe lathe) { lathe.latheSlot.put(ic2.core.item.resources.ItemLatheDefault.getStackForState(new ItemStack(Ic2Items.IRON_TURNING_BLANK.get()),0x32323));lathe.toolSlot.put(new ItemStack(Ic2Items.LATHING_TOOL.get()));lathe.kUBuffer=5000; }
                if (level.getBlockEntity(p) instanceof ic2.core.block.kineticgenerator.TileEntityStirlingKineticGenerator s) { s.inputTank.fill(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,1500),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);s.outputTank.fill(new net.neoforged.neoforge.fluids.FluidStack(ic2.core.fluid.Ic2Fluids.HOT_WATER.get(),500),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);s.kUBuffer=1200; }
                if (level.getBlockEntity(p) instanceof ic2.core.block.machine.tileentity.TileEntitySteamGenerator boiler) { boiler.waterTank.fill(new net.neoforged.neoforge.fluids.FluidStack(ic2.core.fluid.Ic2Fluids.DISTILLED_WATER.get(),7500),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);boiler.systemheat=100;boiler.pressurevalve=220; }
                if (level.getBlockEntity(p) instanceof ic2.core.block.kineticgenerator.TileEntitySteamKineticGenerator t) { t.turbineSlot.put(new ItemStack(Ic2Items.STEAM_TURBINE.get()));t.waterTank.fill(new net.neoforged.neoforge.fluids.FluidStack(ic2.core.fluid.Ic2Fluids.DISTILLED_WATER.get(),250),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE); }
                if (level.getBlockEntity(p) instanceof ic2.core.block.heatgenerator.TileEntityFluidHeatGenerator h) { h.tank.fill(new net.neoforged.neoforge.fluids.FluidStack(ic2.core.fluid.Ic2Fluids.BIOGAS.get(),7500),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE); }
                if (level.getBlockEntity(p) instanceof ic2.core.block.machine.tileentity.TileEntityLiquidHeatExchanger h) { for(int n=0;n<10;n++)h.conductors.put(n,new ItemStack(Ic2Items.get("heat_conductor")));h.inputTank.fill(new net.neoforged.neoforge.fluids.FluidStack(ic2.core.fluid.Ic2Fluids.HOT_COOLANT.get(),1500),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);h.outputTank.fill(new net.neoforged.neoforge.fluids.FluidStack(ic2.core.fluid.Ic2Fluids.COOLANT.get(),1000),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE); }
                if (level.getBlockEntity(p) instanceof ic2.core.block.machine.tileentity.TileEntityFermenter f) { f.inputTank.fill(new net.neoforged.neoforge.fluids.FluidStack(ic2.core.fluid.Ic2Fluids.BIOMASS.get(),5000),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);f.outputTank.fill(new net.neoforged.neoforge.fluids.FluidStack(ic2.core.fluid.Ic2Fluids.BIOGAS.get(),500),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);f.heatbuffer=4000;f.progress=250; }
                if (level.getBlockEntity(p) instanceof ic2.core.block.machine.tileentity.TileEntityFluidDistributor d) { d.tank.fill(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,750),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);d.setActive(true); }
                if (level.getBlockEntity(p) instanceof ic2.core.block.machine.tileentity.TileEntityFluidRegulator r) { r.tank.fill(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,7500),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);r.outputmb=250;r.energy=5000; }
                if (level.getBlockEntity(p) instanceof ic2.core.block.machine.tileentity.TileEntityCondenser c) { c.inputTank.fill(new net.neoforged.neoforge.fluids.FluidStack(ic2.core.fluid.Ic2Fluids.STEAM.get(),90000),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);c.outputTank.fill(new net.neoforged.neoforge.fluids.FluidStack(ic2.core.fluid.Ic2Fluids.DISTILLED_WATER.get(),500),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE); }
                if (level.getBlockEntity(p) instanceof ic2.core.block.machine.tileentity.TileEntitySolarDistiller d) { d.inputTank.fill(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,7500),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);d.outputTank.fill(new net.neoforged.neoforge.fluids.FluidStack(ic2.core.fluid.Ic2Fluids.DISTILLED_WATER.get(),5000),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE); }
                if (level.getBlockEntity(p) instanceof ic2.core.block.personal.TileEntityTradeOMat t) { t.demandSlot.put(new ItemStack(net.minecraft.world.item.Items.EMERALD, 2)); t.offerSlot.put(new ItemStack(net.minecraft.world.item.Items.APPLE, 3)); t.totalTradeCount = 7; }
                if (level.getBlockEntity(p) instanceof ic2.core.block.personal.TileEntityEnergyOMat t) t.demandSlot.put(new ItemStack(net.minecraft.world.item.Items.REDSTONE));
                i++;
            }
            // customer views: a trade-o-mat and an energy-o-mat owned by somebody else
            var trader = new com.mojang.authlib.GameProfile(java.util.UUID.nameUUIDFromBytes("ic2trader".getBytes()), "Trader");
            for (int k = 0; k < 2; k++) {
                BlockPos p = origin.offset(-4 + k * 4, 0, 3);
                level.setBlock(p, (k == 0 ? Ic2Blocks.TRADE_O_MAT : Ic2Blocks.ENERGY_O_MAT).get().defaultBlockState(), 3);
                if (level.getBlockEntity(p) instanceof ic2.core.block.personal.IPersonalBlock pb) pb.permitsAccess(trader);
                if (level.getBlockEntity(p) instanceof ic2.core.block.personal.TileEntityTradeOMat t) { t.demandSlot.put(new ItemStack(net.minecraft.world.item.Items.EMERALD, 2)); t.offerSlot.put(new ItemStack(net.minecraft.world.item.Items.APPLE, 3)); }
                if (level.getBlockEntity(p) instanceof ic2.core.block.personal.TileEntityEnergyOMat t) { t.demandSlot.put(new ItemStack(net.minecraft.world.item.Items.REDSTONE)); t.paidFor = 2500; }
                guis.add(p);
            }
            int ci = 0;
            for (var type : ic2.core.block.wiring.CableType.values()) {
                BlockPos cp = origin.offset(-10 + ci * 2, 0, -4);
                var cs = Ic2Blocks.CABLES.get(type).get().defaultBlockState();
                if (type.colorable && ci % 2 == 0) cs = cs.setValue(ic2.core.block.wiring.BlockCable.COLOR, 1 + ci);
                for (int k = 0; k < 3; k++) level.setBlock(cp.offset(0, 0, k), cs, 3);
                level.setBlock(cp.offset(0, 1, 0), cs, 3);
                ci++;
            }
            // foamed cables in front of the camera: soft foam, hardened light gray wall, hardened blue wall
            for (int f = 0; f < 3; f++) {
                BlockPos fp = origin.offset(-2 + f * 2, 0, -7);
                level.setBlock(fp, Ic2Blocks.CABLES.get(ic2.core.block.wiring.CableType.insulated_copper).get().defaultBlockState(), 3);
                if (level.getBlockEntity(fp) instanceof ic2.core.block.wiring.TileEntityCable fc) { fc.changeFoam(f == 0 ? 1 : 2); if (f == 2) fc.changeColor(4); }
            }
            // dynamite between the foamed cables: standing, remote-linked, wall-hung, and a hovering thrown stick
            level.setBlock(origin.offset(-1, 0, -7), Ic2Blocks.DYNAMITE.get().defaultBlockState(), 3);
            level.setBlock(origin.offset(1, 0, -7), Ic2Blocks.DYNAMITE_REMOTE.get().defaultBlockState(), 3);
            level.setBlock(origin.offset(3, 0, -6), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
            level.setBlock(origin.offset(3, 0, -7), Ic2Blocks.DYNAMITE.get().defaultBlockState().setValue(ic2.core.block.explosive.BlockDynamite.FACING, net.minecraft.core.Direction.NORTH), 3);
            var thrown = new ic2.core.block.explosive.EntityDynamite(level, origin.getX() + 0.5, origin.getY() + 1.6, origin.getZ() - 6.5, false);
            thrown.fuse = 1000000; thrown.setNoGravity(true); thrown.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            level.addFreshEntity(thrown);
            player.teleportTo(level, origin.getX() + .5, origin.getY() + 1, origin.getZ() - 11.5, 0, 10);
            player.getInventory().clearContent();
            int slot = 0;
            for (var item : Ic2Items.REGISTRY.getEntries()) if (slot < 36) player.getInventory().setItem(slot++, new ItemStack(item.get()));
        });
        if (tick == 125 - 1) shot(mc, "blocks.png");
        if (tick == 100) server.execute(() -> { var pl = server.getPlayerList().getPlayers().get(0); pl.teleportTo(server.overworld(), origin.getX() + .5, origin.getY() + 1.5, origin.getZ() - 8.5, 0, 25); });
        if (tick == 115) shot(mc, "cables.png");
        if (tick == 116) server.execute(() -> { var pl = server.getPlayerList().getPlayers().get(0); pl.teleportTo(server.overworld(), origin.getX() + .5, origin.getY() + 1, origin.getZ() - 11.5, 0, 10); });
        if (tick == 125) mc.setScreen(new InventoryScreen(mc.player));
        if (tick == 135) shot(mc, "inventory.png");
        if (tick == 140) mc.setScreen(null);
        int g = (tick - 150) / 20;
        if (tick >= 150 && g < guis.size()) {
            BlockPos p = guis.get(g);
            if ((tick - 150) % 20 == 0) server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayers().get(0);
                player.teleportTo(server.overworld(), p.getX() + .5, p.getY(), p.getZ() - 2.5, 0, 30);
                var be = server.overworld().getBlockEntity(p);
                if (be instanceof IHasGui gui) player.openMenu(new SimpleMenuProvider((id, inv, pl) -> gui.getGuiContainer(id, inv), Component.empty()), buf -> { buf.writeBlockPos(p); gui.writeGuiData(player, buf); });
            });
            if ((tick - 150) % 20 == 15) shot(mc, "gui_" + net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(server.overworld().getBlockState(p).getBlock()).getPath()
                + (mc.player.containerMenu instanceof ic2.core.block.personal.ContainerTradeOMat c && !c.open || mc.player.containerMenu instanceof ic2.core.block.personal.ContainerEnergyOMat e && !e.open ? "_customer" : "") + ".png");
            if ((tick - 150) % 20 == 18) mc.player.closeContainer();
        }
        int end = 150 + guis.size() * 20;
        if (tick == end + 2) mc.setScreen(new ic2.core.client.GuiToolScanner(new ic2.core.item.tool.ItemScanner.ScanResult(
            List.of(new ItemStack(net.minecraft.world.item.Items.RAW_IRON), new ItemStack(net.minecraft.world.item.Items.COAL), new ItemStack(Ic2Blocks.URANIUM_ORE.get())), List.of(14, 9, 2)),
            Component.translatable("ic2.itemScanner")));
        if (tick == end + 10) shot(mc, "gui_od_scanner.png");
        if (tick == end + 12) mc.setScreen(null);
        if (tick == end + 14) server.execute(() -> {
            var l=server.overworld();var pl=server.getPlayerList().getPlayers().get(0);var field=origin.offset(40,0,0);
            for(var pos:BlockPos.betweenClosed(field.offset(-2,-1,-8),field.offset(15,4,8)))l.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
            int index=0;
            for(var card:ic2.api.crops.Crops.instance.getCrops()) {
                var pos=field.offset(index%10,0,index/10*2);
                l.setBlock(pos.below(),Blocks.FARMLAND.defaultBlockState().setValue(net.minecraft.world.level.block.FarmBlock.MOISTURE,7),3);
                l.setBlock(pos,Ic2Blocks.CROP.get().defaultBlockState(),3);
                var c=(ic2.core.crop.TileEntityCrop)l.getBlockEntity(pos);c.setCrop(card);c.setSize((byte)card.maxSize());c.setHydrationStorage(200);c.setNutrientStorage(100);c.updateState();index++;
            }
            pl.getAbilities().mayfly=true;pl.getAbilities().flying=true;pl.onUpdateAbilities();
            pl.teleportTo(l,field.getX()+4.5,field.getY()+3,field.getZ()-5.5,0,28);
        });
        if (tick == end + 60) shot(mc, "crops.png");
        if (tick == end + 65) server.execute(() -> {
            var pl=server.getPlayerList().getPlayers().get(0);
            var tool=Ic2Items.CROP_ANALYZER.get().charged();pl.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,tool);
            var seed=ic2.core.item.ItemCropSeed.create(ic2.core.crop.IC2Crops.cropWheat,12,17,9,4);
            var stored=net.minecraft.core.NonNullList.withSize(3,ItemStack.EMPTY);stored.set(1,seed);
            tool.set(net.minecraft.core.component.DataComponents.CONTAINER,net.minecraft.world.item.component.ItemContainerContents.fromItems(stored));
            Ic2Items.CROP_ANALYZER.get().use(pl.level(),pl,net.minecraft.world.InteractionHand.MAIN_HAND);
        });
        if (tick == end + 85) shot(mc,"gui_crop_analyzer.png");
        if (tick == end + 90) mc.player.closeContainer();
        if (tick == end + 95) server.execute(() -> {
            var l=server.overworld();var p=origin.offset(40,0,-15);var pl=server.getPlayerList().getPlayers().get(0);
            for(var pos:BlockPos.betweenClosed(p.offset(-3,-1,-3),p.offset(3,-1,3)))l.setBlock(pos,Blocks.STONE.defaultBlockState(),3);
            for(int n=0;n<2;n++){var e=new ic2.core.block.explosive.EntityIC2Explosive(ic2.core.ref.Ic2Entities.IC2_EXPLOSIVE.get(),l);e.setPos(p.getX()+n*2,p.getY(),p.getZ());e.setBlockState((n==0?Ic2Blocks.INDUSTRIAL_TNT.get():Ic2Blocks.NUKE.get()).defaultBlockState());e.setFuse(20000);l.addFreshEntity(e);}
            pl.teleportTo(l,p.getX()+1,p.getY()+1,p.getZ()-4,0,12);
        });
        if (tick == end + 130) shot(mc,"primed_explosives.png");
        if (tick == end + 135) server.execute(() -> {
            var l=server.overworld();var p=origin.offset(48,1,-15);var pl=server.getPlayerList().getPlayers().get(0);
            for(int n=0;n<3;n++){
                var q=p.east(n*2);l.setBlock(q.south(),Ic2Blocks.CABLES.get(ic2.core.block.wiring.CableType.glass_fibre).get().defaultBlockState(),3);
                l.setBlock(q.south(2),Ic2Blocks.BATBOX.get().defaultBlockState().setValue(BlockTileEntity.FACING,Direction.NORTH),3);((ic2.core.block.wiring.TileEntityElectricBlock)l.getBlockEntity(q.south(2))).energy=10000;
                l.setBlock(q,Ic2Blocks.LUMINATOR.get().defaultBlockState().setValue(ic2.core.block.wiring.BlockLuminator.FULL_COVER,true),3);
            }
            pl.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,Ic2Items.FUEL_JETPACK.get().filled());pl.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,Ic2Items.FUEL_JETPACK.get().filled());pl.teleportTo(l,p.getX()+2,p.getY(),p.getZ()-5,0,0);
        });
        if (tick == end + 170) shot(mc,"luminators.png");
        if (tick == end + 175) mc.setScreen(new InventoryScreen(mc.player));
        if (tick == end + 185) shot(mc,"fuel_jetpack.png");
        if (tick == end + 190) server.execute(() -> {
            var pl=server.getPlayerList().getPlayers().get(0);pl.getInventory().clearContent();int i=0;
            for(var item:new ic2.core.item.ItemBatteryChargeHotbar[]{Ic2Items.CHARGING_RE_BATTERY.get(),Ic2Items.CHARGING_ADVANCED_BATTERY.get(),Ic2Items.CHARGING_ENERGY_CRYSTAL.get(),Ic2Items.CHARGING_LAPOTRON_CRYSTAL.get()}){var filled=item.charged();item.setMode(filled,ic2.core.item.ItemBatteryChargeHotbar.Mode.DISABLED);pl.getInventory().setItem(i++,filled);pl.getInventory().setItem(i++,new ItemStack(item));}
        });
        if (tick == end + 205) shot(mc,"charging_batteries.png");
        if (tick == end + 210) server.execute(() -> {
            // personal stand: closed safe, safe in use (door swings open), trade-o-mat, energy-o-mat; safe item in hand and hotbar
            var l=server.overworld();var p=origin.offset(58,0,-15);var pl=server.getPlayerList().getPlayers().get(0);
            Block[] stand={Ic2Blocks.PERSONAL_CHEST.get(),Ic2Blocks.PERSONAL_CHEST.get(),Ic2Blocks.TRADE_O_MAT.get(),Ic2Blocks.ENERGY_O_MAT.get()};
            for(int n=0;n<stand.length;n++)l.setBlock(p.east(n*2),stand[n].defaultBlockState().setValue(BlockTileEntity.FACING,Direction.NORTH),3);
            l.blockEvent(p.east(2),Ic2Blocks.PERSONAL_CHEST.get(),1,1);
            pl.getInventory().clearContent();pl.getInventory().selected=0;
            pl.getInventory().setItem(0,new ItemStack(Ic2Blocks.PERSONAL_CHEST.get()));pl.getInventory().setItem(1,new ItemStack(Ic2Blocks.TRADE_O_MAT.get()));pl.getInventory().setItem(2,new ItemStack(Ic2Blocks.ENERGY_O_MAT.get()));
            pl.teleportTo(l,p.getX()+3.5,p.getY()+0.8,p.getZ()-3.6,0,15);
        });
        if (tick == end + 212) mc.setScreen(null);
        if (tick == end + 240) shot(mc,"personal.png");
        if (tick >= end + 245) mc.stop();
    }
}
