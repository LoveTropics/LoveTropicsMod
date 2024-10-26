package com.lovetropics.minigames.client.lobby.manage;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.lobby.LobbyKeybinds;
import com.lovetropics.minigames.client.lobby.manage.screen.ManageLobbyScreen;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyManageState;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyPlayer;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyQueue;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyQueuedGame;
import com.lovetropics.minigames.client.lobby.manage.state.update.ClientLobbyUpdate;
import com.lovetropics.minigames.client.lobby.manage.state.update.ServerLobbyUpdate;
import com.lovetropics.minigames.client.lobby.state.ClientCurrentGame;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.common.core.game.lobby.LobbyControls;
import com.lovetropics.minigames.common.core.game.lobby.LobbyVisibility;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.UnaryOperator;

@EventBusSubscriber(modid = LoveTropics.ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class ClientLobbyManagement {
	@Nullable
	private static Session session;

	public static void update(int id, ClientLobbyUpdate.Set updates) {
		Session session = ClientLobbyManagement.session;
		if (session == null || session.id != id) {
			ClientLobbyManagement.session = session = new Session(id, new ClientLobbyManageState());
		}

		updates.applyTo(session);
	}

	@SubscribeEvent
	public static void onKeyInput(ClientTickEvent.Post event) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null && LobbyKeybinds.MANAGE.consumeClick()) {
            if (ClientLobbyManager.getJoined() != null) {
                player.connection.sendUnsignedCommand("game manage");
            } else {
                player.connection.sendUnsignedCommand("game create");
            }
        }
	}

	public static final class Session {
		private final int id;
		private final ClientLobbyManageState lobby;
		@Nullable
		private ManageLobbyScreen screen;
		private Listener listener = Listener.EMPTY;

		private Session(int id, ClientLobbyManageState lobby) {
			this.id = id;
			this.lobby = lobby;
		}

		public int id() {
			return id;
		}

		public ClientLobbyManageState lobby() {
			return lobby;
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
			PacketDistributor.sendToServer(message);
		}

		public void close() {
			PacketDistributor.sendToServer(ServerManageLobbyMessage.stop(id));

			if (ClientLobbyManagement.session == this) {
				ClientLobbyManagement.session = null;
			}
		}

		public void handleInitialize(List<ClientGameDefinition> installedGames, ClientLobbyQueue queue) {
			lobby.setInstalledGames(installedGames);
			lobby.setQueue(queue);

			if (screen == null) {
				screen = new ManageLobbyScreen(this);
				listener = screen;
			}
			Minecraft.getInstance().setScreen(screen);
		}

		public void handleName(String name) {
			lobby.setName(name);
			listener.updateNameField();
		}

		public void handleCurrentGame(@Nullable ClientCurrentGame game) {
			lobby.setCurrentGame(game);
			listener.updateGameEntries();
		}

		public void handleQueueUpdate(IntList queue, Int2ObjectMap<ClientLobbyQueuedGame> updated) {
			lobby.updateQueue(queue, updated);
			listener.updateGameEntries();
		}

		public void handlePlayers(List<ClientLobbyPlayer> players) {
			lobby.setPlayers(players);
		}

		public void handleControlsState(LobbyControls.State state) {
			lobby.setControlsState(state);
			listener.updateControlsState();
		}

		public void handleVisibility(LobbyVisibility visibility, boolean canFocusLive) {
			lobby.setVisibility(visibility, canFocusLive);
			listener.updatePublishState();
		}
	}

	public interface Listener {
		Listener EMPTY = new Listener() {
			@Override
			public void updateNameField() {
			}

			@Override
			public void updateGameEntries() {
			}

			@Override
			public void updateControlsState() {
			}

			@Override
			public void updatePublishState() {
			}
		};

		void updateNameField();

		void updateGameEntries();

		void updateControlsState();

		void updatePublishState();
	}
}
