package com.lovetropics.minigames.common.core.game;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class GameInstanceId {
	private static final AtomicInteger NETWORK_ID = new AtomicInteger();

	public final UUID uuid;
	public final String commandId;
	public final int networkId;

	private GameInstanceId(UUID uuid, String commandId, int networkId) {
		this.uuid = uuid;
		this.commandId = commandId;
		this.networkId = networkId;
	}

	public static GameInstanceId generate(IGameDefinition definition) {
		UUID uuid = UUID.randomUUID();
		String commandId = definition.getDisplayId().getPath() + "_" + RandomStringUtils.randomAlphanumeric(5);
		int networkId = NETWORK_ID.getAndIncrement();
		return new GameInstanceId(uuid, commandId, networkId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		return uuid.equals(((GameInstanceId) obj).uuid);
	}

	@Override
	public int hashCode() {
		return uuid.hashCode();
	}
}
