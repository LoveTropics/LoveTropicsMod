package com.lovetropics.minigames.common.content.crafting_bee;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.List;

public interface RecipeSelector {
    BiMap<String, MapCodec<? extends RecipeSelector>> TYPES = ImmutableBiMap.of("from_list", FromList.CODEC);
    MapCodec<RecipeSelector> CODEC = Codec.STRING.dispatchMap(s -> TYPES.inverse().get(s.getType()), TYPES::get);

    SelectedRecipe select(ServerLevel level);

    MapCodec<? extends RecipeSelector> getType();

    record SelectedRecipe(ResourceLocation id, Either<ShapedRecipe, ShapelessRecipe> recipe) {
        public SelectedRecipe(RecipeHolder<?> holder) {
            this(holder.id(), holder.value() instanceof ShapedRecipe sr ? Either.left(sr) : Either.right((ShapelessRecipe) holder.value()));
        }

        public ItemStack getResult(RegistryAccess access) {
            return recipe.map(rp -> rp.getResultItem(access), rp -> rp.getResultItem(access));
        }

        public List<Ingredient> decompose() {
            return recipe.map(ShapedRecipe::getIngredients, ShapelessRecipe::getIngredients);
        }
    }

    record FromList(List<ResourceLocation> recipes) implements RecipeSelector {
        public static final MapCodec<FromList> CODEC = ResourceLocation.CODEC.listOf().fieldOf("recipes")
                .xmap(FromList::new, FromList::recipes);

        @Override
        public SelectedRecipe select(ServerLevel level) {
            var key = Util.getRandom(recipes, level.getRandom());
            var recipe = level.getRecipeManager().byKey(key).orElseThrow(() -> new NullPointerException("Recipe " + key + " doesn't exist"));
            return new SelectedRecipe(recipe);
        }

        @Override
        public MapCodec<? extends RecipeSelector> getType() {
            return CODEC;
        }
    }
}
