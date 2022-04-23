package com.lovetropics.minigames.common.core.game.lobby;

import net.minecraft.server.level.ServerPlayer;

public interface LobbyStateListener {
	static LobbyStateListener compose(LobbyStateListener... listeners) {
		return new LobbyStateListener() {
			@Override
			public void onPlayerJoin(IGameLobby lobby, ServerPlayer player) {
				for (LobbyStateListener listener : listeners) {
					listener.onPlayerJoin(lobby, player);
				}
			}

			@Override
			public void onPlayerLeave(IGameLobby lobby, ServerPlayer player) {
				for (LobbyStateListener listener : listeners) {
					listener.onPlayerLeave(lobby, player);
				}
			}

			@Override
			public void onPlayerStartTracking(IGameLobby lobby, ServerPlayer player) {
				for (LobbyStateListener listener : listeners) {
					listener.onPlayerStartTracking(lobby, player);
				}
			}

			@Override
			public void onPlayerStopTracking(IGameLobby lobby, ServerPlayer player) {
				for (LobbyStateListener listener : listeners) {
					listener.onPlayerStopTracking(lobby, player);
				}
			}

			@Override
			public void onLobbyStateChange(IGameLobby lobby) {
				for (LobbyStateListener listener : listeners) {
					listener.onLobbyStateChange(lobby);
				}
			}

			@Override
			public void onLobbyNameChange(IGameLobby lobby) {
				for (LobbyStateListener listener : listeners) {
					listener.onLobbyNameChange(lobby);
				}
			}

			@Override
			public void onLobbyPaused(IGameLobby lobby) {
				for (LobbyStateListener listener : listeners) {
					listener.onLobbyPaused(lobby);
				}
			}

			@Override
			public void onLobbyStop(IGameLobby lobby) {
				for (LobbyStateListener listener : listeners) {
					listener.onLobbyStop(lobby);
				}
			}

			@Override
			public void onGamePhaseChange(IGameLobby lobby) {
				for (LobbyStateListener listener : listeners) {
					listener.onGamePhaseChange(lobby);
				}
			}
		};
	}

	default void onPlayerJoin(IGameLobby lobby, ServerPlayer player) {
	}

	default void onPlayerLeave(IGameLobby lobby, ServerPlayer player) {
	}

	default void onPlayerStartTracking(IGameLobby lobby, ServerPlayer player) {
	}

	default void onPlayerStopTracking(IGameLobby lobby, ServerPlayer player) {
	}

	default void onLobbyStateChange(IGameLobby lobby) {
	}

	default void onLobbyNameChange(IGameLobby lobby) {
	}

	default void onLobbyPaused(IGameLobby lobby) {
	}

	default void onLobbyStop(IGameLobby lobby) {
	}

	default void onGamePhaseChange(IGameLobby lobby) {
	}
}
