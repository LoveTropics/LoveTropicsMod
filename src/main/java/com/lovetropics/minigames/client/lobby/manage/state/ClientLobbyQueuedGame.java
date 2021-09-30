package com.lovetropics.minigames.client.lobby.manage.state;

import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.lobby.QueuedGame;
import net.minecraft.network.PacketBuffer;

public final class ClientLobbyQueuedGame {
	private final ClientGameDefinition definition;

	private ClientLobbyQueuedGame(ClientGameDefinition definition) {
		this.definition = definition;
	}

	public static ClientLobbyQueuedGame from(QueuedGame game) {
		IGameDefinition definition = game.definition();
		return new ClientLobbyQueuedGame(ClientGameDefinition.from(definition));
	}

	public void encode(PacketBuffer buffer) {
		this.definition.encode(buffer);
	}

	public static ClientLobbyQueuedGame decode(PacketBuffer buffer) {
		ClientGameDefinition definition = ClientGameDefinition.decode(buffer);
		return new ClientLobbyQueuedGame(definition);
	}

	public ClientGameDefinition definition() {
		return definition;
	}
}
