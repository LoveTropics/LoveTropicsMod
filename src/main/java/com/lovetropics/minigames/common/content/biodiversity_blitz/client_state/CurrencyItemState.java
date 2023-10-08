package com.lovetropics.minigames.common.content.biodiversity_blitz.client_state;

import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public record CurrencyItemState(ItemStack item) implements GameClientState {
	public static final Codec<CurrencyItemState> CODEC = RecordCodecBuilder.create(i -> i.group(
			ItemStack.CODEC.fieldOf("item").forGetter(CurrencyItemState::item)
	).apply(i, CurrencyItemState::new));

	@Override
	public GameClientStateType<?> getType() {
		return BiodiversityBlitz.CURRENCY_ITEM.get();
	}
}
