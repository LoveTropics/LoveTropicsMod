package com.lovetropics.minigames.client.lobby.manage;

import com.lovetropics.minigames.client.lobby.manage.screen.ManageLobbyScreen;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyManageState;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyPlayer;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyQueue;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyQueuedGame;
import com.lovetropics.minigames.client.lobby.manage.state.update.ClientLobbyUpdate;
import com.lovetropics.minigames.client.lobby.manage.state.update.ServerLobbyUpdate;
import com.lovetropics.minigames.client.lobby.state.ClientCurrentGame;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.common.core.game.lobby.LobbyControls;
import com.lovetropics.minigames.common.core.game.lobby.LobbyVisibility;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.UnaryOperator;

public final class ClientLobbyManagement {
	private static Session session;

	public static void update(int id, ClientLobbyUpdate.Set updates) {
		Session session = ClientLobbyManagement.session;
		if (session == null || session.id != id) {
			ClientLobbyManagement.session = session = new Session(id, new ClientLobbyManageState());
		}

		updates.applyTo(session);
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
			lobby.setName(name);
			sendUpdates(updates -> updates.setName(name));
		}

		public void enqueueGame(ClientGameDefinition game) {
			sendUpdates(updates -> updates.enqueue(game));
		}

		public void removeQueuedGame(int id) {
			sendUpdates(updates -> updates.removeQueuedGame(id));
		}

		public void reorderQueuedGame(int id, int newIndex) {
			sendUpdates(updates -> updates.reorderQueuedGame(id, newIndex));
		}

		public void selectControl(LobbyControls.Type control) {
			sendUpdates(updates -> updates.selectControl(control));
		}

		public void publishLobby() {
			LobbyVisibility visibility = lobby.getVisibility();
			if (visibility.isPrivate()) {
				lobby.setVisibility(LobbyVisibility.PUBLIC, lobby.canFocusLive());
				sendUpdates(updates -> updates.setVisibility(LobbyVisibility.PUBLIC));
			}
		}

		public void focusLive() {
			LobbyVisibility visibility = lobby.getVisibility();
			if (visibility.isPublic() && !visibility.isFocusedLive() && lobby.canFocusLive()) {
				lobby.setVisibility(LobbyVisibility.PUBLIC_LIVE, false);
				sendUpdates(updates -> updates.setVisibility(LobbyVisibility.PUBLIC_LIVE));
			}
		}

		public void closeLobby() {
			sendUpdates(ServerLobbyUpdate.Set::close);
		}

		public void configure(int id) {
			ClientLobbyQueuedGame game = lobby.getQueue().byId(id);
			if (game != null) {
				sendUpdates(updates -> updates.configure(id, game));
			}
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

		public void handleInitialize(List<ClientGameDefinition> installedGames, ClientLobbyQueue queue) {
			lobby.setInstalledGames(installedGames);
			lobby.setQueue(queue);

			Minecraft.getInstance().displayGuiScreen(screen);
		}

		public void handleName(String name) {
			lobby.setName(name);
			screen.updateNameField();
		}

		public void handleCurrentGame(@Nullable ClientCurrentGame game) {
			lobby.setCurrentGame(game);
			screen.updateGameEntries();
		}

		public void handleQueueUpdate(IntList queue, Int2ObjectMap<ClientLobbyQueuedGame> updated) {
			lobby.updateQueue(queue, updated);
			screen.updateGameEntries();
		}

		public void handlePlayers(List<ClientLobbyPlayer> players) {
			lobby.setPlayers(players);
		}

		public void handleControlsState(LobbyControls.State state) {
			lobby.setControlsState(state);
			screen.updateControlsState();
		}

		public void handleVisibility(LobbyVisibility visibility, boolean canFocusLive) {
			lobby.setVisibility(visibility, canFocusLive);
			screen.updatePublishState();
		}
	}
}
