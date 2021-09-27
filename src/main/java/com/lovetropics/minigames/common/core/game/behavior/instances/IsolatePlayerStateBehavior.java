package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.PlayerIsolationState;
import com.mojang.serialization.Codec;

public final class IsolatePlayerStateBehavior implements IGameBehavior {
	public static final Codec<IsolatePlayerStateBehavior> CODEC = Codec.unit(IsolatePlayerStateBehavior::new);

	// TODO: handle full game instance ending- is leave included? (we can't just use stop!)
	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		PlayerIsolationState state = game.getState().get(PlayerIsolationState.KEY);
		events.listen(GamePlayerEvents.JOIN, state::accept);
		events.listen(GamePlayerEvents.LEAVE, state::restore);
	}
}
