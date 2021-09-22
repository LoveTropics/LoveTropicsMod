package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ServerScoreboard;

import java.util.Map;

public final class BindObjectiveToStatisticBehavior implements IGameBehavior {
	public static final Codec<BindObjectiveToStatisticBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.unboundedMap(StatisticKey.codecFor(Integer.class), Codec.STRING).fieldOf("objectives").forGetter(c -> c.statisticToObjective)
		).apply(instance, BindObjectiveToStatisticBehavior::new);
	});

	private final Map<StatisticKey<Integer>, String> statisticToObjective;

	public BindObjectiveToStatisticBehavior(Map<StatisticKey<Integer>, String> statisticToObjective) {
		this.statisticToObjective = statisticToObjective;
	}

	@Override
	public void register(IActiveGame registerGame, EventRegistrar events) {
		events.listen(GameLifecycleEvents.STOP, (game, reason) -> {
			ServerScoreboard scoreboard = game.getServer().getScoreboard();

			for (Map.Entry<StatisticKey<Integer>, String> entry : statisticToObjective.entrySet()) {
				StatisticKey<Integer> key = entry.getKey();
				String objectiveKey = entry.getValue();

				ScoreObjective objective = scoreboard.getObjective(objectiveKey);
				if (objective != null) {
					applyFromObjective(game, key, objective);
				}
			}
		});
	}

	private void applyFromObjective(IActiveGame game, StatisticKey<Integer> key, ScoreObjective objective) {
		GameStatistics statistics = game.getStatistics();
		ServerScoreboard scoreboard = game.getServer().getScoreboard();

		for (ServerPlayerEntity player : game.getAllPlayers()) {
			Map<ScoreObjective, Score> objectives = scoreboard.getObjectivesForEntity(player.getScoreboardName());
			Score score = objectives.get(objective);
			if (score != null) {
				statistics.forPlayer(player).set(key, score.getScorePoints());
			}
		}
	}
}
