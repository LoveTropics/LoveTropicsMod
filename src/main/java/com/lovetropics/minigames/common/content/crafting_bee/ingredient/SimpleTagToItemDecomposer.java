package com.lovetropics.minigames.common.content.crafting_bee.ingredient;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record SimpleTagToItemDecomposer() implements IngredientDecomposer {
    public static final MapCodec<SimpleTagToItemDecomposer> CODEC = MapCodec.unit(SimpleTagToItemDecomposer::new);

    @Override
    public @Nullable List<Ingredient> decompose(Ingredient ingredient) {
        // This is a "hack". Neo will sometimes replace a vanilla recipe with a difference ingredient (#chests - #chests/trapped)
        // we just resolve it and return the first item
        if (ingredient.getCustomIngredient() instanceof DifferenceIngredient) {
            return List.of(Ingredient.of(ingredient.getItems()[0]));
        } else if (ingredient.getValues().length == 1 && ingredient.getValues()[0] instanceof Ingredient.TagValue) {
            var items = ingredient.getValues()[0].getItems();
            if (items.size() == 1) {
                return items.stream().map(Ingredient::of).toList();
            }
        }
        return null;
    }

    @Override
    public MapCodec<? extends IngredientDecomposer> codec() {
        return CODEC;
    }
}
