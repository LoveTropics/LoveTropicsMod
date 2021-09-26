package com.lovetropics.minigames.client.lobby.state.message;

import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyPlayerEntry;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public final class LobbyPlayersMessage {
	private final int id;
	private final Operation operation;
	private final List<ClientLobbyPlayerEntry> players;

	private LobbyPlayersMessage(int id, Operation operation, List<ClientLobbyPlayerEntry> players) {
		this.id = id;
		this.operation = operation;
		this.players = players;
	}

	public static LobbyPlayersMessage add(IGameLobby lobby, Iterable<ServerPlayerEntity> players) {
		return create(lobby, Operation.ADD, players);
	}

	public static LobbyPlayersMessage remove(IGameLobby lobby, Iterable<ServerPlayerEntity> players) {
		return create(lobby, Operation.REMOVE, players);
	}

	public static LobbyPlayersMessage set(IGameLobby lobby, Iterable<ServerPlayerEntity> players) {
		return create(lobby, Operation.SET, players);
	}

	private static LobbyPlayersMessage create(IGameLobby lobby, Operation operation, Iterable<ServerPlayerEntity> players) {
		IActiveGame activeGame = lobby.getActiveGame();

		List<ClientLobbyPlayerEntry> entries = new ArrayList<>();
		for (ServerPlayerEntity player : players) {
			UUID uuid = player.getUniqueID();
			PlayerRole registeredRole = lobby.getRegisteredRoleFor(player);
			PlayerRole activeRole = activeGame != null ? activeGame.getRoleFor(player) : null;
			entries.add(new ClientLobbyPlayerEntry(uuid, registeredRole, activeRole));
		}

		return new LobbyPlayersMessage(lobby.getMetadata().id().networkId(), operation, entries);
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(id);
		buffer.writeEnumValue(operation);
		buffer.writeVarInt(players.size());
		for (ClientLobbyPlayerEntry player : players) {
			player.encode(buffer);
		}
	}

	public static LobbyPlayersMessage decode(PacketBuffer buffer) {
		int id = buffer.readVarInt();
		Operation operation = buffer.readEnumValue(Operation.class);

		int playerCount = buffer.readVarInt();
		List<ClientLobbyPlayerEntry> players = new ArrayList<>(playerCount);
		for (int i = 0; i < playerCount; i++) {
			players.add(ClientLobbyPlayerEntry.decode(buffer));
		}

		return new LobbyPlayersMessage(id, operation, players);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientLobbyManager.get(id).ifPresent(state -> {
				switch (operation) {
					case ADD:
						state.addPlayers(players);
						break;
					case REMOVE:
						state.removePlayers(players);
						break;
					case SET:
						state.setPlayers(players);
						break;
				}
			});
		});
		ctx.get().setPacketHandled(true);
	}

	public enum Operation {
		ADD,
		REMOVE,
		SET
	}
}
