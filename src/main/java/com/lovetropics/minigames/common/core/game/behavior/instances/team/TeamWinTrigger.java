package com.lovetropics.minigames.common.core.game.behavior.instances.team;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class TeamWinTrigger implements IGameBehavior {
	public static final Codec<TeamWinTrigger> CODEC = Codec.unit(TeamWinTrigger::new);

	private boolean winTriggered;

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		TeamState teamState = game.getInstanceState().getOrThrow(TeamState.KEY);

		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> {
			if (lastRole != PlayerRole.PARTICIPANT || winTriggered) {
				return;
			}

			GameTeamKey playerTeam = teamState.getTeamForPlayer(player);
			if (teamState.getPlayersForTeam(playerTeam).isEmpty()) {
				GameTeam finalTeam = getFinalTeam(teamState);
				if (finalTeam != null) {
					winTriggered = true;

					Component winnerName = finalTeam.config().name().copy()
							.withStyle(finalTeam.config().formatting());
					game.invoker(GameLogicEvents.WIN_TRIGGERED).onWinTriggered(winnerName);
					game.invoker(GameLogicEvents.GAME_OVER).onGameOver();

					game.getStatistics().global().set(StatisticKey.WINNING_TEAM, finalTeam.key());
				}
			}
		});
	}

	@Nullable
	private GameTeam getFinalTeam(TeamState teamState) {
		GameTeam finalTeam = null;
		for (GameTeam team : teamState) {
			if (teamState.getPlayersForTeam(team.key()).isEmpty()) {
				continue;
			}

			if (finalTeam != null) {
				return null;
			} else {
				finalTeam = team;
			}
		}

		return finalTeam;
	}
}
