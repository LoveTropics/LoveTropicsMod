package com.lovetropics.minigames.client.lobby.manage;

import com.lovetropics.minigames.client.lobby.manage.screen.ManageLobbyScreen;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyManageState;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyPlayer;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyQueue;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyQueuedGame;
import com.lovetropics.minigames.client.lobby.manage.state.update.ClientLobbyUpdate;
import com.lovetropics.minigames.client.lobby.manage.state.update.ServerLobbyUpdate;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.common.core.game.lobby.LobbyControls;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.function.UnaryOperator;

public final class ClientLobbyManagement {
	private static Session session;

	public static void update(int id, ClientLobbyUpdate.Set updates) {
		Session session = ClientLobbyManagement.session;
		if (session == null || session.id != id) {
			ClientLobbyManagement.session = session = new Session(id, new ClientLobbyManageState());
			displayScreen(session);
		}

		updates.applyTo(session);
	}

	static void displayScreen(Session session) {
		Minecraft.getInstance().displayGuiScreen(session.screen);
	}

	public static final class Session {
		final int id;
		final ClientLobbyManageState lobby;
		final ManageLobbyScreen screen;

		Session(int id, ClientLobbyManageState lobby) {
			this.id = id;
			this.lobby = lobby;
			this.screen = new ManageLobbyScreen(this);
		}

		public int id() {
			return this.id;
		}

		public ClientLobbyManageState lobby() {
			return this.lobby;
		}

		public void setName(String name) {
			sendUpdates(updates -> updates.setName(name));
		}

		public void enqueueGame(ClientGameDefinition game) {
			sendUpdates(updates -> updates.enqueue(game));
		}

		public void removeQueuedGame(int id) {
			sendUpdates(updates -> updates.removeQueuedGame(id));
		}

		public void selectControl(LobbyControls.Type control) {
			sendUpdates(updates -> updates.selectControl(control));
		}

		private void sendUpdates(UnaryOperator<ServerLobbyUpdate.Set> updates) {
			ServerLobbyUpdate.Set set = ServerLobbyUpdate.Set.create();
			set = updates.apply(set);

			ServerManageLobbyMessage message = set.intoMessage(id);
			LoveTropicsNetwork.CHANNEL.sendToServer(message);
		}

		public void close() {
			LoveTropicsNetwork.CHANNEL.sendToServer(ServerManageLobbyMessage.stop(id));

			if (ClientLobbyManagement.session == this) {
				ClientLobbyManagement.session = null;
			}
		}

		public void handleInstalledGames(List<ClientGameDefinition> installedGames) {
			lobby.setInstalledGames(installedGames);
			screen.updateGameList();
		}

		public void handleQueue(ClientLobbyQueue queue) {
			lobby.setQueue(queue);
			screen.updateGameList();
		}

		public void handleName(String name) {
			lobby.setName(name);
			screen.updateNameField();
		}

		public void handleQueueUpdate(IntList queue, Int2ObjectMap<ClientLobbyQueuedGame> updated) {
			lobby.updateQueue(queue, updated);
			screen.updateGameList();
		}

		public void handlePlayers(List<ClientLobbyPlayer> players) {
			lobby.setPlayers(players);
		}

		public void handleControlsState(LobbyControls.State state) {
			lobby.setControlsState(state);
			screen.setControlsState();
		}
	}
}
