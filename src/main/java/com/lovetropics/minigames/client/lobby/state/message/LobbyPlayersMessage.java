package com.lovetropics.minigames.client.lobby.state.message;

import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobbyPlayers;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public record LobbyPlayersMessage(int id, Set<UUID> players) {
	public static LobbyPlayersMessage update(IGameLobby lobby) {
		IGameLobbyPlayers players = lobby.getPlayers();
		Set<UUID> playerIds = new ObjectOpenHashSet<>(players.size());
		for (ServerPlayer player : players) {
			playerIds.add(player.getUUID());
		}
		return new LobbyPlayersMessage(lobby.getMetadata().id().networkId(), playerIds);
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeVarInt(id);
		buffer.writeCollection(players, FriendlyByteBuf::writeUUID);
	}

	public static LobbyPlayersMessage decode(FriendlyByteBuf buffer) {
		int id = buffer.readVarInt();
		ObjectOpenHashSet<UUID> players = buffer.readCollection(ObjectOpenHashSet::new, FriendlyByteBuf::readUUID);
		return new LobbyPlayersMessage(id, players);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ClientLobbyManager.get(id).ifPresent(state -> state.setPlayers(players));
	}
}
