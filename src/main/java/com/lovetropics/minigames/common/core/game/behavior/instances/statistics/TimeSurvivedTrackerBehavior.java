package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.*;
import com.lovetropics.minigames.common.core.game.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;

public final class TimeSurvivedTrackerBehavior implements IGameBehavior {
	public static final Codec<TimeSurvivedTrackerBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				TriggerAfterConfig.CODEC.optionalFieldOf("trigger", TriggerAfterConfig.EMPTY).forGetter(c -> c.trigger)
		).apply(instance, TimeSurvivedTrackerBehavior::new);
	});

	private final TriggerAfterConfig trigger;

	private long startTime = -1;

	public TimeSurvivedTrackerBehavior(TriggerAfterConfig trigger) {
		this.trigger = trigger;
	}

	@Override
	public void register(IGameInstance game, EventRegistrar events) {
		trigger.awaitThen(events, () -> {
			startTime = game.ticks();

			events.listen(GameLifecycleEvents.STOP, this::onFinish);
			events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);
		});
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
		statistics.forPlayer(player).set(StatisticKey.TIME_SURVIVED, getSecondsSurvived(minigame));

		return ActionResultType.PASS;
	}

	private int getSecondsSurvived(IGameInstance minigame) {
		return (int) ((minigame.ticks() - startTime) / 20);
	}
}
