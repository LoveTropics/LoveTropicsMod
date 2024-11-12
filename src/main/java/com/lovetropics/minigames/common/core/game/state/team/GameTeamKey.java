package com.lovetropics.minigames.common.core.game.state.team;

import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.state.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticHolder;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticsMap;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

public record GameTeamKey(String id) implements StatisticHolder {
	public static final Codec<GameTeamKey> CODEC = Codec.STRING.xmap(GameTeamKey::new, GameTeamKey::id);

	@Override
	public String toString() {
		return id;
	}

	@Override
	public Component getName(IGamePhase game) {
		TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
		GameTeam team = teams != null ? teams.getTeamByKey(this) : null;
		return team != null ? team.config().styledName() : MinigameTexts.UNKNOWN;
	}

	@Override
	public StatisticsMap getOwnStatistics(GameStatistics statistics) {
		return statistics.forTeam(this);
	}
}
