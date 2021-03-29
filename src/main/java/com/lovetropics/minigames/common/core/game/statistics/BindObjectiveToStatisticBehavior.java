package com.lovetropics.minigames.common.core.game.statistics;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
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
	public void onFinish(IGameInstance minigame) {
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

	private void applyFromObjective(IGameInstance minigame, StatisticKey<Integer> key, ScoreObjective objective) {
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
