package com.lovetropics.minigames.common.content.crafting_bee.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public record PreferItemFromTagDecomposer(Map<TagKey<Item>, Item> preferences) implements IngredientDecomposer {
    public PreferItemFromTagDecomposer(Map<TagKey<Item>, Item> preferences) {
        this.preferences = new IdentityHashMap<>(preferences);
    }

    public static final MapCodec<PreferItemFromTagDecomposer> CODEC = Codec.unboundedMap(
            TagKey.hashedCodec(Registries.ITEM), BuiltInRegistries.ITEM.byNameCodec()
    ).fieldOf("preferences").xmap(PreferItemFromTagDecomposer::new, PreferItemFromTagDecomposer::preferences);

    @Override
    public @Nullable List<Ingredient> decompose(Ingredient ingredient) {
        if (ingredient.getValues().length == 1 && ingredient.getValues()[0] instanceof Ingredient.TagValue(TagKey<Item> tag)) {
            var pref = preferences.get(tag);
            if (pref != null) {
                return List.of(Ingredient.of(pref));
            }
        }
        return null;
    }

    @Override
    public MapCodec<? extends IngredientDecomposer> codec() {
        return CODEC;
    }
}
