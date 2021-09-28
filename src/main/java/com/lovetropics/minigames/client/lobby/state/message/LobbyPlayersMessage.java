package com.lovetropics.minigames.client.lobby.state.message;

import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobbyPlayers;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class LobbyPlayersMessage {
	private final int id;
	private final int participantCount;
	private final int spectatorCount;

	private LobbyPlayersMessage(int id, int participantCount, int spectatorCount) {
		this.id = id;
		this.participantCount = participantCount;
		this.spectatorCount = spectatorCount;
	}

	public static LobbyPlayersMessage update(IGameLobby lobby) {
		IGameLobbyPlayers players = lobby.getPlayers();
		int participantCount = players.getParticipantCount();
		int spectatorCount = players.getSpectatorCount();
		return new LobbyPlayersMessage(lobby.getMetadata().id().networkId(), participantCount, spectatorCount);
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(id);
		buffer.writeVarInt(participantCount);
		buffer.writeVarInt(spectatorCount);
	}

	public static LobbyPlayersMessage decode(PacketBuffer buffer) {
		int id = buffer.readVarInt();
		int participantCount = buffer.readVarInt();
		int spectatorCount = buffer.readVarInt();
		return new LobbyPlayersMessage(id, participantCount, spectatorCount);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientLobbyManager.get(id).ifPresent(state -> {
				state.setPlayerCounts(participantCount, spectatorCount);
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
