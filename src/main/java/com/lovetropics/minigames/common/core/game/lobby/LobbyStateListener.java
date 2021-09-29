package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import net.minecraft.entity.player.ServerPlayerEntity;

public interface LobbyStateListener {
	static LobbyStateListener compose(LobbyStateListener... watchers) {
		return new LobbyStateListener() {
			@Override
			public void onPlayerJoin(IGameLobby lobby, ServerPlayerEntity player, PlayerRole registeredRole) {
				for (LobbyStateListener watcher : watchers) {
					watcher.onPlayerJoin(lobby, player, registeredRole);
				}
			}

			@Override
			public void onPlayerLeave(IGameLobby lobby, ServerPlayerEntity player) {
				for (LobbyStateListener watcher : watchers) {
					watcher.onPlayerLeave(lobby, player);
				}
			}

			@Override
			public void onPlayerStartTracking(IGameLobby lobby, ServerPlayerEntity player) {
				for (LobbyStateListener watcher : watchers) {
					watcher.onPlayerStartTracking(lobby, player);
				}
			}

			@Override
			public void onPlayerStopTracking(IGameLobby lobby, ServerPlayerEntity player) {
				for (LobbyStateListener watcher : watchers) {
					watcher.onPlayerStopTracking(lobby, player);
				}
			}

			@Override
			public void onLobbyPaused(IGameLobby lobby) {
				for (LobbyStateListener watcher : watchers) {
					watcher.onLobbyPaused(lobby);
				}
			}

			@Override
			public void onLobbyStop(IGameLobby lobby) {
				for (LobbyStateListener watcher : watchers) {
					watcher.onLobbyStop(lobby);
				}
			}

			@Override
			public void onGamePhaseChange(IGameLobby lobby) {
				for (LobbyStateListener watcher : watchers) {
					watcher.onGamePhaseChange(lobby);
				}
			}
		};
	}

	default void onPlayerJoin(IGameLobby lobby, ServerPlayerEntity player, PlayerRole registeredRole) {
	}

	default void onPlayerLeave(IGameLobby lobby, ServerPlayerEntity player) {
	}

	default void onPlayerStartTracking(IGameLobby lobby, ServerPlayerEntity player) {
	}

	default void onPlayerStopTracking(IGameLobby lobby, ServerPlayerEntity player) {
	}

	default void onLobbyPaused(IGameLobby lobby) {
	}

	default void onLobbyStop(IGameLobby lobby) {
	}

	default void onGamePhaseChange(IGameLobby lobby) {
	}
}
