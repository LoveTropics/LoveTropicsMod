package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.IGameDefinition;

public final class QueuedGame {
	private final IGameDefinition definition;

	private QueuedGame(IGameDefinition definition) {
		this.definition = definition;
	}

	public static QueuedGame create(IGameDefinition game) {
		return new QueuedGame(game);
	}

	public IGameDefinition definition() {
		return this.definition;
	}
}
