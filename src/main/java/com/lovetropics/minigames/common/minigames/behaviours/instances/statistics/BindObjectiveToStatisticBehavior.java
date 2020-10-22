package com.lovetropics.minigames.common.minigames.behaviours.instances.statistics;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.minigames.statistics.StatisticKey;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ServerScoreboard;

import java.util.Map;

public final class BindObjectiveToStatisticBehavior implements IMinigameBehavior {
	private final Map<StatisticKey<Integer>, String> statisticToObjective;

	public BindObjectiveToStatisticBehavior(Map<StatisticKey<Integer>, String> statisticToObjective) {
		this.statisticToObjective = statisticToObjective;
	}

	@SuppressWarnings("unchecked")
	public static <T> BindObjectiveToStatisticBehavior parse(Dynamic<T> root) {
		Map<StatisticKey<Integer>, String> statisticToObjectives = root.get("objectives")
				.asMap(
						k -> (StatisticKey<Integer>) StatisticKey.get(k.asString("")),
						v -> v.asString("")
				);

		return new BindObjectiveToStatisticBehavior(statisticToObjectives);
	}

	@Override
	public void onFinish(IMinigameInstance minigame) {
		ServerScoreboard scoreboard = minigame.getServer().getScoreboard();

		for (Map.Entry<StatisticKey<Integer>, String> entry : statisticToObjective.entrySet()) {
			StatisticKey<Integer> key = entry.getKey();
			String objectiveKey = entry.getValue();

			ScoreObjective objective = scoreboard.getObjective(objectiveKey);
			if (objective != null) {
				applyFromObjective(minigame, key, objective);
			}
		}
	}

	private void applyFromObjective(IMinigameInstance minigame, StatisticKey<Integer> key, ScoreObjective objective) {
		MinigameStatistics statistics = minigame.getStatistics();
		ServerScoreboard scoreboard = minigame.getServer().getScoreboard();

		for (ServerPlayerEntity player : minigame.getPlayers()) {
			Map<ScoreObjective, Score> objectives = scoreboard.getObjectivesForEntity(player.getScoreboardName());
			Score score = objectives.get(objective);
			if (score != null) {
				statistics.forPlayer(player).set(key, score.getScorePoints());
			}
		}
	}
}
