package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.PlayerIsolationState;
import com.mojang.serialization.Codec;

public final class IsolatePlayerStateBehavior implements IGameBehavior {
	public static final Codec<IsolatePlayerStateBehavior> CODEC = Codec.unit(IsolatePlayerStateBehavior::new);

	// TODO: this current setup is not correct- it's challenging because we have to support a transition between phases
	//       where the isolation state should be maintained. but that means we need to be able to test whether the next
	//       state is isolated or not? ideally we don't have to move this out of behaviours but it might be unavoidable
	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		PlayerIsolationState state = game.getState().get(PlayerIsolationState.KEY);
		events.listen(GamePlayerEvents.JOIN, state::accept);
		events.listen(GamePlayerEvents.LEAVE, state::restore);
	}
}
