package com.lovetropics.minigames.common.content.crafting_bee;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.List;

public record SelectedRecipe(ResourceLocation id, Either<ShapedRecipe, ShapelessRecipe> recipe) {
    public SelectedRecipe(RecipeHolder<?> holder) {
        this(holder.id(), holder.value() instanceof ShapedRecipe sr ? Either.left(sr) : Either.right((ShapelessRecipe) holder.value()));
    }

    public SelectedRecipe(ResourceLocation id, RecipeManager manager) {
        this(manager.byKey(id).orElseThrow());
    }

    public ItemStack getResult(RegistryAccess access) {
        return recipe.map(rp -> rp.getResultItem(access), rp -> rp.getResultItem(access));
    }

    public List<Ingredient> decompose() {
        return recipe.map(ShapedRecipe::getIngredients, ShapelessRecipe::getIngredients);
    }
}
