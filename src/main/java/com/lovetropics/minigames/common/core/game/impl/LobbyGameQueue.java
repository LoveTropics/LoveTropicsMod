package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.lobby.ILobbyGameQueue;
import com.lovetropics.minigames.common.core.game.lobby.QueuedGame;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

final class LobbyGameQueue implements ILobbyGameQueue {
	private final Queue<QueuedGame> entries = new ArrayDeque<>();

	@Nullable
	QueuedGame next() {
		return entries.poll();
	}

	@Override
	public QueuedGame enqueue(IGameDefinition game) {
		QueuedGame entry = QueuedGame.create(game);
		entries.add(entry);
		return entry;
	}

	@Override
	public void clear() {
		entries.clear();
	}

	@Override
	public Iterator<QueuedGame> iterator() {
		return entries.iterator();
	}

	@Override
	public int size() {
		return entries.size();
	}

	@Nullable
	QueuedGame removeByNetworkId(int networkId) {
		Iterator<QueuedGame> iterator = entries.iterator();
		while (iterator.hasNext()) {
			QueuedGame game = iterator.next();
			if (game.networkId() == networkId) {
				iterator.remove();
				return game;
			}
		}
		return null;
	}

	@Nullable
	QueuedGame byNetworkId(int networkId) {
		for (QueuedGame game : this) {
			if (game.networkId() == networkId) {
				return game;
			}
		}
		return null;
	}
}
