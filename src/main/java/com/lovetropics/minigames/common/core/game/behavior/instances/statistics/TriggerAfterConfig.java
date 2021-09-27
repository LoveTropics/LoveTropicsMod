package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import javax.annotation.Nullable;
import java.util.Optional;

public final class TriggerAfterConfig {
	public static final Codec<TriggerAfterConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.optionalFieldOf("after_phase").forGetter(c -> Optional.ofNullable(c.afterPhase))
		).apply(instance, TriggerAfterConfig::new);
	});

	public static final TriggerAfterConfig EMPTY = new TriggerAfterConfig((String) null);

	public final @Nullable String afterPhase;

	private TriggerAfterConfig(Optional<String> afterPhase) {
		this(afterPhase.orElse(null));
	}

	public TriggerAfterConfig(String afterPhase) {
		this.afterPhase = afterPhase;
	}

	public void awaitThen(EventRegistrar events, Runnable handler) {
		if (afterPhase != null) {
			events.listen(GameLogicEvents.PHASE_CHANGE, (phase, lastPhase) -> {
				if (lastPhase.is(afterPhase)) {
					handler.run();
				}
			});
		} else {
			handler.run();
		}
	}
}
