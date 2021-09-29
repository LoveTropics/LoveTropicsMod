package com.lovetropics.minigames.common.core.game.behavior;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.config.BehaviorReference;
import com.lovetropics.minigames.common.core.game.impl.GamePhase;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

	public void registerTo(GamePhase phase, GameStateMap state, GameEventListeners events) {
		for (IGameBehavior behavior : this) {
			behavior.registerState(phase, state);
		}

		for (IGameBehavior behavior : this) {
			behavior.register(phase, events);
		}
	}

	@Override
	public Iterator<IGameBehavior> iterator() {
		return this.behaviors.values().iterator();
	}

	public Stream<IGameBehavior> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
}
