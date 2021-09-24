package com.lovetropics.minigames.client.lobby.state.message;

import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyPlayerEntry;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// TODO: send
public final class ClientLobbyPlayersMessage {
	private final int id;
	private final Operation operation;
	private final List<ClientLobbyPlayerEntry> players;

	private ClientLobbyPlayersMessage(int id, Operation operation, List<ClientLobbyPlayerEntry> players) {
		this.id = id;
		this.operation = operation;
		this.players = players;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(id);
		buffer.writeEnumValue(operation);
		buffer.writeVarInt(players.size());
		for (ClientLobbyPlayerEntry player : players) {
			player.encode(buffer);
		}
	}

	public static ClientLobbyPlayersMessage decode(PacketBuffer buffer) {
		int id = buffer.readVarInt();
		Operation operation = buffer.readEnumValue(Operation.class);

		int playerCount = buffer.readVarInt();
		List<ClientLobbyPlayerEntry> players = new ArrayList<>(playerCount);
		for (int i = 0; i < playerCount; i++) {
			players.add(ClientLobbyPlayerEntry.decode(buffer));
		}

		return new ClientLobbyPlayersMessage(id, operation, players);
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
