package com.lovetropics.minigames.common.minigames.behaviours;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.lovetropics.minigames.common.minigames.config.BehaviorReference;

import java.util.Collection;
import java.util.List;

public final class BehaviorMap {
	private final Multimap<MinigameBehaviorType<?>, IMinigameBehavior> behaviors;
	private final Multimap<MinigameBehaviorType<?>, IPollingMinigameBehavior> pollingBehaviors;

	BehaviorMap(Multimap<MinigameBehaviorType<?>, IMinigameBehavior> behaviors, Multimap<MinigameBehaviorType<?>, IPollingMinigameBehavior> pollingBehaviors) {
		this.behaviors = behaviors;
		this.pollingBehaviors = pollingBehaviors;
	}

	public static BehaviorMap create(List<BehaviorReference> references) {
		Multimap<MinigameBehaviorType<?>, IMinigameBehavior> behaviors = LinkedHashMultimap.create();
		Multimap<MinigameBehaviorType<?>, IPollingMinigameBehavior> pollingBehaviors = LinkedHashMultimap.create();

		for (BehaviorReference reference : references) {
			reference.addTo(behaviors::put, pollingBehaviors::put);
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
	public <T extends IMinigameBehavior> Collection<T> getBehaviors(MinigameBehaviorType<T> type) {
		return (Collection<T>) behaviors.get(type);
	}

	@SuppressWarnings("unchecked")
	public <T extends IPollingMinigameBehavior> Collection<T> getPollingBehaviors(MinigameBehaviorType<T> type) {
		return (Collection<T>) pollingBehaviors.get(type);
	}
}
