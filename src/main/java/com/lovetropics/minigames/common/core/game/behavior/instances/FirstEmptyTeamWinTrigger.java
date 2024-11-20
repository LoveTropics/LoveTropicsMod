package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GameWinner;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.serialization.MapCodec;
import org.apache.commons.lang3.mutable.MutableBoolean;

public record FirstEmptyTeamWinTrigger() implements IGameBehavior {
	public static final MapCodec<FirstEmptyTeamWinTrigger> CODEC = MapCodec.unit(FirstEmptyTeamWinTrigger::new);

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		TeamState teams = game.instanceState().getOrThrow(TeamState.KEY);
		MutableBoolean gameOver = new MutableBoolean();
		events.listen(GameLogicEvents.GAME_OVER, winner -> gameOver.setTrue());
		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> {
			if (gameOver.isTrue()) {
				return;
			}
			GameTeamKey team = teams.getTeamForPlayer(player);
			if (lastRole == PlayerRole.PARTICIPANT && team != null) {
				if (teams.getParticipantsForTeam(game, team).isEmpty()) {
					game.invoker(GameLogicEvents.GAME_OVER).onGameOver(new GameWinner.Team(teams.getTeamOrThrow(team)));
				}
			}
		});
	}
}
