package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.Optional;

public class TeamWinTrigger implements IGameBehavior {
	public static final Codec<TeamWinTrigger> CODEC = Codec.unit(TeamWinTrigger::new);

	private boolean winTriggered;

	@Override
	public void register(IGameInstance registerGame, GameEventListeners events) throws GameException {
		events.listen(GamePlayerEvents.DEATH, (game, player, damageSource) -> {
			if (winTriggered) {
				return ActionResultType.PASS;
			}

			// TODO: use state variable
			Optional<TeamsBehavior> teamsBehaviorOpt = game.getBehaviors().getOne(GameBehaviorTypes.TEAMS.get());
			if (!teamsBehaviorOpt.isPresent()) {
				return ActionResultType.PASS;
			}

			TeamsBehavior teamsBehavior = teamsBehaviorOpt.get();

			TeamsBehavior.TeamKey playerTeam = teamsBehavior.getTeamForPlayer(player);
			if (teamsBehavior.getPlayersForTeam(playerTeam).isEmpty()) {
				TeamsBehavior.TeamKey lastTeam = getLastTeam(teamsBehavior);
				if (lastTeam != null) {
					winTriggered = true;

					ITextComponent winnerName = new StringTextComponent(lastTeam.name).mergeStyle(lastTeam.text);
					game.invoker(GameLogicEvents.WIN_TRIGGERED).onWinTriggered(game, winnerName);
					game.invoker(GameLogicEvents.GAME_OVER).onGameOver(game);

					game.getStatistics().getGlobal().set(StatisticKey.WINNING_TEAM, lastTeam);
				}
			}

			return ActionResultType.PASS;
		});
	}

	@Nullable
	private TeamsBehavior.TeamKey getLastTeam(TeamsBehavior teamBehavior) {
		TeamsBehavior.TeamKey lastTeam = null;
		for (TeamsBehavior.TeamKey team : teamBehavior.getTeams()) {
			if (teamBehavior.getPlayersForTeam(team).isEmpty()) {
				continue;
			}

			if (lastTeam != null) {
				return null;
			} else {
				lastTeam = team;
			}
		}

		return lastTeam;
	}
}
