package com.lovetropics.minigames.common.core.game.lobby;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class GameLobbyId {
	private static final AtomicInteger NETWORK_ID = new AtomicInteger();

	private final UUID uuid;
	private final int networkId;

	private GameLobbyId(UUID uuid, int networkId) {
		this.uuid = uuid;
		this.networkId = networkId;
	}

	public static GameLobbyId next() {
		return new GameLobbyId(UUID.randomUUID(), NETWORK_ID.getAndIncrement());
	}

	public UUID uuid() {
		return uuid;
	}

	public int networkId() {
		return networkId;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		return uuid.equals(((GameLobbyId) obj).uuid);
	}

	@Override
	public int hashCode() {
		return uuid.hashCode();
	}
}
