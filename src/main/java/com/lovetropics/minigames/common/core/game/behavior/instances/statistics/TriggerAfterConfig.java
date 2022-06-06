package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record TriggerAfterConfig(Optional<String> afterPhase) {
	public static final Codec<TriggerAfterConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.optionalFieldOf("after_phase").forGetter(c -> c.afterPhase)
	).apply(i, TriggerAfterConfig::new));

	public static final TriggerAfterConfig EMPTY = new TriggerAfterConfig(Optional.empty());

	public TriggerAfterConfig(String afterPhase) {
		this(Optional.of(afterPhase));
	}

	public void awaitThen(EventRegistrar events, Runnable handler) {
		if (afterPhase.isPresent()) {
			events.listen(GameLogicEvents.PHASE_CHANGE, (phase, lastPhase) -> {
				if (lastPhase.is(afterPhase.get())) {
					handler.run();
				}
			});
		} else {
			handler.run();
		}
	}
}
