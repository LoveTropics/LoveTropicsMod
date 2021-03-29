package com.lovetropics.minigames.common.core.game.behavior;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.lovetropics.minigames.common.core.game.config.BehaviorReference;

import java.util.Collection;
import java.util.List;

public final class BehaviorMap {
	private final Multimap<GameBehaviorType<?>, IGameBehavior> behaviors;
	private final Multimap<GameBehaviorType<?>, IPollingMinigameBehavior> pollingBehaviors;

	BehaviorMap(Multimap<GameBehaviorType<?>, IGameBehavior> behaviors, Multimap<GameBehaviorType<?>, IPollingMinigameBehavior> pollingBehaviors) {
		this.behaviors = behaviors;
		this.pollingBehaviors = pollingBehaviors;
	}

	public static BehaviorMap create(List<BehaviorReference> references) {
		Multimap<GameBehaviorType<?>, IGameBehavior> behaviors = LinkedHashMultimap.create();
		Multimap<GameBehaviorType<?>, IPollingMinigameBehavior> pollingBehaviors = LinkedHashMultimap.create();

		for (BehaviorReference reference : references) {
			reference.addTo(behaviors::put, pollingBehaviors::put);
		}

		return new BehaviorMap(behaviors, pollingBehaviors);
	}

	public Collection<IGameBehavior> getBehaviors() {
		return behaviors.values();
	}

	public Collection<IPollingMinigameBehavior> getPollingBehaviors() {
		return pollingBehaviors.values();
	}

	@SuppressWarnings("unchecked")
	public <T extends IGameBehavior> Collection<T> getBehaviors(GameBehaviorType<T> type) {
		return (Collection<T>) behaviors.get(type);
	}

	@SuppressWarnings("unchecked")
	public <T extends IPollingMinigameBehavior> Collection<T> getPollingBehaviors(GameBehaviorType<T> type) {
		return (Collection<T>) pollingBehaviors.get(type);
	}
}
