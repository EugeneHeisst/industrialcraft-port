package ic2.core.client;

import ic2.core.IC2;
import ic2.core.fluid.Ic2Fluids;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = IC2.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FluidClient {
    private FluidClient() {}

    @SubscribeEvent
    static void extensions(RegisterClientExtensionsEvent event) {
        for (var f : Ic2Fluids.ALL) {
            ResourceLocation still = IC2.id("block/fluid/" + f.texture + "_still"), flow = IC2.id("block/fluid/" + f.texture + (f.hasFlowTexture ? "_flow" : "_still"));
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override public ResourceLocation getStillTexture() { return still; }
                @Override public ResourceLocation getFlowingTexture() { return flow; }
            }, f.type.get());
        }
    }
}
