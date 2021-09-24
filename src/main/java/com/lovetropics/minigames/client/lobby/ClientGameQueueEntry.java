package com.lovetropics.minigames.client.lobby;

import net.minecraft.network.PacketBuffer;

public final class ClientGameQueueEntry {
	public final ClientGameDefinition definition;

	public ClientGameQueueEntry(ClientGameDefinition definition) {
		this.definition = definition;
	}

	public void encode(PacketBuffer buffer) {
		this.definition.encode(buffer);
	}

	public static ClientGameQueueEntry decode(PacketBuffer buffer) {
		ClientGameDefinition definition = ClientGameDefinition.decode(buffer);
		return new ClientGameQueueEntry(definition);
	}
}
