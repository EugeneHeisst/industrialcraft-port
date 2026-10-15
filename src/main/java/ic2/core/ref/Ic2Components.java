package ic2.core.ref;

import com.mojang.serialization.Codec;
import ic2.core.IC2;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2Components {
    public static final DeferredRegister<DataComponentType<?>> REGISTRY = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, IC2.MODID);
    /** EU stored in an electric item. */
    public static final Supplier<DataComponentType<Double>> CHARGE = REGISTRY.register("charge", () -> DataComponentType.<Double>builder().persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE).build());
    /** EU kept in a wrenched storage block. */
    public static final Supplier<DataComponentType<Double>> ENERGY = REGISTRY.register("energy", () -> DataComponentType.<Double>builder().persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE).build());
    /** Side of ejector/pulling upgrades, 0 = any. */
    public static final Supplier<DataComponentType<Integer>> UPGRADE_DIR = REGISTRY.register("upgrade_dir", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    private Ic2Components() {}
}
