package com.lovetropics.minigames.common.core.game.behavior;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.lovetropics.minigames.client.lobby.state.ClientBehaviorMap;
import com.lovetropics.minigames.client.lobby.state.ClientConfigList;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigList;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.config.BehaviorReference;
import com.lovetropics.minigames.common.core.game.impl.GamePhase;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;

import net.minecraft.server.MinecraftServer;

public final class BehaviorMap implements Iterable<IGameBehavior> {

	public static final BehaviorMap EMPTY = new BehaviorMap(HashMultimap.create());

	private final Multimap<GameBehaviorType<?>, IGameBehavior> behaviors;

	BehaviorMap(Multimap<GameBehaviorType<?>, IGameBehavior> behaviors) {
		this.behaviors = behaviors;
	}

	public static BehaviorMap create(MinecraftServer server, List<BehaviorReference> references) {
		Multimap<GameBehaviorType<?>, IGameBehavior> behaviors = LinkedHashMultimap.create();
		for (BehaviorReference reference : references) {
			reference.addTo(server, behaviors::put);
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

	public <V> Multimap<GameBehaviorType<?>, V> mapValues(Function<? super IGameBehavior, V> valMap) {
		return Multimaps.transformValues(behaviors, valMap);
	}

	public void configure(ClientBehaviorMap configs) {
		List<IGameBehavior> currentBehaviors = this.behaviors.values().stream().collect(Collectors.toList());
		List<ClientConfigList> newBehaviors = configs.behaviors.values().stream().collect(Collectors.toList());
		
		if (currentBehaviors.size() != newBehaviors.size()) throw new IllegalArgumentException("Invalid config length");
		
		for (int i = 0; i < currentBehaviors.size(); i++) {
			IGameBehavior b = currentBehaviors.get(i);
			currentBehaviors.set(i, b.configure(new ConfigList(newBehaviors.get(i).configs)));
		}
	}
}
