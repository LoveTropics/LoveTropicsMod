package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.LobbyRegistrations;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobbyPlayers;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerRoleSelections;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.lovetropics.minigames.common.core.game.util.TeamAllocator;
import net.minecraft.server.level.ServerPlayer;
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
	public TeamAllocator<PlayerRole, ServerPlayer> createRoleAllocator() {
		TeamAllocator<PlayerRole, ServerPlayer> allocator = registrations.createAllocator();
		for (ServerPlayer player : registrations) {
			PlayerRole role = roleSelections.getSelectedRoleFor(player);
			if (role != PlayerRole.PARTICIPANT && !allocator.hasPreference(player)) {
				allocator.addPlayer(player, role);
			}
		}

		return allocator;
	}

	@Override
	public CompletableFuture<GameResult<Unit>> join(ServerPlayer player) {
		if (lobby.manager.getLobbyFor(player) != null || registrations.contains(player.getUUID())) {
			return CompletableFuture.completedFuture(GameResult.error(GameTexts.Commands.alreadyInLobby()));
		}

		CompletableFuture<PlayerRole> future = roleSelections.prompt(player);
		return future.thenApplyAsync(role -> {
			if (registrations.add(player.getUUID())) {
				lobby.onPlayerRegister(player);
			} else {
				return GameResult.error(GameTexts.Commands.alreadyInLobby());
			}
			return GameResult.ok();
		}, lobby.getServer());
	}

	@Override
	public boolean remove(ServerPlayer player) {
		if (registrations.remove(player.getUUID())) {
			lobby.onPlayerLeave(player);
			roleSelections.remove(player);
			return true;
		}
		return false;
	}

	@Override
	public boolean forceRole(ServerPlayer player, @Nullable PlayerRole role) {
		return registrations.forceRole(player.getUUID(), role);
	}

	@Nullable
	@Override
	public PlayerRole getForcedRoleFor(ServerPlayer player) {
		return registrations.getForcedRoleFor(player.getUUID());
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
	public ServerPlayer getPlayerBy(UUID id) {
		return registrations.getPlayerBy(id);
	}

	@Override
	public int size() {
		return registrations.size();
	}

	@Override
	public Iterator<ServerPlayer> iterator() {
		return registrations.iterator();
	}
}
