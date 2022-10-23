package com.lovetropics.minigames.common.core.game.state;

import com.mojang.serialization.Codec;

// TODO: confusing duplicate naming
public record GamePhase(String key) {
	public static final Codec<GamePhase> CODEC = Codec.STRING.xmap(GamePhase::new, GamePhase::key);

	public boolean is(String phase) {
		return this.key.equals(phase);
	}
}
