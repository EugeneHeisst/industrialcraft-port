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
        WATERMILL_LOOP = reg("generators.watermill_loop"), WINDGEN_LOOP = reg("generators.windgen_loop"), MASS_FAB_LOOP = reg("generators.mass_fab_loop"), MASS_FAB_SCRAP = reg("generators.mass_fab_scrap"), WRENCH = reg("tools.wrench"), TREETAP = reg("tools.treetap"),
        MINER_OP = reg("machines.miner_op"), PUMP_OP = reg("machines.pump_op"), OD_SCANNER = reg("tools.od_scanner"),
        TELEPORTER_CHARGED = reg("machines.teleporter.charged_loop"), TELEPORTER_USE = reg("machines.teleporter.use"),
        LASER = reg("tools.mining_laser.mining"), LASER_LOW_FOCUS = reg("tools.mining_laser.low_focus"), LASER_LONG_RANGE = reg("tools.mining_laser.long_range"),
        LASER_SCATTER = reg("tools.mining_laser.scatter"), LASER_EXPLOSIVE = reg("tools.mining_laser.explosive");
    private static Supplier<SoundEvent> reg(String name) { return REGISTRY.register(name, () -> SoundEvent.createVariableRangeEvent(IC2.id(name))); }
    private Ic2Sounds() {}
}
