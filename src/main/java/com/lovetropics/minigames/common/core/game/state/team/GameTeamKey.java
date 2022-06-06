package com.lovetropics.minigames.common.core.game.state.team;

import com.mojang.serialization.Codec;

public record GameTeamKey(String id) {
	public static final Codec<GameTeamKey> CODEC = Codec.STRING.xmap(GameTeamKey::new, GameTeamKey::id);

	@Override
	public String toString() {
		return id;
	}
}
