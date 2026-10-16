package ic2.core;

import ic2.api.energy.EnergyNet;
import ic2.api.info.Info;
import ic2.api.item.ElectricItem;
import ic2.api.recipe.Recipes;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import ic2.core.energy.EnergyNetGlobal;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.GatewayElectricItemManager;
import ic2.core.network.ContainerEventPayload;
import ic2.core.network.ContainerSyncPayload;
import ic2.core.network.TileEventPayload;
import ic2.core.ref.*;
import ic2.core.upgrade.UpgradeRegistry;
import ic2.core.util.ItemInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IC2.MODID)
public final class IC2 {
    public static final String MODID = "ic2";
    public static final Logger LOG = LoggerFactory.getLogger("IC2");
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final java.util.function.Supplier<CreativeModeTab> TAB = TABS.register("ic2", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.IC2"))
        .icon(() -> new ItemStack(Ic2Items.get("electronic_circuit")))
        .displayItems((params, out) -> {
            for (var item : Ic2Items.REGISTRY.getEntries()) {
                out.accept(item.get());
                if (item.get() instanceof ic2.core.item.BaseElectricItem electric) out.accept(electric.charged());
            }
        }).build());

    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MODID, path); }

    public IC2(IEventBus modBus, ModContainer container) {
        Ic2Components.REGISTRY.register(modBus);
        Ic2Sounds.REGISTRY.register(modBus);
        Ic2Blocks.REGISTRY.register(modBus);
        Ic2Items.REGISTRY.register(modBus);
        Ic2BlockEntities.REGISTRY.register(modBus);
        Ic2Menus.REGISTRY.register(modBus);
        Ic2Features.REGISTRY.register(modBus);
        TABS.register(modBus);
        EnergyNetGlobal enet = new EnergyNetGlobal();
        EnergyNet.instance = enet;
        NeoForge.EVENT_BUS.register(enet);
        NeoForge.EVENT_BUS.register(new TickHandler());
        ElectricItem.rawManager = new ElectricItemManager();
        ElectricItem.manager = new GatewayElectricItemManager();
        ItemInfo info = new ItemInfo();
        Info.itemEnergy = info;
        Info.itemFuel = info;
        Recipes.macerator = new BasicMachineRecipeManager();
        Recipes.extractor = new BasicMachineRecipeManager();
        Recipes.compressor = new BasicMachineRecipeManager();
        TileEntityRecycler.init();
        modBus.addListener(this::setup);
        modBus.addListener(this::registerCapabilities);
        modBus.addListener(this::registerPayloads);
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MachineRecipeLoader.load("macerator", Recipes.macerator);
            MachineRecipeLoader.load("extractor", Recipes.extractor);
            MachineRecipeLoader.load("compressor", Recipes.compressor);
            MachineRecipeLoader.loadList("recycler_blacklist", Recipes.recyclerBlacklist);
            for (var upgrade : new net.minecraft.world.item.Item[]{Ic2Items.OVERCLOCKER_UPGRADE.get(), Ic2Items.TRANSFORMER_UPGRADE.get(), Ic2Items.ENERGY_STORAGE_UPGRADE.get(),
                    Ic2Items.EJECTOR_UPGRADE.get(), Ic2Items.FLUID_EJECTOR_UPGRADE.get(), Ic2Items.REDSTONE_INVERTER_UPGRADE.get(), Ic2Items.PULLING_UPGRADE.get()})
                UpgradeRegistry.register(new ItemStack(upgrade));
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (var type : Ic2BlockEntities.REGISTRY.getEntries())
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type.get(), (be, side) -> be instanceof TileEntityInventory inv ? (side == null ? new net.neoforged.neoforge.items.wrapper.InvWrapper(inv) : new SidedInvWrapper(inv, side)) : null);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        var r = event.registrar("1");
        r.playToClient(ContainerSyncPayload.TYPE, ContainerSyncPayload.CODEC, ContainerSyncPayload::handle);
        r.playToClient(TileEventPayload.TYPE, TileEventPayload.CODEC, TileEventPayload::handle);
        r.playToServer(ContainerEventPayload.TYPE, ContainerEventPayload.CODEC, ContainerEventPayload::handle);
    }
}
