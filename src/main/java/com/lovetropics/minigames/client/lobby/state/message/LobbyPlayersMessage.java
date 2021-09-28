package com.lovetropics.minigames.client.lobby.state.message;

import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import net.minecraft.entity.player.ServerPlayerEntity;
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
		int participantCount = 0;
		int spectatorCount = 0;

		for (ServerPlayerEntity player : lobby.getPlayers()) {
			PlayerRole role = getEffectiveRoleFor(lobby, player);

			if (role == PlayerRole.PARTICIPANT) participantCount++;
			else spectatorCount++;
		}

		return new LobbyPlayersMessage(lobby.getMetadata().id().networkId(), participantCount, spectatorCount);
	}

	private static PlayerRole getEffectiveRoleFor(IGameLobby lobby, ServerPlayerEntity player) {
		IGamePhase currentPhase = lobby.getCurrentPhase();

		PlayerRole playingRole = currentPhase != null ? currentPhase.getRoleFor(player) : null;
		PlayerRole registeredRole = lobby.getPlayers().getRegisteredRoleFor(player);

		return playingRole != null ? playingRole : registeredRole;
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
