package com.lovetropics.minigames.client.lobby.manage.state.update;

import com.lovetropics.minigames.client.lobby.manage.ClientLobbyManagement;
import com.lovetropics.minigames.client.lobby.manage.ClientManageLobbyMessage;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyPlayer;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyQueue;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyQueuedGame;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.lobby.LobbyControls;
import com.lovetropics.minigames.common.core.game.lobby.ILobbyGameQueue;
import com.lovetropics.minigames.common.core.game.lobby.QueuedGame;
import com.lovetropics.minigames.common.util.PartialUpdate;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

public abstract class ClientLobbyUpdate extends PartialUpdate<ClientLobbyManagement.Session> {
	private static final PartialUpdate.Family<ClientLobbyManagement.Session> FAMILY = Family.of(Type.values());

	public static final class Set extends AbstractSet<ClientLobbyManagement.Session> {
		private Set() {
			super(FAMILY);
		}

		public static Set create() {
			return new Set();
		}

		public static Set initialize(IGameLobby lobby) {
			return new Set()
					.setName(lobby.getMetadata().name())
					.initInstalledGames(ClientGameDefinition.collectInstalled())
					.initQueue(lobby.getGameQueue())
					.setPlayersFrom(lobby)
					.setCurrentGame(lobby.getCurrentGame() != null ? lobby.getCurrentGame().getDefinition() : null)
					.setControlState(lobby.getControls().asState());
		}

		public static Set decode(PacketBuffer buffer) {
			Set set = new Set();
			set.decodeSelf(buffer);
			return set;
		}

		public Set setName(String name) {
			this.add(new SetName(name));
			return this;
		}

		public Set initInstalledGames(List<ClientGameDefinition> installedGames) {
			this.add(new InitInstalledGames(installedGames));
			return this;
		}

		public Set initQueue(ILobbyGameQueue queue) {
			ClientLobbyQueue clientQueue = new ClientLobbyQueue();
			for (QueuedGame game : queue) {
				ClientGameDefinition definition = ClientGameDefinition.from(game.definition());
				clientQueue.add(game.networkId(), new ClientLobbyQueuedGame(definition));
			}

			this.add(new InitQueue(clientQueue));

			return this;
		}

		public Set setPlayersFrom(IGameLobby lobby) {
			List<ClientLobbyPlayer> players = lobby.getPlayers().stream()
					.map(player -> ClientLobbyPlayer.from(lobby, player))
					.collect(Collectors.toList());
			this.add(new SetPlayers(players));
			return this;
		}

		public Set setControlState(LobbyControls.State controlsState) {
			this.add(new SetControlsState(controlsState));
			return this;
		}

		public Set setCurrentGame(IGameDefinition currentGame) {
			this.add(new SetCurrentGame(currentGame != null ? ClientGameDefinition.from(currentGame) : null));
			return this;
		}

		public Set updateQueue(ILobbyGameQueue queue, int... updatedIds) {
			IntList order = new IntArrayList(queue.size());
			Int2ObjectMap<ClientLobbyQueuedGame> updated = new Int2ObjectArrayMap<>();

			for (QueuedGame game : queue) {
				order.add(game.networkId());

				for (int id : updatedIds) {
					if (id == game.networkId()) {
						ClientGameDefinition definition = ClientGameDefinition.from(game.definition());
						updated.put(id, new ClientLobbyQueuedGame(definition));
						break;
					}
				}
			}

			this.add(new UpdateQueue(order, updated));

			return this;
		}

		public ClientManageLobbyMessage intoMessage(int id) {
			return new ClientManageLobbyMessage(id, this);
		}
	}

	public enum Type implements AbstractType<ClientLobbyManagement.Session> {
		INIT_INSTALLED_GAMES(InitInstalledGames::decode),
		INIT_QUEUE(InitQueue::decode),
		SET_NAME(SetName::decode),
		SET_CURRENT_GAME(SetCurrentGame::decode),
		UPDATE_QUEUE(UpdateQueue::decode),
		SET_PLAYERS(SetPlayers::decode),
		SET_CONTROLS_STATE(SetControlsState::decode);

		private final Function<PacketBuffer, ClientLobbyUpdate> decode;

		Type(Function<PacketBuffer, ClientLobbyUpdate> decode) {
			this.decode = decode;
		}

		@Override
		public ClientLobbyUpdate decode(PacketBuffer buffer) {
			return decode.apply(buffer);
		}
	}

	protected ClientLobbyUpdate(Type type) {
		super(type);
	}

	public static final class InitInstalledGames extends ClientLobbyUpdate {
		private final List<ClientGameDefinition> installedGames;

		InitInstalledGames(List<ClientGameDefinition> installedGames) {
			super(Type.INIT_INSTALLED_GAMES);
			this.installedGames = installedGames;
		}

		@Override
		public void applyTo(ClientLobbyManagement.Session session) {
			session.handleInstalledGames(installedGames);
		}

		@Override
		protected void encode(PacketBuffer buffer) {
			buffer.writeVarInt(installedGames.size());
			for (ClientGameDefinition game : installedGames) {
				game.encode(buffer);
			}
		}

		static InitInstalledGames decode(PacketBuffer buffer) {
			int installedSize = buffer.readVarInt();
			List<ClientGameDefinition> installedGames = new ArrayList<>(installedSize);
			for (int i = 0; i < installedSize; i++) {
				installedGames.add(ClientGameDefinition.decode(buffer));
			}

			return new InitInstalledGames(installedGames);
		}
	}

	public static final class InitQueue extends ClientLobbyUpdate {
		private final ClientLobbyQueue queue;

		InitQueue(ClientLobbyQueue queue) {
			super(Type.INIT_QUEUE);
			this.queue = queue;
		}

		@Override
		public void applyTo(ClientLobbyManagement.Session session) {
			session.handleQueue(queue);
		}

		@Override
		protected void encode(PacketBuffer buffer) {
			queue.encode(buffer);
		}

		static InitQueue decode(PacketBuffer buffer) {
			return new InitQueue(ClientLobbyQueue.decode(buffer));
		}
	}

	public static final class SetName extends ClientLobbyUpdate {
		private final String name;

		SetName(String name) {
			super(Type.SET_NAME);
			this.name = name;
		}

		@Override
		public void applyTo(ClientLobbyManagement.Session session) {
			session.handleName(name);
		}

		@Override
		protected void encode(PacketBuffer buffer) {
			buffer.writeString(name, 200);
		}

		static SetName decode(PacketBuffer buffer) {
			return new SetName(buffer.readString(200));
		}
	}

	public static final class SetCurrentGame extends ClientLobbyUpdate {
		@Nullable
		private final ClientGameDefinition currentGame;

		SetCurrentGame(@Nullable ClientGameDefinition currentGame) {
			super(Type.SET_CURRENT_GAME);
			this.currentGame = currentGame;
		}

		@Override
		public void applyTo(ClientLobbyManagement.Session session) {
			session.handleCurrentGame(currentGame);
		}

		@Override
		protected void encode(PacketBuffer buffer) {
			buffer.writeBoolean(currentGame != null);
			if (currentGame != null) {
				currentGame.encode(buffer);
			}
		}

		static SetCurrentGame decode(PacketBuffer buffer) {
			ClientGameDefinition game = buffer.readBoolean() ? ClientGameDefinition.decode(buffer) : null;
			return new SetCurrentGame(game);
		}
	}

	public static final class UpdateQueue extends ClientLobbyUpdate {
		private final IntList queue;
		private final Int2ObjectMap<ClientLobbyQueuedGame> updated;

		UpdateQueue(IntList queue, Int2ObjectMap<ClientLobbyQueuedGame> updated) {
			super(Type.UPDATE_QUEUE);
			this.queue = queue;
			this.updated = updated;
		}

		@Override
		public void applyTo(ClientLobbyManagement.Session session) {
			session.handleQueueUpdate(queue, updated);
		}

		@Override
		protected void encode(PacketBuffer buffer) {
			buffer.writeVarInt(queue.size());
			queue.forEach((IntConsumer) buffer::writeVarInt);

			buffer.writeVarInt(updated.size());
			updated.forEach((id, game) -> {
				buffer.writeVarInt(id);
				game.encode(buffer);
			});
		}

		static UpdateQueue decode(PacketBuffer buffer) {
			int queueSize = buffer.readVarInt();
			IntList queue = new IntArrayList(queueSize);
			for (int i = 0; i < queueSize; i++) {
				queue.add(buffer.readVarInt());
			}

			int updatedSize = buffer.readVarInt();
			Int2ObjectMap<ClientLobbyQueuedGame> updated = new Int2ObjectArrayMap<>(updatedSize);
			for (int i = 0; i < updatedSize; i++) {
				int id = buffer.readVarInt();
				ClientLobbyQueuedGame game = ClientLobbyQueuedGame.decode(buffer);
				updated.put(id, game);
			}

			return new UpdateQueue(queue, updated);
		}
	}

	public static final class SetPlayers extends ClientLobbyUpdate {
		private final List<ClientLobbyPlayer> players;

		SetPlayers(List<ClientLobbyPlayer> players) {
			super(Type.SET_PLAYERS);
			this.players = players;
		}

		@Override
		public void applyTo(ClientLobbyManagement.Session session) {
			session.handlePlayers(players);
		}

		@Override
		protected void encode(PacketBuffer buffer) {
			buffer.writeVarInt(players.size());
			for (ClientLobbyPlayer player : players) {
				player.encode(buffer);
			}
		}

		static SetPlayers decode(PacketBuffer buffer) {
			int size = buffer.readVarInt();
			List<ClientLobbyPlayer> players = new ArrayList<>(size);
			for (int i = 0; i < size; i++) {
				players.add(ClientLobbyPlayer.decode(buffer));
			}
			return new SetPlayers(players);
		}
	}

	public static final class SetControlsState extends ClientLobbyUpdate {
		private final LobbyControls.State state;

		SetControlsState(LobbyControls.State state) {
			super(Type.SET_CONTROLS_STATE);
			this.state = state;
		}

		@Override
		public void applyTo(ClientLobbyManagement.Session session) {
			session.handleControlsState(state);
		}

		@Override
		protected void encode(PacketBuffer buffer) {
			state.encode(buffer);
		}

		static SetControlsState decode(PacketBuffer buffer) {
			return new SetControlsState(LobbyControls.State.decode(buffer));
		}
	}
}
