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
            origin = player.blockPosition().offset(0, 0, 4);
            for (BlockPos p : BlockPos.betweenClosed(origin.offset(-12, 0, -2), origin.offset(12, 4, 3))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            server.setDifficulty(net.minecraft.world.Difficulty.PEACEFUL, true);
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            player.getAbilities().invulnerable = true;
            player.setHealth(player.getMaxHealth());
            int i = 0;
            for (var b : Ic2Blocks.REGISTRY.getEntries()) {
                if (b.get() instanceof net.minecraft.world.level.block.LiquidBlock) continue;
                BlockPos p = origin.offset(-9 + i % 19, i / 19 * 2, 0);
                var state = b.get().defaultBlockState();
                if (state.hasProperty(BlockTileEntity.FACING)) state = state.setValue(BlockTileEntity.FACING, Direction.NORTH).setValue(BlockTileEntity.ACTIVE, i % 2 == 0);
                level.setBlock(p, state, 3);
                if (level.getBlockEntity(p) instanceof IHasGui) guis.add(p);
                if (level.getBlockEntity(p) instanceof ic2.core.block.kineticgenerator.TileEntityKineticGenerators.Rotor rotorGen) { rotorGen.rotorSlot.put(new ItemStack(Ic2Items.CARBON_ROTOR.get())); rotorGen.setRotationSpeed(1); }
                i++;
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
                if (be instanceof IHasGui gui) player.openMenu(new SimpleMenuProvider((id, inv, pl) -> gui.getGuiContainer(id, inv), Component.empty()), buf -> buf.writeBlockPos(p));
            });
            if ((tick - 150) % 20 == 15) shot(mc, "gui_" + net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(server.overworld().getBlockState(p).getBlock()).getPath() + ".png");
            if ((tick - 150) % 20 == 18) mc.player.closeContainer();
        }
        if (tick >= 150 && g >= guis.size() + 1) mc.stop();
    }
}
