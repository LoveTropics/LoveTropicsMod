package com.lovetropics.minigames.common.minigames.behaviours;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.lovetropics.minigames.common.minigames.config.BehaviorReference;

import java.util.Collection;
import java.util.List;

public final class BehaviorMap {
	private final Multimap<IMinigameBehaviorType<?>, IMinigameBehavior> behaviors;
	private final Multimap<IMinigameBehaviorType<?>, IPollingMinigameBehavior> pollingBehaviors;

	BehaviorMap(Multimap<IMinigameBehaviorType<?>, IMinigameBehavior> behaviors, Multimap<IMinigameBehaviorType<?>, IPollingMinigameBehavior> pollingBehaviors) {
		this.behaviors = behaviors;
		this.pollingBehaviors = pollingBehaviors;
	}

	public static BehaviorMap create(List<BehaviorReference> references) {
		Multimap<IMinigameBehaviorType<?>, IMinigameBehavior> behaviors = LinkedHashMultimap.create();
		Multimap<IMinigameBehaviorType<?>, IPollingMinigameBehavior> pollingBehaviors = LinkedHashMultimap.create();

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
	public <T extends IMinigameBehavior> Collection<T> getBehaviors(IMinigameBehaviorType<T> type) {
		return (Collection<T>) behaviors.get(type);
	}

	@SuppressWarnings("unchecked")
	public <T extends IPollingMinigameBehavior> Collection<T> getPollingBehaviors(IMinigameBehaviorType<T> type) {
		return (Collection<T>) pollingBehaviors.get(type);
	}
}
