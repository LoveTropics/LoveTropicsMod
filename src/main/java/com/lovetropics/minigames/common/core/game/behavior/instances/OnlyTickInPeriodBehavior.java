package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.ProgressionPeriod;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public record OnlyTickInPeriodBehavior(ProgressionPeriod period, IGameBehavior behavior) implements IGameBehavior {
	public static final MapCodec<OnlyTickInPeriodBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ProgressionPeriod.CODEC.fieldOf("period").forGetter(OnlyTickInPeriodBehavior::period),
			IGameBehavior.CODEC.fieldOf("behavior").forGetter(OnlyTickInPeriodBehavior::behavior)
	).apply(i, OnlyTickInPeriodBehavior::new));

	@Override
	public void registerState(final IGamePhase game, final GameStateMap phaseState, final GameStateMap instanceState) {
		behavior.registerState(game, phaseState, instanceState);
	}

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		final GameEventListeners tickEvents = new GameEventListeners();
		behavior.register(game, events.redirect(type -> type == GamePhaseEvents.TICK || type == GamePlayerEvents.TICK, tickEvents));

		final BooleanSupplier predicate = period.createPredicate(game);
		events.listen(GamePhaseEvents.TICK, () -> {
			if (predicate.getAsBoolean()) {
				tickEvents.invoker(GamePhaseEvents.TICK).tick();
			}
		});

		events.listen(GamePlayerEvents.TICK, player -> {
			if (predicate.getAsBoolean()) {
				tickEvents.invoker(GamePlayerEvents.TICK).tick(player);
			}
		});
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return GameBehaviorTypes.ONLY_TICK_IN_PERIOD;
	}
}
