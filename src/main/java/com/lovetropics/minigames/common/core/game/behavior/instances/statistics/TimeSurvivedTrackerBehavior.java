package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.statistics.StatisticsMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;

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
	public void register(IGameInstance registerGame, GameEventListeners events) {
		events.listen(GameLifecycleEvents.TICK, this::tick);
		events.listen(GameLifecycleEvents.FINISH, this::onFinish);
		events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);
	}

	private void tick(IGameInstance game) {
		if (startTime == 0 && afterPhase != null) {
			game.getOneBehavior(GameBehaviorTypes.PHASES.get()).ifPresent(phases -> {
				if (!phases.getCurrentPhase().is(afterPhase)) {
					startTime = game.ticks();
				}
			});
		}
	}

	private void onFinish(IGameInstance game) {
		GameStatistics statistics = game.getStatistics();

		int secondsSurvived = getSecondsSurvived(game);
		for (ServerPlayerEntity player : game.getParticipants()) {
			statistics.forPlayer(player).set(StatisticKey.TIME_SURVIVED, secondsSurvived);
		}

		statistics.getGlobal().set(StatisticKey.TOTAL_TIME, secondsSurvived);
	}

	private ActionResultType onPlayerDeath(IGameInstance minigame, ServerPlayerEntity player, DamageSource source) {
		GameStatistics statistics = minigame.getStatistics();
		StatisticsMap playerStatistics = statistics.forPlayer(player);

		playerStatistics.set(StatisticKey.TIME_SURVIVED, getSecondsSurvived(minigame));

		return ActionResultType.PASS;
	}

	private int getSecondsSurvived(IGameInstance minigame) {
		return (int) ((minigame.ticks() - startTime) / 20);
	}
}
