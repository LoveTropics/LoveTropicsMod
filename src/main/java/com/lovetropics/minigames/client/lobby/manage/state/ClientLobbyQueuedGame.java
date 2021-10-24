package com.lovetropics.minigames.client.lobby.manage.state;

import com.lovetropics.minigames.client.lobby.state.ClientBehaviorMap;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.lobby.QueuedGame;
import com.lovetropics.minigames.common.core.game.lobby.QueuedGame;
import net.minecraft.network.PacketBuffer;

public final class ClientLobbyQueuedGame {
	private final ClientGameDefinition definition;
	private final ClientBehaviorMap playingConfigs, waitingConfigs;

	private ClientLobbyQueuedGame(ClientGameDefinition definition, ClientBehaviorMap playingConfigs, ClientBehaviorMap waitingConfigs) {
		this.definition = definition;
		this.playingConfigs = playingConfigs;
		this.waitingConfigs = waitingConfigs;
	}

	public static ClientLobbyQueuedGame from(QueuedGame game) {
		ClientGameDefinition definition = ClientGameDefinition.from(game.definition());
		ClientBehaviorMap playingConfigs = ClientBehaviorMap.from(game.playingBehaviors());
		ClientBehaviorMap waitingConfigs = ClientBehaviorMap.from(game.waitingBehaviors());
		return new ClientLobbyQueuedGame(definition, playingConfigs, waitingConfigs);
	}

	public void encode(PacketBuffer buffer) {
		this.definition.encode(buffer);
		this.playingConfigs.encode(buffer);
		this.waitingConfigs.encode(buffer);
	}

	public static ClientLobbyQueuedGame decode(PacketBuffer buffer) {
		ClientGameDefinition definition = ClientGameDefinition.decode(buffer);
		ClientBehaviorMap playingConfigs = ClientBehaviorMap.decode(buffer);
		ClientBehaviorMap waitingConfigs = ClientBehaviorMap.decode(buffer);
		return new ClientLobbyQueuedGame(definition, playingConfigs, waitingConfigs);
	}

	public ClientGameDefinition definition() {
		return definition;
	}

	public ClientBehaviorMap playingConfigs() {
		return playingConfigs;
	}

	public ClientBehaviorMap waitingConfigs() {
		return waitingConfigs;
	}
}
