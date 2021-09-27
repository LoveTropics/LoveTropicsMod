package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.client.lobby.state.message.JoinedLobbyMessage;
import com.lovetropics.minigames.client.lobby.state.message.LeftLobbyMessage;
import com.lovetropics.minigames.client.lobby.state.message.LobbyPlayersMessage;
import com.lovetropics.minigames.client.lobby.state.message.LobbyUpdateMessage;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.util.GameMessages;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface LobbyWatcher {
	static LobbyWatcher compose(LobbyWatcher... watchers) {
		return new LobbyWatcher() {
			@Override
			public void onPlayerJoin(IGameLobby lobby, ServerPlayerEntity player, PlayerRole role) {
				for (LobbyWatcher watcher : watchers) {
					watcher.onPlayerJoin(lobby, player, role);
				}
			}

			@Override
			public void onPlayerLeave(IGameLobby lobby, ServerPlayerEntity player) {
				for (LobbyWatcher watcher : watchers) {
					watcher.onPlayerLeave(lobby, player);
				}
			}

			@Override
			public void onLobbyCreate(IGameLobby lobby) {
				for (LobbyWatcher watcher : watchers) {
					watcher.onLobbyCreate(lobby);
				}
			}

			@Override
			public void onLobbyStop(IGameLobby lobby) {
				for (LobbyWatcher watcher : watchers) {
					watcher.onLobbyStop(lobby);
				}
			}
		};
	}

	default void onPlayerJoin(IGameLobby lobby, ServerPlayerEntity player, PlayerRole role) {
	}

	default void onPlayerLeave(IGameLobby lobby, ServerPlayerEntity player) {
	}

	default void onLobbyCreate(IGameLobby lobby) {
	}

	default void onLobbyStop(IGameLobby lobby) {
	}

	final class Messages implements LobbyWatcher {
		@Override
		public void onPlayerJoin(IGameLobby lobby, ServerPlayerEntity player, PlayerRole role) {
			PlayerSet players = PlayerSet.ofServer(lobby.getServer());
			GameMessages gameMessages = GameMessages.forLobby(lobby);

			// TODO: how do we want to manage these?
			/*if (registrations.participantCount() == definition.getMinimumParticipantCount()) {
				serverPlayers.sendMessage(gameMessages.enoughPlayers());
			}*/

			players.sendMessage(gameMessages.playerJoined(player, role));
		}

		@Override
		public void onPlayerLeave(IGameLobby lobby, ServerPlayerEntity player) {
			/*GameMessages gameMessages = GameMessages.forLobby(this);
			if (registrations.participantCount() == definition.getMinimumParticipantCount() - 1) {
				PlayerSet.ofServer(server).sendMessage(gameMessages.noLongerEnoughPlayers());
			}*/
		}

		@Override
		public void onLobbyStop(IGameLobby lobby) {
			PlayerSet players = PlayerSet.ofServer(lobby.getServer());
			players.sendMessage(GameMessages.forLobby(lobby).stopPolling()); // TODO: wrong message
		}
	}

	final class Network implements LobbyWatcher {
		@Override
		public void onPlayerJoin(IGameLobby lobby, ServerPlayerEntity player, PlayerRole role) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), JoinedLobbyMessage.create(lobby, role));
			LoveTropicsNetwork.CHANNEL.send(this.trackingPlayers(lobby), LobbyPlayersMessage.add(lobby, Collections.singleton(player)));
		}

		@Override
		public void onPlayerLeave(IGameLobby lobby, ServerPlayerEntity player) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new LeftLobbyMessage());
			LoveTropicsNetwork.CHANNEL.send(this.trackingPlayers(lobby), LobbyPlayersMessage.remove(lobby, Collections.singleton(player)));
		}

		@Override
		public void onLobbyCreate(IGameLobby lobby) {
			LoveTropicsNetwork.CHANNEL.send(this.trackingPlayers(lobby), LobbyUpdateMessage.update(lobby));
		}

		@Override
		public void onLobbyStop(IGameLobby lobby) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), LobbyUpdateMessage.remove(lobby));
		}

		private PacketDistributor.PacketTarget trackingPlayers(IGameLobby lobby) {
			return PacketDistributor.NMLIST.with(() -> {
				List<NetworkManager> tracking = new ArrayList<>();
				for (ServerPlayerEntity player : lobby.getServer().getPlayerList().getPlayers()) {
					if (lobby.isVisibleTo(player.getCommandSource())) {
						tracking.add(player.connection.netManager);
					}
				}
				return tracking;
			});
		}
	}
}
