package ic2.core.block.machine.tileentity;

import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.upgrade.UpgradableProperty;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Solid canning machine: container + fill -> canned item (cannerBottle recipes), 2 EU/t for 200 ticks. */
public class TileEntitySolidCanner extends TileEntityStandardMachine {
    public final InvSlotConsumable canInputSlot;

    public TileEntitySolidCanner(BlockPos pos, BlockState state) {
        super(Ic2BlockEntities.SOLID_CANNER.get(), pos, state, 2, 200, 1);
        inputSlot = new InvSlotProcessableGeneric(this, "input", 1, null) {
            @Override public boolean accepts(ItemStack stack) { return getOutputFor(stack, false, true) != null; }
            @Override protected boolean allowEmptyInput() { return true; }
            @Override protected RecipeOutput getOutputFor(ItemStack input, boolean adjustInput, boolean forAccept) { return Recipes.cannerBottle.getOutputFor(canInputSlot.get(), input, adjustInput, forAccept); }
            @Override public void consume() { super.consume(); if (!canInputSlot.get().isEmpty() && canInputSlot.get().getCount() <= 0) canInputSlot.put(ItemStack.EMPTY); }
        };
        canInputSlot = new InvSlotConsumable(this, "canInput", InvSlot.Access.I, 1, InvSlot.InvSide.TOP) {
            @Override public boolean accepts(ItemStack stack) { return Recipes.cannerBottle.getOutputFor(stack, inputSlot.get(), false, true) != null; }
        };
    }

    @Override public Set<UpgradableProperty> getUpgradableProperties() { return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing); }
    @Override public ContainerBase<?> getGuiContainer(int id, Inventory inv) { return new ic2.core.block.machine.container.ContainerSolidCanner(id, inv, this); }
}
