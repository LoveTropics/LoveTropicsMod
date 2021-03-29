package com.lovetropics.minigames.common.minigames.behaviours.instances.statistics;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.lovetropics.minigames.common.minigames.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.minigames.statistics.StatisticKey;
import com.lovetropics.minigames.common.minigames.statistics.StatisticsMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.Optional;

public final class TimeSurvivedTrackerBehavior implements IMinigameBehavior {
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
	public void worldUpdate(IMinigameInstance minigame, ServerWorld world) {
		if (startTime == 0 && afterPhase != null) {
			minigame.getOneBehavior(MinigameBehaviorTypes.PHASES.get()).ifPresent(phases -> {
				if (!phases.getCurrentPhase().is(afterPhase)) {
					startTime = minigame.ticks();
				}
			});
		}
	}

	@Override
	public void onFinish(IMinigameInstance minigame) {
		MinigameStatistics statistics = minigame.getStatistics();

		int secondsSurvived = getSecondsSurvived(minigame);
		for (ServerPlayerEntity player : minigame.getParticipants()) {
			statistics.forPlayer(player).set(StatisticKey.TIME_SURVIVED, secondsSurvived);
		}

		statistics.getGlobal().set(StatisticKey.TOTAL_TIME, secondsSurvived);
	}

	@Override
	public void onPlayerDeath(IMinigameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		MinigameStatistics statistics = minigame.getStatistics();
		StatisticsMap playerStatistics = statistics.forPlayer(player);

		playerStatistics.set(StatisticKey.TIME_SURVIVED, getSecondsSurvived(minigame));
	}

	private int getSecondsSurvived(IMinigameInstance minigame) {
		return (int) ((minigame.ticks() - startTime) / 20);
	}
}
