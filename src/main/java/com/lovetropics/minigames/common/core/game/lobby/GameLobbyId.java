package com.lovetropics.minigames.common.core.game.lobby;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class GameLobbyId {
	private static final AtomicInteger NETWORK_ID = new AtomicInteger();

	private final UUID uuid;
	private final String name;
	private final String commandId;
	private final int networkId;

	private GameLobbyId(UUID uuid, String name, String commandId, int networkId) {
		this.uuid = uuid;
		this.name = name;
		this.commandId = commandId;
		this.networkId = networkId;
	}

	public static GameLobbyId create(String name, String commandId) {
		return new GameLobbyId(UUID.randomUUID(), name, commandId, NETWORK_ID.getAndIncrement());
	}

	public UUID getUuid() {
		return uuid;
	}

	public IFormattableTextComponent getName() {
		return new StringTextComponent(name);
	}

	public String getCommandId() {
		return commandId;
	}

	public int getNetworkId() {
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
