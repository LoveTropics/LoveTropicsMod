package com.lovetropics.minigames.client.lobby.manage.state;

import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import net.minecraft.network.PacketBuffer;

public final class ClientLobbyQueuedGame {
	private final ClientGameDefinition definition;

	public ClientLobbyQueuedGame(ClientGameDefinition definition) {
		this.definition = definition;
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
