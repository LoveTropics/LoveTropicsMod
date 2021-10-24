package com.lovetropics.minigames.client.lobby.manage.state;

import com.lovetropics.minigames.client.lobby.state.ClientBehaviorMap;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.common.core.game.lobby.QueuedGame;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;

public final class ClientLobbyQueuedGame {
	private final ClientGameDefinition definition;
	private final ClientBehaviorMap playingConfigs;
	@Nullable
	private final ClientBehaviorMap waitingConfigs;

	private ClientLobbyQueuedGame(ClientGameDefinition definition, ClientBehaviorMap playingConfigs, @Nullable ClientBehaviorMap waitingConfigs) {
		this.definition = definition;
		this.playingConfigs = playingConfigs;
		this.waitingConfigs = waitingConfigs;
	}

	public static ClientLobbyQueuedGame from(QueuedGame game) {
		ClientGameDefinition definition = ClientGameDefinition.from(game.definition());
		ClientBehaviorMap playingConfigs = ClientBehaviorMap.from(game.playingBehaviors());
		ClientBehaviorMap waitingConfigs = game.waitingBehaviors() != null ? ClientBehaviorMap.from(game.waitingBehaviors()) : null;
		return new ClientLobbyQueuedGame(definition, playingConfigs, waitingConfigs);
	}

	public void encode(PacketBuffer buffer) {
		this.definition.encode(buffer);
		this.playingConfigs.encode(buffer);

		buffer.writeBoolean(this.waitingConfigs != null);
		if (this.waitingConfigs != null) {
			this.waitingConfigs.encode(buffer);
		}
	}

	public static ClientLobbyQueuedGame decode(PacketBuffer buffer) {
		ClientGameDefinition definition = ClientGameDefinition.decode(buffer);
		ClientBehaviorMap playingConfigs = ClientBehaviorMap.decode(buffer);
		ClientBehaviorMap waitingConfigs = buffer.readBoolean() ? ClientBehaviorMap.decode(buffer) : null;
		return new ClientLobbyQueuedGame(definition, playingConfigs, waitingConfigs);
	}

	public ClientGameDefinition definition() {
		return definition;
	}

	public ClientBehaviorMap playingConfigs() {
		return playingConfigs;
	}

	@Nullable
	public ClientBehaviorMap waitingConfigs() {
		return waitingConfigs;
	}
}
