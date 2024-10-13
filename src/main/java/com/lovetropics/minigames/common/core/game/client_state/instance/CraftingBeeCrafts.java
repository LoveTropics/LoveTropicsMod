package com.lovetropics.minigames.common.core.game.client_state.instance;

import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record CraftingBeeCrafts(List<Craft> crafts) implements GameClientState {
    public static final MapCodec<CraftingBeeCrafts> CODEC = Craft.CODEC.listOf()
            .fieldOf("crafts").xmap(CraftingBeeCrafts::new, CraftingBeeCrafts::crafts);

    @Override
    public GameClientStateType<?> getType() {
        return GameClientStateTypes.CRAFTING_BEE_CRAFTS.get();
    }

    public record Craft(ItemStack output, ResourceLocation recipeId, boolean done) {
        public static final Codec<Craft> CODEC = RecordCodecBuilder.create(in -> in.group(
                ItemStack.CODEC.fieldOf("output").forGetter(Craft::output),
                ResourceLocation.CODEC.fieldOf("recipe").forGetter(Craft::recipeId),
                Codec.BOOL.fieldOf("done").forGetter(Craft::done)
        ).apply(in, Craft::new));
    }
}
