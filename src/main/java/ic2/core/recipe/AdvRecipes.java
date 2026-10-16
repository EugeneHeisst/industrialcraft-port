package ic2.core.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.neoforge.registries.DeferredRegister;

/** AdvRecipe / AdvShapelessRecipe of the original: the result receives the summed EU charge of all electric ingredients. */
public final class AdvRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, IC2.MODID);
    public static final Supplier<RecipeSerializer<Shaped>> SHAPED = SERIALIZERS.register("shaped", ShapedSerializer::new);
    public static final Supplier<RecipeSerializer<Shapeless>> SHAPELESS = SERIALIZERS.register("shapeless", ShapelessSerializer::new);
    private AdvRecipes() {}

    static ItemStack withCharge(ItemStack ret, CraftingInput input) {
        double charge = 0;
        for (int i = 0; i < input.size(); i++) if (!input.getItem(i).isEmpty()) charge += ElectricItem.manager.getCharge(input.getItem(i));
        if (charge > 0) ElectricItem.manager.charge(ret, charge, Integer.MAX_VALUE, true, false);
        return ret;
    }

    public static class Shaped extends ShapedRecipe {
        final ShapedRecipePattern pattern;
        final ItemStack result;
        public Shaped(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification) {
            super(group, category, pattern, result, showNotification);
            this.pattern = pattern;
            this.result = result;
        }
        @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) { return withCharge(super.assemble(input, registries), input); }
        @Override public RecipeSerializer<?> getSerializer() { return SHAPED.get(); }
    }

    public static class Shapeless extends ShapelessRecipe {
        final ItemStack result;
        public Shapeless(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) { super(group, category, result, ingredients); this.result = result; }
        @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) { return withCharge(super.assemble(input, registries), input); }
        @Override public RecipeSerializer<?> getSerializer() { return SHAPELESS.get(); }
    }

    static class ShapedSerializer implements RecipeSerializer<Shaped> {
        static final MapCodec<Shaped> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(Shaped::getGroup),
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(Shaped::category),
            ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result),
            Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(Shaped::showNotification)
        ).apply(i, Shaped::new));
        static final StreamCodec<RegistryFriendlyByteBuf, Shaped> STREAM = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Shaped::getGroup, CraftingBookCategory.STREAM_CODEC, Shaped::category, ShapedRecipePattern.STREAM_CODEC, r -> r.pattern,
            ItemStack.STREAM_CODEC, r -> r.result, ByteBufCodecs.BOOL, Shaped::showNotification, Shaped::new);
        @Override public MapCodec<Shaped> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, Shaped> streamCodec() { return STREAM; }
    }

    static class ShapelessSerializer implements RecipeSerializer<Shapeless> {
        static final MapCodec<Shapeless> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(Shapeless::getGroup),
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(Shapeless::category),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result),
            Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").xmap(l -> { NonNullList<Ingredient> n = NonNullList.create(); n.addAll(l); return n; }, l -> l).forGetter(Shapeless::getIngredients)
        ).apply(i, Shapeless::new));
        static final StreamCodec<RegistryFriendlyByteBuf, Shapeless> STREAM = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Shapeless::getGroup, CraftingBookCategory.STREAM_CODEC, Shapeless::category, ItemStack.STREAM_CODEC, r -> r.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).map(l -> { NonNullList<Ingredient> n = NonNullList.create(); n.addAll(l); return n; }, l -> l), Shapeless::getIngredients, Shapeless::new);
        @Override public MapCodec<Shapeless> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, Shapeless> streamCodec() { return STREAM; }
    }
}
