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
        ic2.core.item.tool.ItemScanner.ScanResult.clientHandler = (p, ctx) -> GuiToolScanner.open(p);
        event.enqueueWork(() -> {
            for (var item : Ic2Items.REGISTRY.getEntries()) {
                if (item.get() instanceof BaseElectricItem electric)
                    ItemProperties.register(electric, IC2.id("charge_level"), (stack, level, entity, seed) -> BaseElectricItem.chargeLevel(stack, electric.getMaxCharge(stack)) / 4f);
                if (item.get() instanceof ic2.core.item.tool.ItemNanoSaber)
                    ItemProperties.register(item.get(), IC2.id("active"), (stack, level, entity, seed) -> ic2.core.item.tool.ItemNanoSaber.isActive(stack) ? 1 : 0);
                if (item.get() instanceof ItemUpgradeModule)
                    ItemProperties.register(item.get(), IC2.id("dir"), (stack, level, entity, seed) -> ItemUpgradeModule.getDirection(stack) / 6f);
            }
        });
    }

    /** Rubber leaves use the constant birch foliage colour like the original. */
    @SubscribeEvent
    static void blockColors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tint) -> net.minecraft.world.level.FoliageColor.getBirchColor(), ic2.core.ref.Ic2Blocks.RUBBER_LEAVES.get());
    }

    @SubscribeEvent
    static void itemColors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> net.minecraft.world.level.FoliageColor.getBirchColor(), ic2.core.ref.Ic2Blocks.RUBBER_LEAVES.get());
    }

    @SubscribeEvent
    static void renderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ic2.core.ref.Ic2BlockEntities.CROP.get(), CropRenderer::new);
        event.registerEntityRenderer(ic2.core.ref.Ic2Entities.MINING_LASER.get(), RenderMiningLaser::new);
        event.registerBlockEntityRenderer(ic2.core.ref.Ic2BlockEntities.WIND_KINETIC_GENERATOR.get(), KineticGeneratorRenderer::new);
        event.registerBlockEntityRenderer(ic2.core.ref.Ic2BlockEntities.WATER_KINETIC_GENERATOR.get(), KineticGeneratorRenderer::new);
    }

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    static void screens(RegisterMenuScreensEvent event) {
        event.register(Ic2Menus.TILE.get(), (net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor)(menu, inv, title) -> screen((ContainerBase<?>)menu, (Inventory)inv, (Component)title));
        event.register(Ic2Menus.HANDHELD.get(), (net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor)(menu, inv, title) -> screen((ContainerBase<?>)menu, (Inventory)inv, (Component)title));
    }

    private static Screen screen(ContainerBase<?> menu, Inventory inv, Component title) {
        if (menu.getClass() == ContainerStandardMachine.class && menu instanceof ContainerStandardMachine<?> m) {
            Object base = m.base;
            if (base instanceof TileEntityMacerator) return new GuiStandardMachine(m, inv, title, "guimacerator", "ic2.Macerator.gui.name");
            if (base instanceof TileEntityExtractor) return new GuiStandardMachine(m, inv, title, "guiextractor", "ic2.Extractor.gui.name");
            if (base instanceof TileEntityCompressor) return new GuiStandardMachine(m, inv, title, "guicompressor", "ic2.Compressor.gui.name");
            if (base instanceof TileEntityElectricFurnace) return new GuiStandardMachine(m, inv, title, "guielecfurnace", "ic2.ElecFurnace.gui.name");
            if (base instanceof TileEntityRecycler) return new GuiStandardMachine(m, inv, title, "guirecycler", "ic2.Recycler.gui.name");
        }
        if (menu instanceof ic2.core.block.machine.container.ContainerInduction m) return new ic2.core.block.machine.gui.GuiMachines.Induction(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerMetalFormer m) return new ic2.core.block.machine.gui.GuiMachines.MetalFormer(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerOreWashing m) return new ic2.core.block.machine.gui.GuiMachines.OreWashing(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerCentrifuge m) return new ic2.core.block.machine.gui.GuiMachines.Centrifuge(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerBlockCutter m) return new ic2.core.block.machine.gui.GuiMachines.BlockCutter(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerBlastFurnace m) return new ic2.core.block.machine.gui.GuiMachines.BlastFurnace(m, inv, title);
        if (menu instanceof ic2.core.block.heatgenerator.ContainerHeatGenerators.Solid m) return new ic2.core.block.heatgenerator.GuiHeatGenerators.Solid(m, inv, title);
        if (menu instanceof ic2.core.block.heatgenerator.ContainerHeatGenerators.RT m) return new ic2.core.block.heatgenerator.GuiHeatGenerators.RT(m, inv, title);
        if (menu instanceof ic2.core.block.heatgenerator.ContainerHeatGenerators.Electric m) return new ic2.core.block.heatgenerator.GuiHeatGenerators.Electric(m, inv, title);
        if (menu instanceof ic2.core.block.generator.container.ContainerGenerators2.RTG m) return new ic2.core.block.generator.gui.GuiGenerators2.RTG(m, inv, title);
        if (menu instanceof ic2.core.block.generator.container.ContainerGenerators2.Semifluid m) return new ic2.core.block.generator.gui.GuiGenerators2.Semifluid(m, inv, title);
        if (menu instanceof ic2.core.block.generator.container.ContainerGenerators2.Stirling m) return new ic2.core.block.generator.gui.GuiGenerators2.Stirling(m, inv, title);
        if (menu instanceof ic2.core.block.generator.container.ContainerGenerators2.Kinetic m) return new ic2.core.block.generator.gui.GuiGenerators2.Kinetic(m, inv, title);
        if (menu instanceof ic2.core.block.kineticgenerator.ContainerKineticGenerators.Wind m) return new ic2.core.block.kineticgenerator.GuiKineticGenerators.Wind(m, inv, title);
        if (menu instanceof ic2.core.block.kineticgenerator.ContainerKineticGenerators.Water m) return new ic2.core.block.kineticgenerator.GuiKineticGenerators.Water(m, inv, title);
        if (menu instanceof ic2.core.block.kineticgenerator.ContainerKineticGenerators.Electric m) return new ic2.core.block.kineticgenerator.GuiKineticGenerators.Electric(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerMatter m) return new ic2.core.block.machine.gui.GuiMatter(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerUu.Scanner m) return new ic2.core.block.machine.gui.GuiUu.Scanner(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerUu.Replicator m) return new ic2.core.block.machine.gui.GuiUu.Replicator(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerUu.PatternStorage m) return new ic2.core.block.machine.gui.GuiUu.PatternStorage(m, inv, title);
        if (menu instanceof ic2.core.block.reactor.ContainerNuclearReactor m) return new ic2.core.block.reactor.GuiNuclearReactor(m, inv, title);
        if (menu instanceof ic2.core.block.reactor.ContainerReactorFluidPort m) return new ic2.core.block.reactor.GuiNuclearReactor.FluidPort(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerFluidMachines.Distributor m) return new ic2.core.block.machine.gui.GuiFluidMachines(m, inv, title, "fluid_distributor", "guifluiddistributor");
        if (menu instanceof ic2.core.block.machine.container.ContainerFluidMachines.Regulator m) return new ic2.core.block.machine.gui.GuiFluidMachines(m, inv, title, "fluid_regulator", "guifluidregulator");
        if (menu instanceof ic2.core.block.machine.container.ContainerFluidMachines.Condenser m) return new ic2.core.block.machine.gui.GuiFluidMachines(m, inv, title, "condenser", "guicondenser");
        if (menu instanceof ic2.core.block.machine.container.ContainerFluidMachines.Solar m) return new ic2.core.block.machine.gui.GuiFluidMachines(m, inv, title, "solar_distiller", "guisolardestiller");
        if (menu instanceof ic2.core.block.machine.container.ContainerLogistics.Sorter m) return new ic2.core.block.machine.gui.GuiLogistics.Sorter(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerLogistics.Buffer m) return new ic2.core.block.machine.gui.GuiLogistics.Buffer(m, inv, title);
        if (menu instanceof ic2.core.crop.ContainerCrops.Matron m) return new ic2.core.crop.GuiCrops.Matron(m, inv, title);
        if (menu instanceof ic2.core.crop.ContainerCrops.Harvester m) return new ic2.core.crop.GuiCrops.Harvester(m, inv, title);
        if (menu instanceof ic2.core.item.ItemCropAnalyzer.Container m) return new ic2.core.crop.GuiCrops.Analyzer(m, inv, title);
        if (menu instanceof ic2.core.item.ItemToolbox.Container m) return new ic2.core.item.GuiToolbox(m, inv, title);
        if (menu instanceof ic2.core.item.tool.ItemToolMeter.Container m) return new ic2.core.item.tool.GuiToolMeter(m, inv, title);
        if (menu instanceof ic2.core.block.wiring.ContainerChargepad m) return new ic2.core.block.wiring.GuiChargepad(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerMagnetizer m) return new ic2.core.block.machine.gui.GuiMagnetizer(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerElectrolyzer m) return new ic2.core.block.machine.gui.GuiMagnetizer.Electrolyzer(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerCanner m) return new ic2.core.block.machine.gui.GuiCanners.Canner(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerSolidCanner m) return new ic2.core.block.machine.gui.GuiCanners.SolidCanner(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerFluidBottler m) return new ic2.core.block.machine.gui.GuiCanners.FluidBottler(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerMiner m) return new ic2.core.block.machine.gui.GuiMiner(m, inv, title);
        if (menu instanceof ic2.core.block.machine.container.ContainerPump m) return new ic2.core.block.machine.gui.GuiPump(m, inv, title);
        if (menu instanceof ContainerIronFurnace m) return new GuiIronFurnace(m, inv, title);
        if (menu instanceof ContainerGenerator m) return new GuiGenerator(m, inv, title);
        if (menu instanceof ic2.core.block.generator.container.ContainerGeoGenerator m) return new ic2.core.block.generator.gui.GuiGeoGenerator(m, inv, title);
        if (menu instanceof ic2.core.block.generator.container.ContainerWaterGenerator m) return new ic2.core.block.generator.gui.GuiWaterGenerator(m, inv, title);
        if (menu instanceof ic2.core.block.generator.container.ContainerWindGenerator m) return new ic2.core.block.generator.gui.GuiWindGenerator(m, inv, title);
        if (menu instanceof ContainerSolarGenerator m) return new GuiSolarGenerator(m, inv, title);
        if (menu instanceof ContainerElectricBlock m) return new GuiElectricBlock(m, inv, title);
        if (menu instanceof ContainerTransformer m) return new GuiTransformer(m, inv, title);
        throw new IllegalStateException("No IC2 screen for " + menu);
    }
}
