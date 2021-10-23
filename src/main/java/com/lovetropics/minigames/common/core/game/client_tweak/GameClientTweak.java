package com.lovetropics.minigames.common.core.game.client_tweak;

import com.mojang.serialization.Codec;

public interface GameClientTweak {
	Codec<GameClientTweak> CODEC = GameClientTweakTypes.TYPE_CODEC.dispatch(
			"type",
			GameClientTweak::getType,
			GameClientTweakType::getCodec
	);

	GameClientTweakType<?> getType();
}
