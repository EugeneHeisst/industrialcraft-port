package ic2.core.block.reactor;

import ic2.core.ContainerBase;
import ic2.core.block.machine.container.FluidSync;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ContainerNuclearReactor extends ContainerBase<TileEntityNuclearReactor> {
    public final int size;
    public ContainerNuclearReactor(int id, Inventory inv, TileEntityNuclearReactor base) {
        super(id, inv, base);
        size = base.getReactorSize();
        for (int y = 0; y < 6; y++) for (int x = 0; x < size; x++) addSlot(new SlotInvSlot(base.reactorSlot, y * 9 + x, 26 + 18 * x, 25 + 18 * y));
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(inv, col + row * 9 + 9, 26 + col * 18, 161 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inv, col, 26 + col * 18, 219));
        addSlot(new SlotInvSlot(base.coolantInputSlot, 0, 8, 25));
        addSlot(new SlotInvSlot(base.hotCoolantInputSlot, 0, 188, 25));
        addSlot(new SlotInvSlot(base.coolantOutputSlot, 0, 8, 115));
        addSlot(new SlotInvSlot(base.hotCoolantOutputSlot, 0, 188, 115));
        sync(() -> base.heat, v -> base.heat = (int)v);
        sync(() -> base.maxHeat, v -> base.maxHeat = (int)v);
        sync(() -> base.emitHeat, v -> base.emitHeat = (int)v);
        sync(() -> base.output, v -> base.output = (float)v);
        sync(() -> base.isFluidCooled() ? 1 : 0, v -> base.setFluidCooledClient(v > 0));
        FluidSync.tank(this, base.inputTank);
        FluidSync.tank(this, base.outputTank);
    }
}
