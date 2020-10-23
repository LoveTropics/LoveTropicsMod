package com.lovetropics.minigames.common.minigames.behaviours;

import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BehaviorMap {
	private final Map<IMinigameBehaviorType<?>, IMinigameBehavior> behaviors;
	private final Map<IMinigameBehaviorType<?>, IPollingMinigameBehavior> pollingBehaviors;

	BehaviorMap(Map<IMinigameBehaviorType<?>, IMinigameBehavior> behaviors, Map<IMinigameBehaviorType<?>, IPollingMinigameBehavior> pollingBehaviors) {
		this.behaviors = behaviors;
		this.pollingBehaviors = pollingBehaviors;
	}

	public static BehaviorMap create(List<ConfiguredBehavior<?>> configs) {
		Map<IMinigameBehaviorType<?>, IMinigameBehavior> behaviors = new Reference2ObjectLinkedOpenHashMap<>();
		Map<IMinigameBehaviorType<?>, IPollingMinigameBehavior> pollingBehaviors = new Reference2ObjectLinkedOpenHashMap<>();

		for (ConfiguredBehavior<?> config : configs) {
			Object behavior = config.create();
			if (behavior instanceof IMinigameBehavior) {
				behaviors.put(config.type, (IMinigameBehavior) behavior);
			}

			if (behavior instanceof IPollingMinigameBehavior) {
				pollingBehaviors.put(config.type, (IPollingMinigameBehavior) behavior);
			}
		}

		return new BehaviorMap(behaviors, pollingBehaviors);
	}

	public Collection<IMinigameBehavior> getBehaviors() {
		return behaviors.values();
	}

	public Collection<IPollingMinigameBehavior> getPollingBehaviors() {
		return pollingBehaviors.values();
	}

	@SuppressWarnings("unchecked")
	public <T extends IMinigameBehavior> Optional<T> getBehavior(IMinigameBehaviorType<T> type) {
		return Optional.ofNullable((T) behaviors.get(type));
	}

	@SuppressWarnings("unchecked")
	public <T extends IPollingMinigameBehavior> Optional<T> getPollingBehavior(IMinigameBehaviorType<T> type) {
		return Optional.ofNullable((T) pollingBehaviors.get(type));
	}
}
