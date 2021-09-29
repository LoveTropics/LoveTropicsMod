package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.IGameDefinition;

import java.util.Iterator;

public interface ILobbyGameQueue extends Iterable<QueuedGame> {
	QueuedGame enqueue(IGameDefinition game);

	void clear();

	boolean remove(QueuedGame game);

	int size();

	@Override
	Iterator<QueuedGame> iterator();
}
