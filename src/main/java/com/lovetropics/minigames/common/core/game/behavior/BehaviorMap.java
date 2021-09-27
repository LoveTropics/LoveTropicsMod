package com.lovetropics.minigames.common.core.game.behavior;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.base.Function;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.lovetropics.minigames.common.core.game.config.BehaviorReference;

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

	@Override
	public Iterator<IGameBehavior> iterator() {
		return this.behaviors.values().iterator();
	}

	public Stream<IGameBehavior> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
	
	public <V> Multimap<GameBehaviorType<?>, V> mapValues(Function<? super IGameBehavior, V> valMap) {
		return Multimaps.transformValues(behaviors, valMap);
	}
}
