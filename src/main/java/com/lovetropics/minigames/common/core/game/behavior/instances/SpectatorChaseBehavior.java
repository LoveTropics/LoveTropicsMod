package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.client_state.instance.SpectatingClientState;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SpectatorChaseBehavior implements IGameBehavior {
	public static final Codec<SpectatorChaseBehavior> CODEC = Codec.unit(SpectatorChaseBehavior::new);

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> sendSpectatingUpdate(game));
		events.listen(GamePlayerEvents.REMOVE, player -> removePlayer(game, player));
		events.listen(GamePhaseEvents.DESTROY, () -> stop(game));
	}

	private void removePlayer(IGamePhase game, ServerPlayerEntity player) {
		GameClientState.removeFromPlayer(GameClientStateTypes.SPECTATING.get(), player);

		this.sendSpectatingUpdate(game);
	}

	private void sendSpectatingUpdate(IGamePhase game) {
		SpectatingClientState spectating = this.buildSpectatingState(game);
		GameClientState.sendToPlayers(spectating, game.getSpectators());
	}

	private void stop(IGamePhase game) {
		GameClientState.removeFromPlayers(GameClientStateTypes.SPECTATING.get(), game.getSpectators());
	}

	private SpectatingClientState buildSpectatingState(IGamePhase game) {
		PlayerSet participants = game.getParticipants();

		List<UUID> ids = new ArrayList<>(participants.size());
		for (ServerPlayerEntity participant : participants) {
			ids.add(participant.getUniqueID());
		}

		return new SpectatingClientState(ids);
	}
}
