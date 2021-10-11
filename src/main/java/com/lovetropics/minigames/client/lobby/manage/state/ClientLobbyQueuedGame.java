package com.lovetropics.minigames.client.lobby.manage.state;

import com.lovetropics.minigames.client.lobby.state.ClientBehaviorMap;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.lobby.QueuedGame;
import com.lovetropics.minigames.common.core.game.lobby.QueuedGame;
import net.minecraft.network.PacketBuffer;

public final class ClientLobbyQueuedGame {
	private final ClientGameDefinition definition;
	private final ClientBehaviorMap configs;

	private ClientLobbyQueuedGame(ClientGameDefinition definition, ClientBehaviorMap configs) {
		this.definition = definition;
		this.configs = configs;
	}

	public static ClientLobbyQueuedGame from(QueuedGame game) {
		ClientGameDefinition definition = ClientGameDefinition.from(game.definition());
		ClientBehaviorMap configs = ClientBehaviorMap.from(game.playingBehaviors());
		return new ClientLobbyQueuedGame(definition, configs);
	}

	public void encode(PacketBuffer buffer) {
		this.definition.encode(buffer);
		this.configs.encode(buffer);
	}

	public static ClientLobbyQueuedGame decode(PacketBuffer buffer) {
		ClientGameDefinition definition = ClientGameDefinition.decode(buffer);
		ClientBehaviorMap configs = ClientBehaviorMap.decode(buffer);
		return new ClientLobbyQueuedGame(definition, configs);
	}

	public ClientGameDefinition definition() {
		return definition;
	}

	public ClientBehaviorMap configs() {
		return configs;
	}
}
