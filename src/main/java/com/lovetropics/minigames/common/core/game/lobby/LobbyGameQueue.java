package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.IGameDefinition;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

public final class LobbyGameQueue implements Iterable<QueuedGame> {
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
}
