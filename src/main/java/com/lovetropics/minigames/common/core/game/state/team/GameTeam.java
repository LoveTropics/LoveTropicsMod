package com.lovetropics.minigames.common.core.game.state.team;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GameTeam(GameTeamKey key, GameTeamConfig config) {
	public static final Codec<GameTeam> CODEC = RecordCodecBuilder.create(i -> i.group(
			GameTeamKey.CODEC.fieldOf("key").forGetter(GameTeam::key),
			GameTeamConfig.MAP_CODEC.forGetter(GameTeam::config)
	).apply(i, GameTeam::new));
}
