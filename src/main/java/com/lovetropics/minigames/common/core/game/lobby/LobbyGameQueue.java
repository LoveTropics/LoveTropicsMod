package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.client.lobby.ClientGameDefinition;
import com.lovetropics.minigames.client.lobby.ClientGameQueueEntry;
import com.lovetropics.minigames.common.core.game.IGameDefinition;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public final class LobbyGameQueue {
	private final Queue<IGameDefinition> queue = new ArrayDeque<>();

	public void enqueue(IGameDefinition game) {
		queue.add(game);
	}

	@Nullable
	public IGameDefinition next() {
		return queue.poll();
	}

	public List<ClientGameQueueEntry> clientEntries() {
		List<ClientGameQueueEntry> entries = new ArrayList<>(queue.size());
		for (IGameDefinition game : queue) {
			ClientGameDefinition clientDefinition = ClientGameDefinition.from(game);
			entries.add(new ClientGameQueueEntry(clientDefinition));
		}
		return entries;
	}
}
