package com.lovetropics.minigames.client.lobby.state;

import net.minecraft.network.PacketBuffer;

public final class ClientQueuedGame {
	public final ClientGameDefinition definition;

	public ClientQueuedGame(ClientGameDefinition definition) {
		this.definition = definition;
	}

	public void encode(PacketBuffer buffer) {
		this.definition.encode(buffer);
	}

	public static ClientQueuedGame decode(PacketBuffer buffer) {
		ClientGameDefinition definition = ClientGameDefinition.decode(buffer);
		return new ClientQueuedGame(definition);
	}
}
