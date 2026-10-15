package ic2.core.client;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.audio.AudioSource;
import ic2.core.block.generator.container.ContainerGenerator;
import ic2.core.block.generator.container.ContainerSolarGenerator;
import ic2.core.block.generator.gui.GuiGenerator;
import ic2.core.block.generator.gui.GuiSolarGenerator;
import ic2.core.block.machine.container.ContainerIronFurnace;
import ic2.core.block.machine.container.ContainerStandardMachine;
import ic2.core.block.machine.gui.GuiIronFurnace;
import ic2.core.block.machine.gui.GuiStandardMachine;
import ic2.core.block.machine.tileentity.*;
import ic2.core.block.wiring.ContainerElectricBlock;
import ic2.core.block.wiring.ContainerTransformer;
import ic2.core.block.wiring.GuiElectricBlock;
import ic2.core.block.wiring.GuiTransformer;
import ic2.core.item.BaseElectricItem;
import ic2.core.item.ItemUpgradeModule;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2Menus;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = IC2.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class IC2Client {
    private IC2Client() {}

    @SubscribeEvent
    static void setup(FMLClientSetupEvent event) {
        AudioSource.factory = MachineSound::new;
        event.enqueueWork(() -> {
            for (var item : Ic2Items.REGISTRY.getEntries()) {
                if (item.get() instanceof BaseElectricItem electric)
                    ItemProperties.register(electric, IC2.id("charge_level"), (stack, level, entity, seed) -> BaseElectricItem.chargeLevel(stack, electric.getMaxCharge(stack)) / 4f);
                if (item.get() instanceof ItemUpgradeModule)
                    ItemProperties.register(item.get(), IC2.id("dir"), (stack, level, entity, seed) -> ItemUpgradeModule.getDirection(stack) / 6f);
            }
        });
    }

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    static void screens(RegisterMenuScreensEvent event) {
        event.register(Ic2Menus.TILE.get(), (net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor)(menu, inv, title) -> screen((ContainerBase<?>)menu, (Inventory)inv, (Component)title));
    }

    private static Screen screen(ContainerBase<?> menu, Inventory inv, Component title) {
        if (menu instanceof ContainerStandardMachine<?> m) {
            Object base = m.base;
            if (base instanceof TileEntityMacerator) return new GuiStandardMachine(m, inv, title, "guimacerator", "ic2.Macerator.gui.name");
            if (base instanceof TileEntityExtractor) return new GuiStandardMachine(m, inv, title, "guiextractor", "ic2.Extractor.gui.name");
            if (base instanceof TileEntityCompressor) return new GuiStandardMachine(m, inv, title, "guicompressor", "ic2.Compressor.gui.name");
            if (base instanceof TileEntityElectricFurnace) return new GuiStandardMachine(m, inv, title, "guielecfurnace", "ic2.ElecFurnace.gui.name");
            if (base instanceof TileEntityRecycler) return new GuiStandardMachine(m, inv, title, "guirecycler", "ic2.Recycler.gui.name");
        }
        if (menu instanceof ContainerIronFurnace m) return new GuiIronFurnace(m, inv, title);
        if (menu instanceof ContainerGenerator m) return new GuiGenerator(m, inv, title);
        if (menu instanceof ContainerSolarGenerator m) return new GuiSolarGenerator(m, inv, title);
        if (menu instanceof ContainerElectricBlock m) return new GuiElectricBlock(m, inv, title);
        if (menu instanceof ContainerTransformer m) return new GuiTransformer(m, inv, title);
        throw new IllegalStateException("No IC2 screen for " + menu);
    }
}
