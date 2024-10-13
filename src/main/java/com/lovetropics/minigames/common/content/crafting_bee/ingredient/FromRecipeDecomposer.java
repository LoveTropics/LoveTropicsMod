package com.lovetropics.minigames.common.content.crafting_bee.ingredient;

import com.lovetropics.minigames.common.content.crafting_bee.RecipeSelector;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class FromRecipeDecomposer implements IngredientDecomposer {
    public static final MapCodec<FromRecipeDecomposer> CODEC = ResourceLocation.CODEC.listOf()
            .fieldOf("recipes").xmap(FromRecipeDecomposer::new, d -> d.recipes);

    private final List<ResourceLocation> recipes;

    private final Map<Item, List<Ingredient>> cache = new IdentityHashMap<>();

    public FromRecipeDecomposer(List<ResourceLocation> recipes) {
        this.recipes = recipes;
    }

    @Override
    public @Nullable List<Ingredient> decompose(Ingredient ingredient) {
        if (ingredient.getValues().length == 1 && ingredient.getValues()[0] instanceof Ingredient.ItemValue(ItemStack item)) {
            return cache.get(item.getItem());
        }
        return null;
    }

    @Override
    public void prepareCache(ServerLevel level) {
        cache.clear();

        for (ResourceLocation recipe : recipes) {
            level.getServer().getRecipeManager().byKey(recipe).map(RecipeSelector.SelectedRecipe::new)
                    .ifPresent(r -> cache.put(
                            r.getResult(level.registryAccess()).getItem(),
                            r.decompose()
                    ));
        }
    }

    @Override
    public MapCodec<? extends IngredientDecomposer> codec() {
        return CODEC;
    }
}
