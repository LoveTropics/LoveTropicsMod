package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.GameWinner;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.Placement;
import com.lovetropics.minigames.common.core.game.state.statistics.PlacementOrder;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.function.Supplier;

public record PlaceByStatisticBehavior(StatisticKey<Integer> statistic, PlacementOrder order, boolean triggerWin) implements IGameBehavior {
	public static final MapCodec<PlaceByStatisticBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			StatisticKey.INT_CODEC.fieldOf("statistic").forGetter(c -> c.statistic),
			PlacementOrder.CODEC.optionalFieldOf("order", PlacementOrder.MAX).forGetter(c -> c.order),
			Codec.BOOL.optionalFieldOf("trigger_win", true).forGetter(c -> c.triggerWin)
	).apply(i, PlaceByStatisticBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		if (triggerWin) {
			events.listen(GameLogicEvents.REQUEST_GAME_OVER, () -> {
				GameWinner winner = runPlacement(game);
				game.invoker(GameLogicEvents.GAME_OVER).onGameOver(winner);
				return true;
			});
		}

		// Just to make sure that the placement statistics are there, even if game_over was never triggered
		events.listen(GamePhaseEvents.FINISH, () -> runPlacement(game));
	}

	private GameWinner runPlacement(IGamePhase game) {
		Placement.Score<PlayerKey, Integer> playerPlacement = Placement.fromPlayerScore(order, game, statistic);
		playerPlacement.placeInto(StatisticKey.PLACEMENT);

		Placement.Score<GameTeamKey, Integer> teamPlacement = Placement.fromTeamScore(order, game, statistic, 0);
		teamPlacement.placeInto(StatisticKey.PLACEMENT);

		PlayerKey winningPlayerKey = playerPlacement.getWinner();
		GameTeamKey winningTeamKey = teamPlacement.getWinner();

		TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
		GameTeam winningTeam = teams != null && winningTeamKey != null ? teams.getTeamByKey(winningTeamKey) : null;
		if (winningTeam != null) {
			return new GameWinner.Team(winningTeam);
		} else if (winningPlayerKey != null) {
			return GameWinner.byPlayerKey(game, winningPlayerKey);
		}
		return new GameWinner.Nobody();
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return GameBehaviorTypes.PLACE_BY_STATISTIC;
	}
}
