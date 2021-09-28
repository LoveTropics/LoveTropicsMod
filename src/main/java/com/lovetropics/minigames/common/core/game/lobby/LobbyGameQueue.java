package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.IGameDefinition;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

public final class LobbyGameQueue implements Iterable<QueuedGame> {
	private final Queue<QueuedGame> queue = new ArrayDeque<>();

	public QueuedGame enqueue(IGameDefinition game) {
		QueuedGame queued = QueuedGame.create(game);
		queue.add(queued);
		return queued;
	}

	public void clear() {
		this.queue.clear();
	}

	@Nullable
	public QueuedGame next() {
		return queue.poll();
	}

	public boolean remove(QueuedGame game) {
		return queue.remove(game);
	}

	@Nullable
	public QueuedGame byNetworkId(int networkId) {
		for (QueuedGame game : queue) {
			if (game.networkId() == networkId) {
				return game;
			}
		}
		return null;
	}

	@Override
	public Iterator<QueuedGame> iterator() {
		return queue.iterator();
	}

	public int size() {
		return queue.size();
	}
}
