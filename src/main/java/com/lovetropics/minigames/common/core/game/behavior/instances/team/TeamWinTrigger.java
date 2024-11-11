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
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.Objects;

public class TeamWinTrigger implements IGameBehavior {
	public static final MapCodec<TeamWinTrigger> CODEC = MapCodec.unit(TeamWinTrigger::new);

	private boolean winTriggered;

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		TeamState teamState = game.instanceState().getOrThrow(TeamState.KEY);

		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> {
			if (lastRole != PlayerRole.PARTICIPANT || winTriggered) {
				return;
			}

			GameTeamKey playerTeam = teamState.getTeamForPlayer(player);
			if (playerTeam == null) {
				return;
			}

			if (teamState.getParticipantsForTeam(game, playerTeam).isEmpty()) {
				GameTeam finalTeam = getFinalTeam(teamState, game);
				if (finalTeam == null) {
					if (game.participants().isEmpty()) {
						// How did we get here? If there are no other teams, the team who died last is probably the winner
						finalTeam = Objects.requireNonNull(teamState.getTeamByKey(playerTeam));
					} else {
						return;
					}
				}

				winTriggered = true;

				game.invoker(GameLogicEvents.WIN_TRIGGERED).onWinTriggered(finalTeam);
				game.invoker(GameLogicEvents.GAME_OVER).onGameOver();

				game.statistics().global().set(StatisticKey.WINNING_TEAM, finalTeam.key());
			}
		});
	}

	@Nullable
	private GameTeam getFinalTeam(TeamState teamState, IGamePhase game) {
		GameTeam finalTeam = null;
		for (GameTeam team : teamState) {
			if (teamState.getParticipantsForTeam(game, team.key()).isEmpty()) {
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
