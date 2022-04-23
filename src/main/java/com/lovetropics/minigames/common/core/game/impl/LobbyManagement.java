package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.client.lobby.manage.ClientManageLobbyMessage;
import com.lovetropics.minigames.client.lobby.manage.state.update.ClientLobbyUpdate;
import com.lovetropics.minigames.client.lobby.state.ClientCurrentGame;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.lobby.*;
import com.lovetropics.minigames.common.core.game.player.MutablePlayerSet;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.util.Scheduler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

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
	public boolean startManaging(ServerPlayer player) {
		if (canManage(player.createCommandSourceStack())) {
			ClientLobbyUpdate.Set initialize = ClientLobbyUpdate.Set.create()
					.setName(lobby.getMetadata().name())
					.initialize(ClientGameDefinition.collectInstalled(), lobby.getGameQueue())
					.setCurrentGame(lobby.state.getClientCurrentGame())
					.setPlayersFrom(lobby)
					.setControlState(lobby.getControls().asState())
					.setVisibility(lobby.getMetadata().visibility(), !lobby.manager.hasFocusedLiveLobby());

			ClientManageLobbyMessage message = initialize.intoMessage(lobby.metadata.id().networkId());
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);

			managingPlayers.add(player);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void stopManaging(ServerPlayer player) {
		managingPlayers.remove(player);
	}

	@Override
	public boolean canManage(CommandSourceStack source) {
		return source.hasPermission(2) || lobby.getMetadata().initiator().matches(source.getEntity());
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
	public QueuedGame getQueuedGame(int id) {
		return lobby.gameQueue.getByNetworkId(id);
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
		sendUpdates(updates -> updates.setVisibility(visibility, !lobby.manager.hasFocusedLiveLobby()));
	}

	@Override
	public void close() {
		lobby.close();
	}

	void onFocusedLiveLobbyChanged() {
		sendUpdates(updates -> updates.setVisibility(lobby.metadata.visibility(), !lobby.manager.hasFocusedLiveLobby()));
	}

	void onPlayersChanged() {
		sendUpdates(updates -> updates.setPlayersFrom(lobby));
	}

	private void sendUpdates(UnaryOperator<ClientLobbyUpdate.Set> updates) {
		if (managingPlayers.isEmpty()) {
			return;
		}

		ClientLobbyUpdate.Set set = ClientLobbyUpdate.Set.create();
		set = updates.apply(set);

		ClientManageLobbyMessage message = set.intoMessage(lobby.getMetadata().id().networkId());
		managingPlayers.sendPacket(LoveTropicsNetwork.CHANNEL, message);
	}
}
