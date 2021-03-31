package com.lovetropics.minigames.common.core.game.behavior;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.lovetropics.minigames.common.core.game.config.BehaviorReference;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public final class BehaviorMap implements Iterable<IGameBehavior> {
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

	public Collection<IGameBehavior> values() {
		return behaviors.values();
	}

	@SuppressWarnings("unchecked")
	public <T extends IGameBehavior> Collection<T> get(GameBehaviorType<T> type) {
		return (Collection<T>) behaviors.get(type);
	}

	public <T extends IGameBehavior> Optional<T> getOne(GameBehaviorType<T> type) {
		return get(type).stream().findFirst();
	}

	public <T extends IGameBehavior> T getOneOrThrow(GameBehaviorType<T> type) {
		return getOne(type).orElseThrow(RuntimeException::new);
	}

	@Override
	public Iterator<IGameBehavior> iterator() {
		return this.behaviors.values().iterator();
	}
}
