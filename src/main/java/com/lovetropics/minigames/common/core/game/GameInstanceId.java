package com.lovetropics.minigames.common.core.game;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;

public final class GameInstanceId {
	public final UUID uuid;
	public final String commandId;

	private GameInstanceId(UUID uuid, String commandId) {
		this.uuid = uuid;
		this.commandId = commandId;
	}

	public static GameInstanceId generate(IGameDefinition definition) {
		UUID uuid = UUID.randomUUID();
		String commandId = definition.getDisplayId().getPath() + "_" + RandomStringUtils.randomAlphanumeric(5);
		return new GameInstanceId(uuid, commandId);
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
