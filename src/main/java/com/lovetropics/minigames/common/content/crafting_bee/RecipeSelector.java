package com.lovetropics.minigames.common.content.crafting_bee;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.Optional;

public interface RecipeSelector {
    BiMap<String, MapCodec<? extends RecipeSelector>> TYPES = ImmutableBiMap.of("from_list", FromList.CODEC, "one_of", OneOf.CODEC, "from_item_tag", FromItemTag.CODEC);
    Codec<RecipeSelector> CODEC = Codec.STRING.dispatch(s -> TYPES.inverse().get(s.getType()), TYPES::get);

    SelectedRecipe select(ServerLevel level);

    MapCodec<? extends RecipeSelector> getType();

    record FromList(List<ResourceLocation> recipes) implements RecipeSelector {
        public static final MapCodec<FromList> CODEC = ResourceLocation.CODEC.listOf().fieldOf("recipes")
                .xmap(FromList::new, FromList::recipes);

        @Override
        public SelectedRecipe select(ServerLevel level) {
            Optional<RecipeHolder<?>> recipe = Optional.empty();
            while (recipe.isEmpty()) {
                var key = Util.getRandom(recipes, level.getRandom());
                recipe = level.getRecipeManager().byKey(key);
                if (recipe.isEmpty()) {
                    LogUtils.getLogger().error("Recipe '{}' doesn't exist", key);
                }
            }
            return new SelectedRecipe(recipe.get());
        }

        @Override
        public MapCodec<? extends RecipeSelector> getType() {
            return CODEC;
        }
    }

    record OneOf(List<RecipeSelector> selectors) implements RecipeSelector {
        public static final MapCodec<OneOf> CODEC = MapCodec.assumeMapUnsafe(Codec.lazyInitialized(() -> RecipeSelector.CODEC.listOf().fieldOf("selectors")
                .xmap(OneOf::new, OneOf::selectors).codec()));
        @Override
        public SelectedRecipe select(ServerLevel level) {
            return Util.getRandom(selectors, level.getRandom()).select(level);
        }

        @Override
        public MapCodec<? extends RecipeSelector> getType() {
            return CODEC;
        }
    }

    class FromItemTag implements RecipeSelector {
        public static final MapCodec<FromItemTag> CODEC = TagKey.hashedCodec(Registries.ITEM).fieldOf("tag")
                .xmap(FromItemTag::new, s -> s.tag);

        private final TagKey<Item> tag;

        private List<SelectedRecipe> cache;

        public FromItemTag(TagKey<Item> tag) {
            this.tag = tag;
        }

        @Override
        public SelectedRecipe select(ServerLevel level) {
            if (cache == null) {
                cache = level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)
                        .stream().filter(h -> h.value().getResultItem(level.registryAccess()).is(tag) && h.id().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE))
                        .map(SelectedRecipe::new)
                        .toList();
            }
            return Util.getRandom(cache, level.getRandom());
        }

        @Override
        public MapCodec<? extends RecipeSelector> getType() {
            return CODEC;
        }
    }
}
