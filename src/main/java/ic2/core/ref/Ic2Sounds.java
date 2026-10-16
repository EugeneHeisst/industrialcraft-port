package ic2.core.ref;

import ic2.core.IC2;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2Sounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, IC2.MODID);
    public static final Supplier<SoundEvent> MACERATOR_OP = reg("machines.macerator_op"), EXTRACTOR_OP = reg("machines.extractor_op"), COMPRESSOR_OP = reg("machines.compressor_op"),
        RECYCLER_OP = reg("machines.recycler_op"), INTERRUPT_ONE = reg("machines.interrupt_one"), ELECTRO_FURNACE_LOOP = reg("machines.electro_furnace_loop"),
        IRON_FURNACE_OP = reg("machines.iron_furnace_op"), GENERATOR_LOOP = reg("generators.generator_loop"), GEOTHERMAL_LOOP = reg("generators.geothermal_loop"),
        WATERMILL_LOOP = reg("generators.watermill_loop"), WINDGEN_LOOP = reg("generators.windgen_loop"), MASS_FAB_LOOP = reg("generators.mass_fab_loop"), MASS_FAB_SCRAP = reg("generators.mass_fab_scrap"), WRENCH = reg("tools.wrench"), TREETAP = reg("tools.treetap");
    private static Supplier<SoundEvent> reg(String name) { return REGISTRY.register(name, () -> SoundEvent.createVariableRangeEvent(IC2.id(name))); }
    private Ic2Sounds() {}
}
