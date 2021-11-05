package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.LobbyRegistrations;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobbyPlayers;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerRoleSelections;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.lovetropics.minigames.common.core.game.util.TeamAllocator;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Unit;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

final class LobbyPlayerManager implements IGameLobbyPlayers {
	private final GameLobby lobby;
	private final LobbyRegistrations registrations;
	private final PlayerRoleSelections roleSelections;

	LobbyPlayerManager(GameLobby lobby) {
		this.lobby = lobby;
		this.registrations = new LobbyRegistrations(lobby.getServer());
		this.roleSelections = new PlayerRoleSelections(lobby.getMetadata().id());
	}

	@Override
	public TeamAllocator<PlayerRole, ServerPlayerEntity> createRoleAllocator() {
		TeamAllocator<PlayerRole, ServerPlayerEntity> allocator = registrations.createAllocator();
		for (ServerPlayerEntity player : registrations) {
			PlayerRole role = roleSelections.getSelectedRoleFor(player);
			if (role != PlayerRole.PARTICIPANT && !allocator.hasPreference(player)) {
				allocator.addPlayer(player, role);
			}
		}

		return allocator;
	}

	@Override
	public CompletableFuture<GameResult<Unit>> join(ServerPlayerEntity player) {
		if (lobby.manager.getLobbyFor(player) != null || registrations.contains(player.getUniqueID())) {
			return CompletableFuture.completedFuture(GameResult.error(GameTexts.Commands.alreadyInLobby()));
		}

		CompletableFuture<PlayerRole> future = roleSelections.prompt(player);
		return future.thenApplyAsync(role -> {
			if (registrations.add(player.getUniqueID())) {
				lobby.onPlayerRegister(player);
			} else {
				return GameResult.error(GameTexts.Commands.alreadyInLobby());
			}
			return GameResult.ok();
		}, lobby.getServer());
	}

	@Override
	public boolean remove(ServerPlayerEntity player) {
		if (registrations.remove(player.getUniqueID())) {
			lobby.onPlayerLeave(player);
			roleSelections.remove(player);
			return true;
		}
		return false;
	}

	@Override
	public boolean forceRole(ServerPlayerEntity player, @Nullable PlayerRole role) {
		return registrations.forceRole(player.getUniqueID(), role);
	}

	@Nullable
	@Override
	public PlayerRole getForcedRoleFor(ServerPlayerEntity player) {
		return registrations.getForcedRoleFor(player.getUniqueID());
	}

	@Override
	public PlayerRoleSelections getRoleSelections() {
		return roleSelections;
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
