package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.ProgressionPoint;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public record TriggerAfterConfig(Optional<ProgressionPoint> after) {
	public static final MapCodec<TriggerAfterConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ProgressionPoint.CODEC.optionalFieldOf("after").forGetter(c -> c.after)
	).apply(i, TriggerAfterConfig::new));

	public static final Codec<TriggerAfterConfig> CODEC = MAP_CODEC.codec();

	public static final TriggerAfterConfig EMPTY = new TriggerAfterConfig(Optional.empty());

	public TriggerAfterConfig(ProgressionPoint after) {
		this(Optional.of(after));
	}

	public void awaitThen(IGamePhase game, EventRegistrar events, Runnable handler) {
		if (after.isPresent()) {
			BooleanSupplier predicate = after.get().createPredicate(game);

			MutableObject<GamePhaseEvents.Tick> listener = new MutableObject<>();
			listener.setValue(() -> {
				if (predicate.getAsBoolean()) {
					handler.run();
					events.unlisten(GamePhaseEvents.TICK, listener.getValue());
				}
			});
			events.listen(GamePhaseEvents.TICK, listener.getValue());
		} else {
			handler.run();
		}
	}
}
