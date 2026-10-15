package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.core.BasicListRecipeManager;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2Sounds;
import ic2.core.upgrade.UpgradableProperty;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityRecycler extends TileEntityStandardMachine {
    public TileEntityRecycler(BlockPos pos, BlockState state) {
        super(Ic2BlockEntities.RECYCLER.get(), pos, state, 1, 45, 1);
        inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.recycler);
    }

    public static void init() {
        Recipes.recycler = new RecyclerRecipeManager();
        Recipes.recyclerWhitelist = new BasicListRecipeManager();
        Recipes.recyclerBlacklist = new BasicListRecipeManager();
    }

    public static int recycleChance() { return 8; }

    public static boolean getIsItemBlacklisted(ItemStack stack) {
        return Recipes.recyclerWhitelist.isEmpty() ? Recipes.recyclerBlacklist.contains(stack) : !Recipes.recyclerWhitelist.contains(stack);
    }

    @Override
    public void operateOnce(RecipeOutput output, List<ItemStack> processResult) {
        inputSlot.consume();
        if (level.random.nextInt(recycleChance()) == 0) outputSlot.add(processResult);
    }

    @Override public SoundEvent getStartSoundFile() { return Ic2Sounds.RECYCLER_OP.get(); }
    @Override public SoundEvent getInterruptSoundFile() { return Ic2Sounds.INTERRUPT_ONE.get(); }
    @Override public float getWrenchDropRate() { return 0.85f; }
    @Override public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }

    private static class RecyclerRecipeManager implements IMachineRecipeManager {
        @Override public void addRecipe(IRecipeInput input, CompoundTag metadata, ItemStack... outputs) {}
        @Override public RecipeOutput getOutputFor(ItemStack input, boolean adjustInput) {
            RecipeOutput ret = getIsItemBlacklisted(input) ? new RecipeOutput(null) : new RecipeOutput(null, new ItemStack(Ic2Items.get("scrap")));
            if (adjustInput) input.shrink(1);
            return ret;
        }
        @Override public Map<IRecipeInput, RecipeOutput> getRecipes() { return Map.of(); }
    }
}
