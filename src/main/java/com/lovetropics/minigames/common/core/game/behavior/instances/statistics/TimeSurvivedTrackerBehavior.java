package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.statistics.StatisticsMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.Optional;

public final class TimeSurvivedTrackerBehavior implements IGameBehavior {
	public static final Codec<TimeSurvivedTrackerBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.optionalFieldOf("after_phase").forGetter(c -> Optional.ofNullable( c.afterPhase))
		).apply(instance, TimeSurvivedTrackerBehavior::new);
	});

	private final String afterPhase;
	private long startTime;

	public TimeSurvivedTrackerBehavior(Optional<String> afterPhase) {
		this.afterPhase = afterPhase.orElse(null);
	}

	@Override
	public void worldUpdate(IGameInstance minigame, ServerWorld world) {
		if (startTime == 0 && afterPhase != null) {
			minigame.getOneBehavior(GameBehaviorTypes.PHASES.get()).ifPresent(phases -> {
				if (!phases.getCurrentPhase().is(afterPhase)) {
					startTime = minigame.ticks();
				}
			});
		}
	}

	@Override
	public void onFinish(IGameInstance minigame) {
		MinigameStatistics statistics = minigame.getStatistics();

		int secondsSurvived = getSecondsSurvived(minigame);
		for (ServerPlayerEntity player : minigame.getParticipants()) {
			statistics.forPlayer(player).set(StatisticKey.TIME_SURVIVED, secondsSurvived);
		}

		statistics.getGlobal().set(StatisticKey.TOTAL_TIME, secondsSurvived);
	}

	@Override
	public void onPlayerDeath(IGameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		MinigameStatistics statistics = minigame.getStatistics();
		StatisticsMap playerStatistics = statistics.forPlayer(player);

		playerStatistics.set(StatisticKey.TIME_SURVIVED, getSecondsSurvived(minigame));
	}

	private int getSecondsSurvived(IGameInstance minigame) {
		return (int) ((minigame.ticks() - startTime) / 20);
	}
}
