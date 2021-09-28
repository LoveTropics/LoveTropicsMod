package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.common.core.game.player.MutablePlayerSet;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.UUID;

final class LobbyTrackingPlayers implements PlayerSet {
	private final GameLobby lobby;
	private final MutablePlayerSet tracking;

	LobbyTrackingPlayers(GameLobby lobby) {
		this.lobby = lobby;
		this.tracking = new MutablePlayerSet(lobby.getServer());
	}

	// TODO: allow games to be published
	void onVisibilityChange() {
		for (ServerPlayerEntity player : PlayerSet.ofServer(this.lobby.getServer())) {
			if (this.lobby.isVisibleTo(player)) {
				this.startTracking(player);
			} else {
				this.stopTracking(player);
			}
		}
	}

	void onPlayerLoggedIn(ServerPlayerEntity player) {
		if (this.lobby.isVisibleTo(player)) {
			this.startTracking(player);
		}
	}

	void onPlayerLoggedOut(ServerPlayerEntity player) {
		this.stopTracking(player);
	}

	private void startTracking(ServerPlayerEntity player) {
		if (this.tracking.add(player)) {
			this.lobby.onPlayerStartTracking(player);
		}
	}

	private void stopTracking(ServerPlayerEntity player) {
		if (this.tracking.remove(player)) {
			this.lobby.onPlayerStopTracking(player);
		}
	}

	@Override
	public boolean contains(UUID id) {
		return this.tracking.contains(id);
	}

	@Nullable
	@Override
	public ServerPlayerEntity getPlayerBy(UUID id) {
		return this.tracking.getPlayerBy(id);
	}

	@Override
	public int size() {
		return this.tracking.size();
	}

	@Override
	public Iterator<ServerPlayerEntity> iterator() {
		return this.tracking.iterator();
	}
}
