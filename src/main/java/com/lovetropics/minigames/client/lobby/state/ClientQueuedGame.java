package com.lovetropics.minigames.client.lobby.state;

import net.minecraft.network.PacketBuffer;

public final class ClientQueuedGame {
	public final ClientGameDefinition definition;
	public final ClientBehaviorMap configs;

	public ClientQueuedGame(ClientGameDefinition definition, ClientBehaviorMap configs) {
		this.definition = definition;
		this.configs = configs;
	}

	public void encode(PacketBuffer buffer) {
		this.definition.encode(buffer);
	}

	public static ClientQueuedGame decode(PacketBuffer buffer) {
		ClientGameDefinition definition = ClientGameDefinition.decode(buffer);
		ClientBehaviorMap configs = ClientBehaviorMap.decode(buffer);
		return new ClientQueuedGame(definition, configs);
	}
}
