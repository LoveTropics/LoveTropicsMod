package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.common.core.game.GameRegistrations;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobbyPlayers;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.TeamAllocator;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.UUID;

final class LobbyPlayerManager implements IGameLobbyPlayers {
	private final GameLobby lobby;
	private final GameRegistrations registrations;

	LobbyPlayerManager(GameLobby lobby) {
		this.lobby = lobby;
		this.registrations = new GameRegistrations(lobby.getServer());
	}

	@Override
	public TeamAllocator<PlayerRole, ServerPlayerEntity> createRoleAllocator() {
		return registrations.createAllocator();
	}

	@Override
	public boolean register(ServerPlayerEntity player, @Nullable PlayerRole requestedRole) {
		if (registrations.add(player.getUniqueID(), requestedRole)) {
			lobby.onPlayerRegister(player);
			return true;
		}
		return false;
	}

	@Override
	public boolean remove(ServerPlayerEntity player) {
		if (registrations.remove(player.getUniqueID())) {
			lobby.onPlayerLeave(player);
			return true;
		}
		return false;
	}

	@Nullable
	@Override
	public PlayerRole getRegisteredRoleFor(ServerPlayerEntity player) {
		return registrations.getRoleFor(player.getUniqueID());
	}

	@Override
	public boolean contains(UUID id) {
		return registrations.contains(id);
	}

	@Nullable
	@Override
	public ServerPlayerEntity getPlayerBy(UUID id) {
		return registrations.getPlayerBy(id);
	}

	@Override
	public int size() {
		return registrations.size();
	}

	@Override
	public Iterator<ServerPlayerEntity> iterator() {
		return registrations.iterator();
	}
}
