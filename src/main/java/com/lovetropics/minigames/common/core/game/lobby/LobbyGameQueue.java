package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.client.lobby.state.ClientQueuedGame;
import com.lovetropics.minigames.common.core.game.IGameDefinition;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public final class LobbyGameQueue {
	private final Queue<QueuedGame> queue = new ArrayDeque<>();

	public void enqueue(IGameDefinition game) {
		queue.add(QueuedGame.create(game));
	}

	public void clear() {
		this.queue.clear();
	}

	@Nullable
	public QueuedGame next() {
		return queue.poll();
	}

	public List<ClientQueuedGame> clientEntries() {
		List<ClientQueuedGame> entries = new ArrayList<>(queue.size());
		for (QueuedGame game : queue) {
			ClientGameDefinition clientDefinition = ClientGameDefinition.from(game.definition());
			entries.add(new ClientQueuedGame(clientDefinition));
		}
		return entries;
	}
}
