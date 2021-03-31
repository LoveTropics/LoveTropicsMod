package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;

import javax.annotation.Nullable;
import java.util.Optional;

public final class TimeSurvivedTrackerBehavior implements IGameBehavior {
	public static final Codec<TimeSurvivedTrackerBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.optionalFieldOf("after_phase").forGetter(c -> Optional.ofNullable(c.afterPhase))
		).apply(instance, TimeSurvivedTrackerBehavior::new);
	});

	private final String afterPhase;

	private long startTime = -1;

	private TimeSurvivedTrackerBehavior(Optional<String> afterPhase) {
		this(afterPhase.orElse(null));
	}

	public TimeSurvivedTrackerBehavior(@Nullable String afterPhase) {
		this.afterPhase = afterPhase;
	}

	@Override
	public void register(IGameInstance registerGame, GameEventListeners events) {
		events.listen(GameLifecycleEvents.FINISH, this::onFinish);
		events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);

		if (afterPhase != null) {
			events.listen(GameLogicEvents.PHASE_CHANGE, (game, phase, lastPhase) -> {
				if (lastPhase.is(afterPhase)) {
					startTime = game.ticks();
				}
			});
		} else {
			startTime = 0;
		}
	}

	private void onFinish(IGameInstance game) {
		if (startTime == -1) {
			return;
		}

		GameStatistics statistics = game.getStatistics();

		int secondsSurvived = getSecondsSurvived(game);
		for (ServerPlayerEntity player : game.getParticipants()) {
			statistics.forPlayer(player).set(StatisticKey.TIME_SURVIVED, secondsSurvived);
		}

		statistics.getGlobal().set(StatisticKey.TOTAL_TIME, secondsSurvived);
	}

	private ActionResultType onPlayerDeath(IGameInstance minigame, ServerPlayerEntity player, DamageSource source) {
		if (startTime == -1) {
			return ActionResultType.PASS;
		}

		GameStatistics statistics = minigame.getStatistics();
		statistics.forPlayer(player).set(StatisticKey.TIME_SURVIVED, getSecondsSurvived(minigame));

		return ActionResultType.PASS;
	}

	private int getSecondsSurvived(IGameInstance minigame) {
		return (int) ((minigame.ticks() - startTime) / 20);
	}
}
