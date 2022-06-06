package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Objective;
import net.minecraft.server.ServerScoreboard;

import java.util.Map;

public record BindObjectiveToStatisticBehavior(Map<StatisticKey<Integer>, String> statisticToObjective) implements IGameBehavior {
	public static final Codec<BindObjectiveToStatisticBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.unboundedMap(StatisticKey.codecFor(Integer.class), Codec.STRING).fieldOf("objectives").forGetter(c -> c.statisticToObjective)
	).apply(i, BindObjectiveToStatisticBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePhaseEvents.STOP, reason -> {
			ServerScoreboard scoreboard = game.getServer().getScoreboard();

			for (Map.Entry<StatisticKey<Integer>, String> entry : statisticToObjective.entrySet()) {
				StatisticKey<Integer> key = entry.getKey();
				String objectiveKey = entry.getValue();

				Objective objective = scoreboard.getOrCreateObjective(objectiveKey);
				if (objective != null) {
					applyFromObjective(game, key, objective);
				}
			}
		});
	}

	private void applyFromObjective(IGamePhase game, StatisticKey<Integer> key, Objective objective) {
		GameStatistics statistics = game.getStatistics();
		ServerScoreboard scoreboard = game.getServer().getScoreboard();

		for (ServerPlayer player : game.getAllPlayers()) {
			Map<Objective, Score> objectives = scoreboard.getPlayerScores(player.getScoreboardName());
			Score score = objectives.get(objective);
			if (score != null) {
				statistics.forPlayer(player).set(key, score.getScore());
			}
		}
	}
}
