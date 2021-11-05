package com.lovetropics.minigames.client.lobby.state.message;

import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobbyPlayers;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class LobbyPlayersMessage {
	private final int id;
	private final int playerCount;

	private LobbyPlayersMessage(int id, int playerCount) {
		this.id = id;
		this.playerCount = playerCount;
	}

	public static LobbyPlayersMessage update(IGameLobby lobby) {
		IGameLobbyPlayers players = lobby.getPlayers();
		int playerCount = players.size();
		return new LobbyPlayersMessage(lobby.getMetadata().id().networkId(), playerCount);
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(id);
		buffer.writeVarInt(playerCount);
	}

	public static LobbyPlayersMessage decode(PacketBuffer buffer) {
		int id = buffer.readVarInt();
		int playerCount = buffer.readVarInt();
		return new LobbyPlayersMessage(id, playerCount);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientLobbyManager.get(id).ifPresent(state -> {
				state.setPlayerCounts(playerCount);
			});
		});
		ctx.get().setPacketHandled(true);
	}
}
