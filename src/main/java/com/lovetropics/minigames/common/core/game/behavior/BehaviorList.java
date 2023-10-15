package com.lovetropics.minigames.common.core.game.behavior;

import com.lovetropics.minigames.client.lobby.state.ClientBehaviorList;
import com.lovetropics.minigames.client.lobby.state.ClientConfigList;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigList;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.impl.GamePhase;
import net.minecraft.resources.ResourceLocation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class BehaviorList implements Iterable<IGameBehavior> {
	public static final BehaviorList EMPTY = new BehaviorList(List.of());

	private final List<IGameBehavior> behaviors;

	public BehaviorList(List<IGameBehavior> behaviors) {
		this.behaviors = List.copyOf(behaviors);
	}

	public static BehaviorList instantiate(List<BehaviorTemplate> behaviors) {
		return new BehaviorList(behaviors.stream()
				.map(BehaviorTemplate::instantiate)
				.toList()
		);
	}

	public void registerTo(GamePhase phase, GameEventListeners events) {
		for (IGameBehavior behavior : this) {
			behavior.registerState(phase, phase.getState(), phase.getInstanceState());
		}

		for (IGameBehavior behavior : this) {
			behavior.register(phase, events);
		}
	}

	@Override
	public Iterator<IGameBehavior> iterator() {
		return behaviors.iterator();
	}

	public Stream<IGameBehavior> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	// TODO: Actually use result
	public BehaviorList configure(ClientBehaviorList configs) {
		Map<ResourceLocation, ConfigList> configsById = configs.behaviors().stream().collect(Collectors.toMap(ClientConfigList::id, list -> new ConfigList(list.id(), list.configs())));
		return new BehaviorList(behaviors.stream().map(behavior -> behavior.configure(configsById)).toList());
	}
}
