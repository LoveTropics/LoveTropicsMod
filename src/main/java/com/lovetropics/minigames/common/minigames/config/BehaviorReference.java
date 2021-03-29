package com.lovetropics.minigames.common.minigames.config;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.behaviours.IPollingMinigameBehavior;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;

import java.util.List;
import java.util.function.BiConsumer;

public abstract class BehaviorReference {
	BehaviorReference() {
	}

	public abstract void addTo(BiConsumer<MinigameBehaviorType<?>, IMinigameBehavior> addActive, BiConsumer<MinigameBehaviorType<?>, IPollingMinigameBehavior> addPolling);

	static final class Static extends BehaviorReference {
		private final MinigameBehaviorType<?> type;
		private final Dynamic<?> config;

		Static(MinigameBehaviorType<?> type, Dynamic<?> config) {
			this.type = type;
			this.config = config;
		}

		@Override
		public void addTo(BiConsumer<MinigameBehaviorType<?>, IMinigameBehavior> addActive, BiConsumer<MinigameBehaviorType<?>, IPollingMinigameBehavior> addPolling) {
			DataResult<?> result = type.codec.parse(config);

			result.result().ifPresent(behavior -> {
				if (behavior instanceof IMinigameBehavior) {
					addActive.accept(type, (IMinigameBehavior) behavior);
				}
				if (behavior instanceof IPollingMinigameBehavior) {
					addPolling.accept(type, (IPollingMinigameBehavior) behavior);
				}
			});

			result.error().ifPresent(error -> {
				LoveTropics.LOGGER.warn("Failed to parse behavior declaration of type {}: {}", type, error);
			});
		}
	}

	static final class Set extends BehaviorReference {
		private final List<BehaviorReference> behaviors;

		Set(List<BehaviorReference> behaviors) {
			this.behaviors = behaviors;
		}

		@Override
		public void addTo(BiConsumer<MinigameBehaviorType<?>, IMinigameBehavior> addActive, BiConsumer<MinigameBehaviorType<?>, IPollingMinigameBehavior> addPolling) {
			for (BehaviorReference behavior : behaviors) {
				behavior.addTo(addActive, addPolling);
			}
		}
	}
}
