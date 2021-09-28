package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.IGameDefinition;

import java.util.concurrent.atomic.AtomicInteger;

public final class QueuedGame {
	private static final AtomicInteger NEXT_NETWORK_ID = new AtomicInteger();

	private final int networkId;
	private final IGameDefinition definition;

	private QueuedGame(int networkId, IGameDefinition definition) {
		this.networkId = networkId;
		this.definition = definition;
	}

	public static QueuedGame create(IGameDefinition game) {
		return new QueuedGame(NEXT_NETWORK_ID.getAndIncrement(), game);
	}

	public int networkId() {
		return networkId;
	}

	public IGameDefinition definition() {
		return this.definition;
	}
}
