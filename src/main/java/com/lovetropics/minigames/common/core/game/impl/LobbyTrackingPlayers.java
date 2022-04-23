package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.common.core.game.player.MutablePlayerSet;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.UUID;

final class LobbyTrackingPlayers implements PlayerSet {
	private final GameLobby lobby;
	private final MutablePlayerSet tracking;

	LobbyTrackingPlayers(GameLobby lobby) {
		this.lobby = lobby;
		this.tracking = new MutablePlayerSet(lobby.getServer());

		this.rebuildTracking();
	}

	void rebuildTracking() {
		for (ServerPlayer player : PlayerSet.ofServer(this.lobby.getServer())) {
			if (this.lobby.isVisibleTo(player)) {
				this.startTracking(player);
			} else {
				this.stopTracking(player);
			}
		}
	}

	void onPlayerLoggedIn(ServerPlayer player) {
		if (this.lobby.isVisibleTo(player)) {
			this.startTracking(player);
		}
	}

	void onPlayerLoggedOut(ServerPlayer player) {
		this.stopTracking(player);
	}

	private void startTracking(ServerPlayer player) {
		if (this.tracking.add(player)) {
			this.lobby.onPlayerStartTracking(player);
		}
	}

	private void stopTracking(ServerPlayer player) {
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
	public ServerPlayer getPlayerBy(UUID id) {
		return this.tracking.getPlayerBy(id);
	}

	@Override
	public int size() {
		return this.tracking.size();
	}

	@Override
	public Iterator<ServerPlayer> iterator() {
		return this.tracking.iterator();
	}
}
