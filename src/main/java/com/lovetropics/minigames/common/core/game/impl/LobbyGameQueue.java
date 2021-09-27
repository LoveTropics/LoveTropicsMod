package com.lovetropics.minigames.common.core.game.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.annotation.Nullable;

import com.lovetropics.minigames.client.lobby.state.ClientBehaviorMap;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.client.lobby.state.ClientQueuedGame;
import com.lovetropics.minigames.common.core.game.IGameDefinition;

public final class LobbyGameQueue {
	private final Queue<QueuedGame> queue = new ArrayDeque<>();

	public void enqueue(IGameDefinition game) {
		queue.add(QueuedGame.create(game));
	}

	@Nullable
	public QueuedGame next() {
		return queue.poll();
	}

	public List<ClientQueuedGame> clientEntries() {
		List<ClientQueuedGame> entries = new ArrayList<>(queue.size());
		for (QueuedGame game : queue) {
			ClientGameDefinition clientDefinition = ClientGameDefinition.from(game.definition());
			ClientBehaviorMap clientBehaviors = ClientBehaviorMap.from(game.behaviors());
			entries.add(new ClientQueuedGame(clientDefinition, clientBehaviors));
		}
		return entries;
	}
}
