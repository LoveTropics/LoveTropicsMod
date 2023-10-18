package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;

public final class TimeSurvivedTrackerBehavior implements IGameBehavior {
	public static final MapCodec<TimeSurvivedTrackerBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			TriggerAfterConfig.CODEC.optionalFieldOf("trigger", TriggerAfterConfig.EMPTY).forGetter(c -> c.trigger)
	).apply(i, TimeSurvivedTrackerBehavior::new));

	private final TriggerAfterConfig trigger;

	private long startTime = -1;

	public TimeSurvivedTrackerBehavior(TriggerAfterConfig trigger) {
		this.trigger = trigger;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		trigger.awaitThen(game, events, () -> {
			startTime = game.ticks();

			events.listen(GamePhaseEvents.FINISH, () -> onFinish(game));
			events.listen(GamePlayerEvents.DEATH, (player, source) -> onPlayerDeath(game, player, source));
		});
	}

	private void onFinish(IGamePhase game) {
		GameStatistics statistics = game.getStatistics();

		int secondsSurvived = getSecondsSurvived(game);
		for (ServerPlayer player : game.getParticipants()) {
			statistics.forPlayer(player).set(StatisticKey.TIME_SURVIVED, secondsSurvived);
		}

		statistics.global().set(StatisticKey.TOTAL_TIME, secondsSurvived);
	}

	private InteractionResult onPlayerDeath(IGamePhase game, ServerPlayer player, DamageSource source) {
		GameStatistics statistics = game.getStatistics();
		statistics.forPlayer(player).set(StatisticKey.TIME_SURVIVED, getSecondsSurvived(game));

		return InteractionResult.PASS;
	}

	private int getSecondsSurvived(IGamePhase game) {
		return (int) ((game.ticks() - startTime) / 20);
	}
}
