package com.lovetropics.minigames.common.minigames.config;

import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.behaviours.IPollingMinigameBehavior;
import com.mojang.serialization.Dynamic;

import java.util.List;
import java.util.function.BiConsumer;

public abstract class BehaviorReference {
	BehaviorReference() {
	}

	public abstract void addTo(BiConsumer<IMinigameBehaviorType<?>, IMinigameBehavior> addActive, BiConsumer<IMinigameBehaviorType<?>, IPollingMinigameBehavior> addPolling);

	static final class Static extends BehaviorReference {
		private final IMinigameBehaviorType<?> type;
		private final Dynamic<?> config;

		Static(IMinigameBehaviorType<?> type, Dynamic<?> config) {
			this.type = type;
			this.config = config;
		}

		@Override
		public void addTo(BiConsumer<IMinigameBehaviorType<?>, IMinigameBehavior> addActive, BiConsumer<IMinigameBehaviorType<?>, IPollingMinigameBehavior> addPolling) {
			Object behavior = type.create(config);
			if (behavior instanceof IMinigameBehavior) {
				addActive.accept(type, (IMinigameBehavior) behavior);
			}
			if (behavior instanceof IPollingMinigameBehavior) {
				addPolling.accept(type, (IPollingMinigameBehavior) behavior);
			}
		}
	}

	static final class Set extends BehaviorReference {
		private final List<BehaviorReference> behaviors;

		Set(List<BehaviorReference> behaviors) {
			this.behaviors = behaviors;
		}

		@Override
		public void addTo(BiConsumer<IMinigameBehaviorType<?>, IMinigameBehavior> addActive, BiConsumer<IMinigameBehaviorType<?>, IPollingMinigameBehavior> addPolling) {
			for (BehaviorReference behavior : behaviors) {
				behavior.addTo(addActive, addPolling);
			}
		}
	}
}
