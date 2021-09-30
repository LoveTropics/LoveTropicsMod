package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.client.lobby.manage.ClientManageLobbyMessage;
import com.lovetropics.minigames.client.lobby.state.ClientCurrentGame;
import com.lovetropics.minigames.client.lobby.manage.state.update.ClientLobbyUpdate;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.lobby.*;
import com.lovetropics.minigames.common.core.game.player.MutablePlayerSet;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.util.Scheduler;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.UnaryOperator;

final class LobbyManagement implements ILobbyManagement {
	private final GameLobby lobby;

	private final MutablePlayerSet managingPlayers;

	LobbyManagement(GameLobby lobby) {
		this.lobby = lobby;
		this.managingPlayers = new MutablePlayerSet(lobby.getServer());
	}

	void disable() {
		managingPlayers.clear();
	}

	void onGameStateChange() {
		sendUpdates(updates -> {
			ClientCurrentGame currentGame = lobby.state.getClientCurrentGame();
			ILobbyGameQueue gameQueue = lobby.getGameQueue();
			LobbyControls controls = lobby.getControls();
			return updates.setCurrentGame(currentGame)
					.updateQueue(gameQueue)
					.setControlState(controls.asState());
		});
	}

	@Override
	public boolean startManaging(ServerPlayerEntity player) {
		if (canManage(player.getCommandSource())) {
			ClientLobbyUpdate.Set initialize = ClientLobbyUpdate.Set.create()
					.setName(lobby.getMetadata().name())
					.initInstalledGames(ClientGameDefinition.collectInstalled())
					.initQueue(lobby.getGameQueue())
					.setCurrentGame(lobby.state.getClientCurrentGame())
					.setPlayersFrom(lobby)
					.setControlState(lobby.getControls().asState())
					.setVisibility(lobby.getVisibility());

			ClientManageLobbyMessage message = initialize.intoMessage(lobby.metadata.id().networkId());
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);

			managingPlayers.add(player);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void stopManaging(ServerPlayerEntity player) {
		managingPlayers.remove(player);
	}

	@Override
	public boolean canManage(CommandSource source) {
		return source.hasPermissionLevel(2) || lobby.getMetadata().initiator().matches(source.getEntity());
	}

	@Override
	public void setName(String name) {
		lobby.setName(name);
		sendUpdates(updates -> updates.setName(name));
	}

	@Override
	public void enqueueGame(IGameDefinition game) {
		QueuedGame queued = lobby.gameQueue.enqueue(game);
		sendUpdates(updates -> updates.updateQueue(lobby.getGameQueue(), queued.networkId()));
	}

	@Override
	public void removeQueuedGame(int id) {
		QueuedGame removed = lobby.gameQueue.removeByNetworkId(id);
		if (removed != null) {
			sendUpdates(updates -> updates.updateQueue(lobby.gameQueue));
		}
	}

	@Override
	public void reorderQueuedGame(int id, int newIndex) {
		if (lobby.gameQueue.reorderByNetworkId(id, newIndex)) {
			sendUpdates(updates -> updates.updateQueue(lobby.gameQueue));
		}
	}

	@Override
	public void selectControl(LobbyControls.Type type) {
		LobbyControls.Action action = lobby.getControls().get(type);
		if (action != null) {
			Scheduler.nextTick().run(server -> {
				// TODO: handle result
				action.run();
			});
		}
	}

	@Override
	public void setVisibility(LobbyVisibility visibility) {
		lobby.setVisibility(visibility);
		sendUpdates(updates -> updates.setVisibility(visibility));
	}

	@Override
	public void close() {
		lobby.close();
	}

	private void sendUpdates(UnaryOperator<ClientLobbyUpdate.Set> updates) {
		ClientLobbyUpdate.Set set = ClientLobbyUpdate.Set.create();
		set = updates.apply(set);

		ClientManageLobbyMessage message = set.intoMessage(lobby.getMetadata().id().networkId());
		managingPlayers.sendPacket(LoveTropicsNetwork.CHANNEL, message);
	}
}
