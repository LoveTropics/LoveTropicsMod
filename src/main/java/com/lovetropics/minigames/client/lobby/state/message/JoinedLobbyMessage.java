package com.lovetropics.minigames.client.lobby.state.message;

import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record JoinedLobbyMessage(int id) {
	public static JoinedLobbyMessage create(IGameLobby lobby) {
		return new JoinedLobbyMessage(lobby.getMetadata().id().networkId());
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeVarInt(id);
	}

	public static JoinedLobbyMessage decode(FriendlyByteBuf buffer) {
		int instanceId = buffer.readVarInt();
		return new JoinedLobbyMessage(instanceId);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ClientLobbyManager.setJoined(id);
	}
}
