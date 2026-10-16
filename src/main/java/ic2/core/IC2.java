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
                if (item.get() instanceof ic2.core.item.tool.ItemElectricTool tool) out.accept(tool.getItemStack(Double.POSITIVE_INFINITY));
                if (item.get() instanceof ic2.core.item.armor.ItemArmorElectric armor) out.accept(armor.charged());
                if (item.get() instanceof ic2.api.item.IElectricItem && !(item.get() instanceof ic2.core.item.BaseElectricItem) && !(item.get() instanceof ic2.core.item.tool.ItemElectricTool) && !(item.get() instanceof ic2.core.item.armor.ItemArmorElectric)) {
                    ItemStack charged = new ItemStack(item.get());
                    ic2.api.item.ElectricItem.manager.charge(charged, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, false);
                    out.accept(charged);
                }
            }
        }).build());

    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MODID, path); }

    public IC2(IEventBus modBus, ModContainer container) {
        Ic2Components.REGISTRY.register(modBus);
        Ic2Sounds.REGISTRY.register(modBus);
        ic2.core.fluid.Ic2Fluids.TYPES.register(modBus);
        ic2.core.fluid.Ic2Fluids.FLUIDS.register(modBus);
        Ic2Blocks.REGISTRY.register(modBus);
        Ic2ArmorMaterials.REGISTRY.register(modBus);
        Ic2Items.REGISTRY.register(modBus);
        Ic2BlockEntities.REGISTRY.register(modBus);
        Ic2Menus.REGISTRY.register(modBus);
        Ic2Features.REGISTRY.register(modBus);
        ic2.core.recipe.AdvRecipes.SERIALIZERS.register(modBus);
        TABS.register(modBus);
        EnergyNetGlobal enet = new EnergyNetGlobal();
        EnergyNet.instance = enet;
        NeoForge.EVENT_BUS.register(enet);
        NeoForge.EVENT_BUS.register(new TickHandler());
        NeoForge.EVENT_BUS.register(new WindSim.Ticker());
        ElectricItem.rawManager = new ElectricItemManager();
        ElectricItem.manager = new GatewayElectricItemManager();
        ItemInfo info = new ItemInfo();
        Info.itemEnergy = info;
        Info.itemFuel = info;
        Recipes.macerator = new BasicMachineRecipeManager();
        Recipes.extractor = new BasicMachineRecipeManager();
        Recipes.compressor = new BasicMachineRecipeManager();
        TileEntityRecycler.init();
        Recipes.metalformerExtruding = new BasicMachineRecipeManager();
        Recipes.metalformerCutting = new BasicMachineRecipeManager();
        Recipes.metalformerRolling = new BasicMachineRecipeManager();
        Recipes.oreWashing = new BasicMachineRecipeManager();
        Recipes.centrifuge = new BasicMachineRecipeManager();
        Recipes.blockcutter = new BasicMachineRecipeManager();
        Recipes.blastfurance = new BasicMachineRecipeManager();
        Recipes.matterAmplifier = new BasicMachineRecipeManager();
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
            MachineRecipeLoader.load("metal_former_extruding", Recipes.metalformerExtruding);
            MachineRecipeLoader.load("metal_former_cutting", Recipes.metalformerCutting);
            MachineRecipeLoader.load("metal_former_rolling", Recipes.metalformerRolling);
            Recipes.metalformerCutting.addRecipe(new ic2.api.recipe.RecipeInputItemStack(new ItemStack(Ic2Items.get("iron_casing")), 1), null, new ItemStack(Ic2Items.get("coin"), 2));
            MachineRecipeLoader.load("ore_washer", Recipes.oreWashing);
            MachineRecipeLoader.load("thermal_centrifuge", Recipes.centrifuge);
            MachineRecipeLoader.load("block_cutter", Recipes.blockcutter);
            MachineRecipeLoader.load("blast_furnace", Recipes.blastfurance);
            var amp5000 = new net.minecraft.nbt.CompoundTag(); amp5000.putInt("amplification", 5000);
            var amp45000 = new net.minecraft.nbt.CompoundTag(); amp45000.putInt("amplification", 45000);
            Recipes.matterAmplifier.addRecipe(new ic2.api.recipe.RecipeInputItemStack(new ItemStack(Ic2Items.get("scrap")), 1), amp5000);
            Recipes.matterAmplifier.addRecipe(new ic2.api.recipe.RecipeInputItemStack(new ItemStack(Ic2Items.SCRAP_BOX.get()), 1), amp45000);
            ic2.core.item.ItemScrapbox.init();
            for (var upgrade : new net.minecraft.world.item.Item[]{Ic2Items.OVERCLOCKER_UPGRADE.get(), Ic2Items.TRANSFORMER_UPGRADE.get(), Ic2Items.ENERGY_STORAGE_UPGRADE.get(),
                    Ic2Items.EJECTOR_UPGRADE.get(), Ic2Items.FLUID_EJECTOR_UPGRADE.get(), Ic2Items.REDSTONE_INVERTER_UPGRADE.get(), Ic2Items.PULLING_UPGRADE.get()})
                UpgradeRegistry.register(new ItemStack(upgrade));
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        registerFluids(event);
        for (var type : Ic2BlockEntities.REGISTRY.getEntries())
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type.get(), (be, side) -> be instanceof TileEntityInventory inv ? (side == null ? new net.neoforged.neoforge.items.wrapper.InvWrapper(inv) : new SidedInvWrapper(inv, side)) : null);
    }

    private static void registerFluids(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, Ic2BlockEntities.GEOTHERMAL_GENERATOR.get(), (be, side) -> be);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, Ic2BlockEntities.ORE_WASHING_PLANT.get(), (be, side) -> be);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, Ic2BlockEntities.SEMIFLUID_GENERATOR.get(), (be, side) -> be);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, Ic2BlockEntities.MASS_FABRICATOR.get(), (be, side) -> be);
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new ic2.core.item.resources.ItemCell.Handler(stack), Ic2Items.EMPTY_CELL.get(), Ic2Items.WATER_CELL.get(), Ic2Items.LAVA_CELL.get());
        for (var cell : Ic2Items.FLUID_CELLS.values()) event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new ic2.core.item.resources.ItemCell.Handler(stack), cell.get());
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> ic2.core.item.resources.ItemFluidCell.handler(stack), Ic2Items.UNIVERSAL_FLUID_CELL.get());
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        var r = event.registrar("1");
        r.playToClient(ContainerSyncPayload.TYPE, ContainerSyncPayload.CODEC, ContainerSyncPayload::handle);
        r.playToClient(TileEventPayload.TYPE, TileEventPayload.CODEC, TileEventPayload::handle);
        r.playToServer(ContainerEventPayload.TYPE, ContainerEventPayload.CODEC, ContainerEventPayload::handle);
        r.playToClient(ic2.core.item.tool.ItemScanner.ScanResult.TYPE, ic2.core.item.tool.ItemScanner.ScanResult.CODEC, ic2.core.item.tool.ItemScanner.ScanResult::handle);
        r.playToServer(ic2.core.util.Keyboard.KeyStatePayload.TYPE, ic2.core.util.Keyboard.KeyStatePayload.CODEC, ic2.core.util.Keyboard.KeyStatePayload::handle);
    }
}
