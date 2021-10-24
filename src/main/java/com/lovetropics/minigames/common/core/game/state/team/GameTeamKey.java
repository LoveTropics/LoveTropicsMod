package com.lovetropics.minigames.common.core.game.state.team;

import com.mojang.serialization.Codec;

public final class GameTeamKey {
	public static final Codec<GameTeamKey> CODEC = Codec.STRING.xmap(GameTeamKey::new, GameTeamKey::id);

	private final String id;

	public GameTeamKey(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;

		if (obj instanceof GameTeamKey) {
			GameTeamKey team = (GameTeamKey) obj;
			return id.equals(team.id);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
