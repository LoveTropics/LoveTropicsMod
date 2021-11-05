package com.lovetropics.minigames.client.lobby.state.message;

import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobbyPlayers;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public final class LobbyPlayersMessage {
	private final int id;
	private final Set<UUID> players;

	private LobbyPlayersMessage(int id, Set<UUID> players) {
		this.id = id;
		this.players = players;
	}

	public static LobbyPlayersMessage update(IGameLobby lobby) {
		IGameLobbyPlayers players = lobby.getPlayers();
		Set<UUID> playerIds = new ObjectOpenHashSet<>(players.size());
		for (ServerPlayerEntity player : players) {
			playerIds.add(player.getUniqueID());
		}
		return new LobbyPlayersMessage(lobby.getMetadata().id().networkId(), playerIds);
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(id);
		buffer.writeVarInt(players.size());
		for (UUID player : players) {
			buffer.writeUniqueId(player);
		}
	}

	public static LobbyPlayersMessage decode(PacketBuffer buffer) {
		int id = buffer.readVarInt();
		int playerCount = buffer.readVarInt();
		Set<UUID> players = new ObjectOpenHashSet<>(playerCount);
		for (int i = 0; i < playerCount; i++) {
			players.add(buffer.readUniqueId());
		}
		return new LobbyPlayersMessage(id, players);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientLobbyManager.get(id).ifPresent(state -> {
				state.setPlayers(players);
			});
		});
		ctx.get().setPacketHandled(true);
	}
}
