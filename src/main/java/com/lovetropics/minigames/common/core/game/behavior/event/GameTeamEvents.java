package com.lovetropics.minigames.common.core.game.behavior.event;

import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import net.minecraft.server.level.ServerPlayer;

public final class GameTeamEvents {
	public static final GameEventType<SetGameTeam> SET_GAME_TEAM = GameEventType.create(SetGameTeam.class, listeners -> (player, teams, team) -> {
		for (SetGameTeam listener : listeners) {
			listener.onSetGameTeam(player, teams, team);
		}
	});

	private GameTeamEvents() {
	}

	public interface SetGameTeam {
		void onSetGameTeam(ServerPlayer player, TeamState teams, GameTeamKey team);
	}
}
