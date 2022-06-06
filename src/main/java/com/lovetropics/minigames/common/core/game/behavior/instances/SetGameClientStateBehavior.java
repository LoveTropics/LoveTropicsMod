package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SetGameClientStateBehavior(GameClientState state) implements IGameBehavior {
	public static final Codec<SetGameClientStateBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			GameClientState.CODEC.fieldOf("state").forGetter(c -> c.state)
	).apply(i, SetGameClientStateBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		GameClientState.applyGlobally(state, events);
	}
}
