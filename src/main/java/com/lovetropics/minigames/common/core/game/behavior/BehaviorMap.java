package com.lovetropics.minigames.common.core.game.behavior;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.lovetropics.minigames.common.core.game.config.BehaviorReference;

import java.util.Collection;
import java.util.List;

public final class BehaviorMap {
	private final Multimap<GameBehaviorType<?>, IGameBehavior> behaviors;

	BehaviorMap(Multimap<GameBehaviorType<?>, IGameBehavior> behaviors) {
		this.behaviors = behaviors;
	}

	public static BehaviorMap create(List<BehaviorReference> references) {
		Multimap<GameBehaviorType<?>, IGameBehavior> behaviors = LinkedHashMultimap.create();
		for (BehaviorReference reference : references) {
			reference.addTo(behaviors::put);
		}

		return new BehaviorMap(behaviors);
	}

	public Collection<IGameBehavior> getBehaviors() {
		return behaviors.values();
	}

	@SuppressWarnings("unchecked")
	public <T extends IGameBehavior> Collection<T> getBehaviors(GameBehaviorType<T> type) {
		return (Collection<T>) behaviors.get(type);
	}
}
