package com.lovetropics.minigames.common.core.game.behavior.instances.team;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.team.TeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;

public class TeamWinTrigger implements IGameBehavior {
	public static final Codec<TeamWinTrigger> CODEC = Codec.unit(TeamWinTrigger::new);

	private boolean winTriggered;

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		TeamState teamState = game.getState().getOrThrow(TeamState.KEY);

		events.listen(GamePlayerEvents.DEATH, (player, damageSource) -> {
			if (winTriggered) {
				return ActionResultType.PASS;
			}

			TeamKey playerTeam = teamState.getTeamForPlayer(player);
			if (teamState.getPlayersForTeam(playerTeam).isEmpty()) {
				TeamKey finalTeam = getFinalTeam(teamState);
				if (finalTeam != null) {
					winTriggered = true;

					ITextComponent winnerName = new StringTextComponent(finalTeam.name).mergeStyle(finalTeam.text);
					game.invoker(GameLogicEvents.WIN_TRIGGERED).onWinTriggered(winnerName);
					game.invoker(GameLogicEvents.GAME_OVER).onGameOver();

					game.getStatistics().global().set(StatisticKey.WINNING_TEAM, finalTeam);
				}
			}

			return ActionResultType.PASS;
		});
	}

	@Nullable
	private TeamKey getFinalTeam(TeamState teamState) {
		TeamKey finalTeam = null;
		for (TeamKey team : teamState.getTeams()) {
			if (teamState.getPlayersForTeam(team).isEmpty()) {
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
