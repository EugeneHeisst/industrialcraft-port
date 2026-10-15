package ic2.core.ref;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2Menus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, IC2.MODID);
    /** Every block entity GUI; the client builds the container from the block entity at the sent position. */
    public static final Supplier<MenuType<ContainerBase<?>>> TILE = REGISTRY.register("tile", () -> IMenuTypeExtension.create((id, inv, buf) -> {
        var be = inv.player.level().getBlockEntity(buf.readBlockPos());
        if (be instanceof IHasGui gui) return gui.getGuiContainer(id, inv);
        throw new IllegalStateException("No IC2 GUI block entity at the sent position");
    }));
    private Ic2Menus() {}
}
