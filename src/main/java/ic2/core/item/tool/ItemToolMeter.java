package ic2.core.item.tool;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.item.IBoxable;
import ic2.core.ContainerBase;
import ic2.core.item.IHandHeldInventory;
import ic2.core.ref.Ic2Menus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

/** EU meter: shows average / min / max energy in, out, gain or voltage of an energy net tile. */
public class ItemToolMeter extends Item implements IBoxable, IHandHeldInventory {
    public ItemToolMeter(Properties properties) { super(properties.stacksTo(1)); }
    @Override public boolean canBeStoredInToolbox(ItemStack stack) { return true; }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        if (ctx.getLevel().isClientSide) return InteractionResult.SUCCESS;
        BlockEntity te = EnergyNet.instance.getTileEntity(ctx.getLevel(), ctx.getClickedPos());
        if (!(ctx.getPlayer() instanceof ServerPlayer sp)) return InteractionResult.PASS;
        if (!(te instanceof IEnergySource) && !(te instanceof IEnergyConductor) && !(te instanceof IEnergySink)) { sp.displayClientMessage(Component.literal("Not an energy net tile"), false); return InteractionResult.SUCCESS; }
        InteractionHand hand = ctx.getHand();
        IHandHeldInventory.open(sp, hand, id -> { var c = new Container(id, sp.getInventory()); c.uut = te; return c; });
        return InteractionResult.SUCCESS;
    }

    @Override public ContainerBase<?> createContainer(int id, Inventory inv, ItemStack stack, InteractionHand hand) { return new Container(id, inv); }

    public enum Mode { EnergyIn, EnergyOut, EnergyGain, Voltage }

    public static class Container extends ContainerBase<Object> {
        public BlockEntity uut;
        public double resultAvg, resultMin, resultMax;
        public int resultCount;
        public Mode mode = Mode.EnergyIn;

        public Container(int id, Inventory inv) {
            super(Ic2Menus.HANDHELD.get(), id, inv, new Object());
            addPlayerInventorySlots(inv, 176, 218);
            sync(() -> resultAvg, v -> resultAvg = v);
            sync(() -> resultMin, v -> resultMin = v);
            sync(() -> resultMax, v -> resultMax = v);
            sync(() -> resultCount, v -> resultCount = (int)v);
            sync(() -> mode.ordinal(), v -> mode = Mode.values()[(int)v]);
        }

        @Override
        public void broadcastChanges() {
            if (uut != null) {
                NodeStats stats = EnergyNet.instance.getNodeStats(uut);
                double r = switch (mode) { case EnergyIn -> stats.getEnergyIn(); case EnergyOut -> stats.getEnergyOut(); case EnergyGain -> stats.getEnergyIn() - stats.getEnergyOut(); case Voltage -> stats.getVoltage(); };
                if (resultCount == 0) resultAvg = resultMin = resultMax = r;
                else { resultMin = Math.min(resultMin, r); resultMax = Math.max(resultMax, r); resultAvg = (resultAvg * resultCount + r) / (resultCount + 1); }
                resultCount++;
            }
            super.broadcastChanges();
        }

        @Override public boolean stillValid(net.minecraft.world.entity.player.Player p) { return uut == null || !uut.isRemoved(); }

        @Override
        public void onContainerEvent(String event) {
            if (event.equals("reset")) resultCount = 0;
            else if (event.startsWith("mode")) { mode = Mode.values()[Math.floorMod(Integer.parseInt(event.substring(4)), 4)]; resultCount = 0; }
        }
    }
}
