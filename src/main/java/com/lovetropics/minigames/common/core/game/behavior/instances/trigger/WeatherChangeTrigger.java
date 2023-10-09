package com.lovetropics.minigames.common.core.game.behavior.instances.trigger;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameWorldEvents;
import com.lovetropics.minigames.common.core.game.weather.WeatherEventType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public class WeatherChangeTrigger implements IGameBehavior {
	public static final Codec<WeatherChangeTrigger> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.unboundedMap(WeatherEventType.CODEC, GameActionList.CODEC).fieldOf("events").forGetter(c -> c.eventActions)
	).apply(i, WeatherChangeTrigger::new));

	private final Map<WeatherEventType, GameActionList> eventActions;

	public WeatherChangeTrigger(Map<WeatherEventType, GameActionList> eventActions) {
		this.eventActions = eventActions;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		for (GameActionList actions : eventActions.values()) {
			actions.register(game, events);
		}

		events.listen(GameWorldEvents.SET_WEATHER, (lastEvent, event) -> {
			if (event != null) {
				GameActionList actions = eventActions.get(event.getType());
				if (actions != null) {
					actions.applyPlayer(game, GameActionContext.EMPTY);
				}
			}
		});
	}
}
