package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import net.minecraft.entity.player.ServerPlayerEntity;

public interface LobbyStateListener {
	static LobbyStateListener compose(LobbyStateListener... listeners) {
		return new LobbyStateListener() {
			@Override
			public void onPlayerJoin(IGameLobby lobby, ServerPlayerEntity player, PlayerRole registeredRole) {
				for (LobbyStateListener listener : listeners) {
					listener.onPlayerJoin(lobby, player, registeredRole);
				}
			}

			@Override
			public void onPlayerLeave(IGameLobby lobby, ServerPlayerEntity player) {
				for (LobbyStateListener listener : listeners) {
					listener.onPlayerLeave(lobby, player);
				}
			}

			@Override
			public void onPlayerStartTracking(IGameLobby lobby, ServerPlayerEntity player) {
				for (LobbyStateListener listener : listeners) {
					listener.onPlayerStartTracking(lobby, player);
				}
			}

			@Override
			public void onPlayerStopTracking(IGameLobby lobby, ServerPlayerEntity player) {
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

	default void onPlayerJoin(IGameLobby lobby, ServerPlayerEntity player, PlayerRole registeredRole) {
	}

	default void onPlayerLeave(IGameLobby lobby, ServerPlayerEntity player) {
	}

	default void onPlayerStartTracking(IGameLobby lobby, ServerPlayerEntity player) {
	}

	default void onPlayerStopTracking(IGameLobby lobby, ServerPlayerEntity player) {
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
