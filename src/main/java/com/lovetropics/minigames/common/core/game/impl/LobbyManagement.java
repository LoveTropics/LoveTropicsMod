package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.client.lobby.manage.ClientManageLobbyMessage;
import com.lovetropics.minigames.client.lobby.manage.state.update.ClientLobbyUpdate;
import com.lovetropics.minigames.common.core.game.IGame;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.lobby.*;
import com.lovetropics.minigames.common.core.game.player.MutablePlayerSet;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.util.Scheduler;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.function.UnaryOperator;

final class LobbyManagement implements ILobbyManagement {
	private final GameLobby lobby;

	private final MutablePlayerSet managingPlayers;

	LobbyManagement(GameLobby lobby) {
		this.lobby = lobby;
		this.managingPlayers = new MutablePlayerSet(lobby.getServer());
	}

	void onGameStateChange() {
		sendUpdates(updates -> {
			IGame currentGame = lobby.getCurrentGame();
			ILobbyGameQueue gameQueue = lobby.getGameQueue();
			LobbyControls controls = lobby.getControls();
			return updates.setCurrentGame(currentGame != null ? currentGame.getDefinition() : null)
					.updateQueue(gameQueue)
					.setControlState(controls.asState());
		});
	}

	@Override
	public boolean startManaging(ServerPlayerEntity player) {
		if (canManage(player.getCommandSource())) {
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
		LobbyGameQueue gameQueue = lobby.gameQueue;
		QueuedGame queued = gameQueue.byNetworkId(id);
		if (queued != null && gameQueue.remove(queued)) {
			sendUpdates(updates -> updates.updateQueue(gameQueue));
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

	private void sendUpdates(UnaryOperator<ClientLobbyUpdate.Set> updates) {
		ClientLobbyUpdate.Set set = ClientLobbyUpdate.Set.create();
		set = updates.apply(set);

		ClientManageLobbyMessage message = set.intoMessage(lobby.getMetadata().id().networkId());
		managingPlayers.sendPacket(LoveTropicsNetwork.CHANNEL, message);
	}
}
