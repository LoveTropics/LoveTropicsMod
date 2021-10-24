package com.lovetropics.minigames.common.core.game.state.team;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class GameTeam {
	public static final Codec<GameTeam> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				GameTeamKey.CODEC.fieldOf("key").forGetter(GameTeam::key),
				GameTeamConfig.MAP_CODEC.forGetter(GameTeam::config)
		).apply(instance, GameTeam::new);
	});

	private final GameTeamKey key;
	private final GameTeamConfig config;

	public GameTeam(GameTeamKey key, GameTeamConfig config) {
		this.key = key;
		this.config = config;
	}

	public GameTeamKey key() {
		return key;
	}

	public GameTeamConfig config() {
		return config;
	}
}
